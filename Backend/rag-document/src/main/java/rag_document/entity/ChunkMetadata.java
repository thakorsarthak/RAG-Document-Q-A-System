package rag_document.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChunkMetadata {

    private Long documentId;
    private String filename;
    private Integer chunkIndex;
    private Integer totalChunks;
}
