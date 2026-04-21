package rag_document.config;


import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(){
        return new OpenAPI()
                .info(new Info()
                        .title("RAG Document Q&A API")
                        .version("1.0")
                        .description("REST API for document ingestion and RAG-based question answering using Ollama LLMs and ChromaDB vector store")
                        .contact(new Contact()
                                .name("Sarthak Thakor")
                                .url("https://github.com/thakorsarthak")));

    }

}
