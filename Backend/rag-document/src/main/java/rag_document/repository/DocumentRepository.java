package rag_document.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rag_document.entity.Document;


@Repository
public interface DocumentRepository extends JpaRepository<Document,Long> {
}
