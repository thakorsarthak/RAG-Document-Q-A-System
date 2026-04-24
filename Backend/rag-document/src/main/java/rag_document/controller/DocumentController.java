package rag_document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rag_document.dto.QueryRequest;
import rag_document.dto.QueryResponse;
import rag_document.entity.Document;
import rag_document.service.DocumentService;
import rag_document.service.QueryService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for document upload, retrieval, and RAG-based querying")
public class DocumentController {

    private final DocumentService documentService;
    private  final QueryService queryService;

   // @PostMapping("/upload")
   @PostMapping(value = "/upload",consumes = MediaType.MULTIPART_FORM_DATA_VALUE )
    @Operation(summary = "Upload a document", description = "Upload PDF or TXT file for processing and embedding generation")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file")MultipartFile file){

        try{
            Document document = documentService.uploadDocument(file);
            return ResponseEntity.ok(document);
        }
        catch (IllegalArgumentException e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        catch (IOException e ){

            return ResponseEntity.badRequest().build();
        }
    }


    @PostMapping("/query")
    @Operation(summary = "Query documents", description = "Ask questions about uploaded documents using RAG")
    public ResponseEntity<QueryResponse> query (@Valid @RequestBody QueryRequest request){

        QueryResponse queryResponse = queryService.query(request);
        return ResponseEntity.ok(queryResponse);

    }

    // get all documents
    @GetMapping
    @Operation(summary = "Get all documents", description = "Retrieve list of all uploaded documents")
    public ResponseEntity<List<Document>> getAllDocuments(){

        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    // get documents with id
    @GetMapping("/{id}")
    @Operation(summary = "Get document by ID", description = "Retrieve a specific document's metadata")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id){
        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);
    }

    // delete document form both chromabd and mysql
    @DeleteMapping("{id}")
    @Operation(summary = "Delete document", description = "Delete document from database (note: embeddings remain in ChromaDB)")
    public ResponseEntity<Document> deleteDocument(@PathVariable Long id){

        Document document = documentService.getDocumentById(id);
        return ResponseEntity.ok(document);

    }

}
