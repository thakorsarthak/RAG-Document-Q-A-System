package rag_document.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import rag_document.dto.QueryRequest;
import rag_document.dto.QueryResponse;
import rag_document.dto.SearchResult;
import rag_document.entity.Document;
import rag_document.service.DocumentService;
import rag_document.service.LuceneSearchService;
import rag_document.service.QueryService;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@Tag(name = "Document Management", description = "APIs for document upload, retrieval, and RAG-based querying")
public class DocumentController {

    private final DocumentService documentService;
    private  final QueryService queryService;
    private final LuceneSearchService luceneSearchService;

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

    @PostMapping ("/search/bm25")
    @Operation(summary = "BM25 keyword search" , description = "Search documents using BM25 algorithm (key-word based)")
    public ResponseEntity<List<SearchResult>> searchBM25(@RequestBody QueryRequest request){

       try {
           List<SearchResult> results = luceneSearchService.search(request.getQuestion() , 5);
           return ResponseEntity.ok(results);
       }catch (Exception e)
       {
           log.error("BM25 search failed" , e);
           return ResponseEntity.internalServerError().build();
       }

    }


    @PostMapping("/query")
    @Operation(summary = "Query documents", description = "Ask questions about uploaded documents using RAG")
    public ResponseEntity<QueryResponse> query (@Valid @RequestBody QueryRequest request){

        QueryResponse queryResponse = queryService.query(request);
        return ResponseEntity.ok(queryResponse);

    }


    @PostMapping("/query/hybrid")
    @Operation(
            summary = "Hybrid query (Vector + BM25)",
            description = "Ask questions using combined semantic and keyword search with RRF merging"
    )
    public ResponseEntity<QueryResponse> hybridQuery(
            @Valid @RequestBody QueryRequest request,
            @RequestParam(defaultValue = "0.7") double alpha){

        QueryResponse response = queryService.hybridQuery(request ,alpha);
        return ResponseEntity.ok(response);
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
