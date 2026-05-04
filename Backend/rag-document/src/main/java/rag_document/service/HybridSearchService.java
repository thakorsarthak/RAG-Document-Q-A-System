package rag_document.service;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rag_document.dto.SearchResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HybridSearchService {

    private final VectorStoreService vectorStoreService;
    private final LuceneSearchService luceneSearchService;

    private static final int k = 60; // RRF constant

    /** Performs hybrid search combining vector and BM25 results using RRF **/

    public List<SearchResult> hybridSearch(String query, int maxResults, double alpha) throws Exception {
        log.info("Hybrid search for: '{}' with alpha={}", query, alpha);

        // Step 1: Get vector search results
        List<EmbeddingMatch<TextSegment>> vectorResults = vectorStoreService.searchSimilar(query, maxResults);
        log.info("Vector search returned {} results", vectorResults.size());

        // Step 2: Get BM25 search results
        List<SearchResult> bm25Results = luceneSearchService.search(query, maxResults);
        log.info("BM25 search returned {} results", bm25Results.size());

        // Step 3: Convert vector results to SearchResult format
        List<SearchResult> vectorSearchResults = convertVectorResults(vectorResults);

        // NEW: Filter out low-quality results BEFORE merging
        vectorSearchResults = filterByScore(vectorSearchResults, 0.75); // Keep only >0.75 similarity
        bm25Results = filterByScore(bm25Results, 0.5); // Keep only >0.5 BM25 score

        log.info("After filtering: {} vector results, {} BM25 results",
                vectorSearchResults.size(), bm25Results.size());

        // Step 4: Merge using RRF
        List<SearchResult> mergedResults = mergeWithRRF(vectorSearchResults, bm25Results, alpha);

        // Step 5: Absolute minimum RRF score threshold
        double MIN_RRF_SCORE = 0.01;  // Anything below this is noise

        mergedResults = mergedResults.stream()
                .filter(r -> r.getScore() >= MIN_RRF_SCORE)
                .collect(Collectors.toList());

        log.info("After RRF threshold (min {}): {} results remain", MIN_RRF_SCORE, mergedResults.size());

        log.info("Hybrid search returned {} merged results", mergedResults.size());
        return mergedResults.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }
    
    // converter for vector result
    private List<SearchResult> convertVectorResults(List<EmbeddingMatch<TextSegment>> matches){

        return matches.stream()
                .map(match -> {

                    TextSegment segment = match.embedded();

                    return SearchResult.builder()
                            .documentId(segment.metadata().getLong("documentId"))
                            .filename(segment.metadata().getString("filename"))
                            .chunkIndex(segment.metadata().getInteger("chunkIndex"))
                            .text(segment.text())
                            .score(match.score())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Merge two ranked lists using Reciprocal Rank Fusion (RRF)
     * @param vectorResults Results from vector search
     * @param bm25Results Results from BM25 search
     * @param alpha Weight for vector vs BM25 (0.0 = only BM25 , 1.0 = only vector)
     * */
    private List<SearchResult> mergeWithRRF(
            List<SearchResult> vectorResults,
            List<SearchResult> bm25Results ,
            double alpha){

        Map<String , SearchResult>  resultMap = new HashMap<>();
        Map<String , Double> rrfScores = new HashMap<>();


       /**
            Vector loop = initialize
            BM25 loop  = update or insert
        */

        //process vector results
        for (int i =0 ; i < vectorResults.size() ; i++){
            SearchResult result = vectorResults.get(i);
            String key = getResultKey(result);

            double rrfScore =(1.0 - alpha)* (1.0/(k + i +1));

            /**
              we are not adding here cause result is initialize from
              here so there will NO result for particular ResultKey
             */

            rrfScores.put(key, rrfScore);
            resultMap.put(key, result);
        }

        //Process BM25 results
        for (int i = 0; i < bm25Results.size(); i++){
            SearchResult result  = bm25Results.get(i);
            String key = getResultKey(result);

            double rrfScore =  alpha *(1.0/(k+1+i));

           /**
            Add to existing score or create new entry
            Here we are adding cause vector might have result for same
            ResultKey so bm25 score will be added to vector RRf Score
            */
            rrfScores.merge(key, rrfScore , Double::sum);
            resultMap.putIfAbsent(key , result);
        }


        //Sort by RRF score descending
        return rrfScores.entrySet().stream()
                .sorted(Map.Entry.<String , Double>comparingByValue().reversed())
                .map(entry -> {
                    SearchResult result = resultMap.get(entry.getKey());
                    // Update score to RRF score for transparency
                    result.setScore(entry.getValue());
                    return result;
                })
                .collect(Collectors.toList());
    }

    /**
     * Create unique key for a search result (documentId + chunkIndex)
     */
    private String getResultKey(SearchResult result) {

        return result.getDocumentId() + "_" + result.getChunkIndex();
    }


    /**
     * Filter results by minimum score threshold
     */
    private List<SearchResult> filterByScore(List<SearchResult> results, double minScore) {
        return results.stream()
                .filter(r -> r.getScore() >= minScore)
                .collect(Collectors.toList());
    }

}
