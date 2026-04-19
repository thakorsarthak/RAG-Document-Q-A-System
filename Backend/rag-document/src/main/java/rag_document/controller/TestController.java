package rag_document.controller;


import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class TestController {

    private final EmbeddingModel embeddingModel;

    @GetMapping("/test/embed")
    public float[] testEmbeddingModel(@RequestParam String text){

        Embedding embedding = embeddingModel.embed(text).content();
        return  embedding.vector();
    }

    @GetMapping("/test/health")
    public String health() {
        return "RAG API is running";
    }

}
