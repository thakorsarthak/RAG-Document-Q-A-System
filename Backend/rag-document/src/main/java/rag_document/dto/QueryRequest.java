package rag_document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequest {

    @NotBlank(message = "Question is Required")
    private String Question;

    private Long DocumentId; // Optional: filter by specific document (not used yet)

    private Double alpha; // Optional: weight for vector vs BM25 (default 0.7)

}
