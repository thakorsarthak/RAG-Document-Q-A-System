package rag_document.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rag_document.dto.QueryRequest;
import rag_document.dto.QueryResponse;
import rag_document.dto.SearchResult;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final VectorStoreService vectorStoreService;
    private final ChatLanguageModel chatLanguageModel;
    private final HybridSearchService hybridSearchService;

    public QueryResponse query(QueryRequest request){
        log.info("Processing query : {}", request.getQuestion());

        // first : Find relevant chunk form db
        List<EmbeddingMatch<TextSegment>> matches =
                vectorStoreService.searchSimilar(request.getQuestion() , 5);

        if(matches.isEmpty()) {

            return QueryResponse.builder()
                    .answer("I couldn't find any revelant information in the document to answer your question")
                    .sources(List.of())
                    .build();
        }

        // step 2 - build context from retrieved chunks
        String context = matches.stream()
                .map(match -> match.embedded().text())
                .collect(Collectors.joining("\n\n"));


        //step 3 - build prompt for llm
        String prompt = buildPrompt(context , request.getQuestion());

        log.info("Context sent to LLM: {}", context);
        log.info("Full prompt: {}", prompt);


        //step 4: get llm answer
        String answer = chatLanguageModel.generate(prompt);
        log.info("Generated answer: {}", answer);


        //step 5 : build response with sources

        List<QueryResponse.SourceChunk> sources = matches.stream()
                .map(match ->{
                    TextSegment segment = match.embedded();
                    return QueryResponse.SourceChunk.builder()
                            .documentId(segment.metadata().getLong("documentId"))
                            .filename(segment.metadata().getString("filename"))
                            .text(segment.text())
                            .score(match.score())
                            .build();
                })
                .collect(Collectors.toList());

        return QueryResponse.builder()
                .answer(answer)
                .sources(sources)
                .build();
    }


    /**
     * NEW: Hybrid query using both vector and BM25 search
     */

    public QueryResponse hybridQuery(QueryRequest request , double alpha){
        log.info("Processing hybrid query: {}" , request.getQuestion());
        try{

            //Step 1: hybrid search with RRF
            List<SearchResult> matches = hybridSearchService.hybridSearch(
                    request.getQuestion(),
                    5 , alpha);

            if(matches.isEmpty()){
                return QueryResponse.builder()
                        .answer("I couldn't find any relevant information in the documents to answer your question.")
                        .sources(List.of())
                        .build();
            }

            //Step 2 : Build context from retrieved chunks
            String context = matches.stream()
                    .map(SearchResult::getText)
                    .collect(Collectors.joining("\n\n"));

            //Step 3 : Build prompt for LLM
            String prompt = buildPrompt(context, request.getQuestion());

            //Step 4 : Get answer from llm
            String answer = chatLanguageModel.generate(prompt);
            log.info("Generated answer: {}", answer);


            //Step 5: Build response with sources
            List<QueryResponse.SourceChunk> sources = matches.stream()
                    .map(match -> QueryResponse.SourceChunk.builder()
                            .documentId(match.getDocumentId())
                            .filename(match.getFilename())
                            .text(match.getText())
                            .score(match.getScore()) //this is now rrf score
                            .build())
                    .collect(Collectors.toList());

            return  QueryResponse.builder()
                    .answer(answer)
                    .sources(sources)
                    .build();


        }catch (Exception e)
        {
            log.error("Hybrid query failed", e);
            return QueryResponse.builder()
                    .answer("An error occurred while processing your query.")
                    .sources(List.of())
                    .build();
        }

    }



    private String buildPrompt(String context , String question){

        return String.format("""
                You are a helpful assistant. Use the context below to answer the question.
                
                Context:
                %s
                
                Question: %s
                Answer based on the context:""" , context , question);


    }


}
