package rag_document.service;


import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.chroma.ChromaEmbeddingStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Text;
import rag_document.entity.ChunkMetadata;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final EmbeddingModel embeddingModel;

    @Value("${chroma.base-url}")
    private String chromaBaseUrl;

    private EmbeddingStore<TextSegment> embeddingStore;

    private EmbeddingStore<TextSegment> getEmbeddingStore() {
        if (embeddingStore == null) {
            embeddingStore = ChromaEmbeddingStore.builder()
                    .baseUrl(chromaBaseUrl)
                    .collectionName("documents")
                    .build();
        }
        return embeddingStore;
    }



    public void storeChunks(List<String> chunks, ChunkMetadata metadata) {
        log.info("Storing {} chunnks for document {}", chunks.size(), metadata.getDocumentId());



        List<TextSegment> segments = new ArrayList<>();
        List<Embedding> embeddings = new ArrayList<>();

        for (int i = 0; i < chunks.size(); i++) {

            String chunk = chunks.get(i);

            // create a metadata for this chunk
            Metadata chunkMeta = new Metadata();
            chunkMeta.put("documentId", metadata.getDocumentId());
            chunkMeta.put("filename", metadata.getFilename());
            chunkMeta.put("chunkIndex", i);
            chunkMeta.put("totalChunks", chunks.size());

            //debugging
            log.info("Storing chunk {} with metadata: docId={}, filename={}",
                    i, metadata.getDocumentId(), metadata.getFilename());


            //creating text segment
            TextSegment segment = TextSegment.from(chunk, chunkMeta);
            segments.add(segment);

            //creating embedding
            Embedding embedding = embeddingModel.embed(chunk).content();
            embeddings.add(embedding);

            log.info("Processed chunk {}/{}", i + 1, chunks.size());
        }

        //store all at once
        getEmbeddingStore().addAll(embeddings, segments);
        log.info("Stored {} chunks in ChromaDB", chunks.size());

    }

    public List<EmbeddingMatch<TextSegment>> searchSimilar(String query, int maxResults) {
        log.info("Searching for: {}", query);

        //embed the query
        Embedding queryEmbedding = embeddingModel.embed(query).content();

        //search in ChromaDB
        List<EmbeddingMatch<TextSegment>> matches = getEmbeddingStore()
                .search(EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(maxResults)
                        .minScore(0.5)
                        .build()) // Minimum similarity threshold
                .matches();

        log.info("Found {} relevant chunks", matches.size());
        return matches;

    }

    public void deleteDocumentChunks(Long documentId) {
        // ChromaDB doesn't have direct delete by metadata in LangChain4j
        // This is a limitation - for now we'll skip deletion
        // In production, you'd use ChromaDB's native client
        log.warn("Delete not implemented yet for document {}", documentId);
    }


}
