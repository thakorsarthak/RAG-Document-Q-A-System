package rag_document.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    private Long documentId;
    private String filename;
    private Integer chunkIndex;
    private String text;
    private Double score;
}

