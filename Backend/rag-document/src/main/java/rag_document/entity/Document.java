package rag_document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false)
    private String fileType; // PDF, TXT

    @Column(nullable = false)
    private Long fileSize; // bytes

    @Column(nullable = false)
    private Integer totalChunks;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(nullable = false)
    private String status; // PROCESSING, COMPLETED, FAILED
}
