package rag_document.service;


import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import rag_document.dto.SearchResult;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HybridSearchService {

    private final VectorStoreService vectorStoreService;
    private final LuceneSearchService luceneSearchService;

    private static final int k = 60; // RRF constant

    /** Performs hybrid search combining vector and BM25 results using RRF **/

    public List<SearchResult> hybridSearch(String query , int maxResults , double alpha) throws Exception{

        log.info("Hybrid search for: '{}' with aplha= {}" , query , alpha);

        //step 1 : Get vector results
        List<EmbeddingMatch<TextSegment>> vectorResults = vectorStoreService.searchSimilar(query , maxResults);
        log.info("Vector search returned {} results",vectorResults.size());


        //Step 2 : Get bm25 results
        List<SearchResult> bm25Results = luceneSearchService.search(query ,maxResults);
        log.info("BM25 search returned {} results", bm25Results.size());

        //Step 3 : converting vector result into search result format
        List<SearchResult> vectorSearchResult = convertVectorResults(vectorResults);





       return null;
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

}
