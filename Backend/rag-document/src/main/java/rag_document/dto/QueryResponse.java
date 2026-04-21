package rag_document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QueryResponse {

    private String answer;
    private List<SourceChunk> sources;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class SourceChunk{

        private Long documentId;
        private String filename;
        private String text;
        private Double score;
    }

}
