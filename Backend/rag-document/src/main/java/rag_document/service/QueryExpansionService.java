package rag_document.service;

import dev.langchain4j.model.chat.ChatLanguageModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class QueryExpansionService {

    private final ChatLanguageModel chatLanguageModel;

    /** HyDE is Hypothetical Document Embedding
     * HyDE: Generate a hypothetical document that would answer the query.
     * This document is then used for vector search instead of the raw query.
     *
     * Why: Short queries like "BMW" produce weak embeddings.
     * A hypothetical answer produces richer embeddings that match real document chunks better.
     */
    public String expandWithHyDE(String originalQuery){

        log.info("Expanding query with HyDE : '{}'", originalQuery);

        String prompt = String.format("""
            Write a single factual sentence that directly answers this question: "%s"
            
            Rules:
            - ONE sentence only
            - Be specific and factual
            - Use domain-specific vocabulary
            - Do not say "I don't know" — make your best guess
            - Do not include phrases like "Based on" or "According to"
            
            Sentence:""" , originalQuery);

        try {
            String hypothetical = chatLanguageModel.generate(prompt).trim();
            // Strip leading label if LLM adds "Sentence: ..."
            if (hypothetical.toLowerCase().startsWith("sentence:")) {
                hypothetical = hypothetical.substring(9).trim();
            }
            log.info("HyDE expanded query: '{}'", hypothetical);
            return hypothetical;
        } catch (Exception e) {
            log.warn("HyDE expansion failed, using original query: {}", e.getMessage());
            return originalQuery; // Graceful fallback
        }
    }

    /**
     * Term expansion: Add synonyms/related terms to the original query.
     * Used for BM25 to catch variations of the same concept.
     * 
     * Why: BM25 is exact keyword matching. "car" won't match "vehicle".
     * Expanding terms catches more relevant documents.
     */
    public String expandWithSynonyms(String originalQuery) {
        log.info("Expanding query with synonyms: '{}'", originalQuery);

        String prompt = String.format("""
            Given this search query: "%s"
            
            Return ONLY a comma-separated list of 3-5 related terms or synonyms.
            No explanations. No sentences. Just the terms.
            
            Example input: "car speed"
            Example output: automobile velocity, vehicle acceleration, mph, top speed, performance
            
            Output:""", originalQuery);

        try {
            String expanded = chatLanguageModel.generate(prompt).trim();
            String combined = originalQuery + " " + expanded.replace(",", " ");
            log.info("Synonym expanded query: '{}'", combined);
            return combined;
        } catch (Exception e) {
            log.warn("Synonym expansion failed, using original query: {}", e.getMessage());
            return originalQuery;
        }
    }
}
