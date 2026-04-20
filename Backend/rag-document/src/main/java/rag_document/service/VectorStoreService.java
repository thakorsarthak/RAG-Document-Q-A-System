package rag_document.service;

import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorStoreService {

    private final EmbeddingModel embeddingModel;

    @Value("${chroma.base.url}")
    private String chromaBaseUrl;

    


}
