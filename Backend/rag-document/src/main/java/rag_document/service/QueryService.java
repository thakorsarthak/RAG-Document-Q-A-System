package rag_document.service;

import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rag_document.dto.QueryRequest;
import rag_document.dto.QueryResponse;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryService {

    private final VectorStoreService vectorStoreService;
    private final ChatLanguageModel chatLanguageModel;

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

    private String buildPrompt(String context , String question){

        return String.format("""
                You are a helpful assistant. Use the context below to answer the question.
                
                Context:
                %s
                
                Question: %s
                Answer based on the context:""" , context , question);


    }


}
