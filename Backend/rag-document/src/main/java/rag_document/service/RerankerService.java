package rag_document.service;


import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rag_document.dto.SearchResult;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RerankerService {

    private final ChatLanguageModel chatLanguageModel;

    /*
    * Cross-encoder reranking using ollama LLM
    *
    * Cause RRF ranks by position. Cross-encoder reads (query-chunk) together
    * and scores actual relevance. Much more accurate for final ranking
    *
    * Trade-off: N LLM calls (one per chunk). Only run on top K candidates from RRF
    *
    * We use RRF to get top K fast, then cross-encoder to rerank those K results.
    * */
    public List<SearchResult> rerank(String query , List<SearchResult> candidates){
        log.info("Reranking {} candidates for query: '{}'" , candidates.size(), query);

        List<SearchResult> reranked = candidates.stream()
                .map(candidate ->{
                    double relevanceScore = scoreRelevance(query,candidate.getText());
                    candidate.setScore(relevanceScore);
                return candidate;
                })
                .sorted(Comparator.comparingDouble(SearchResult::getScore).reversed())
                .collect(Collectors.toList());

        log.info("Reranking complete. Top result score: {}" ,
                reranked.isEmpty() ? "N/A" : reranked.get(0).getScore());

        return reranked;


    }


    /**
     * Ask LLM to score relevance of a chunk to the query.
     * Returns 0.0 to 1.0.
     *
     * Why LLM as cross-encoder:
     * - We don't have a dedicated reranker model (Cohere costs money)
     * - Ollama is free and local
     * - llama3.2 understands relevance well enough for this task
     */
    public double scoreRelevance(String query, String chunk){
            String prpmpt = String.format("""
                    Rate how relevant this text chunk is for answering the query 
                    
                    Query: %s
                    
                    Text chunk: %s
                    
                    Respond with ONLY a number between 0 to 10.
                    10 = perfectly answers the query
                    5  = partially relevant
                    0  = completely irrelevant
                
                    Number:""", query, chunk);

            try {
                String response = chatLanguageModel.generate(prpmpt).trim();

                //Extracting number from response (LLMs sometimes say "7" or "7/10" or "Score: 7")
                // Or formating
                String cleaned = response.replaceAll("[^0-9.]","");
                if(cleaned.isEmpty()) return 0.5; // return default 0.5 , if parsing fails

                double score = Double.parseDouble(cleaned.split("\\.")[0]); //Take integer part
                score = Math.max(0 , Math.min(10 , score)); // clamp to 0-10
                return score / 10.0; // Normalize to 0.0-1.0
            }
            catch (Exception e){
                log.warn("Reranking score failed for chunk, defaulting to 0.5: {}", e.getMessage());
                return 0.5;

            }

    }

}
