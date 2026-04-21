package rag_document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class QueryRequest {

    @NotBlank(message = "Question is Required")
    private String Question;

    private Long DocumentId;

}
