package rag_document.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
public class DocumentController {

    private final DocumentService documentService;
    private  final QueryService queryService;


    // get all documents
    @GetMapping
    public ResponseEntity<List<Document>> getAllDocuments(){

        List<Document> documents = documentService.getAllDocuments();
        return ResponseEntity.ok(documents);
    }

    // get documents with id
    @GetMapping("/{id}")
    public ResponseEntity<Document> getDocumentById(@PathVariable Long id){

        return documentService.getDocumentById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/upload")
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
    public ResponseEntity<QueryResponse> query (@Valid @RequestBody QueryRequest request){

        QueryResponse queryResponse = queryService.query(request);
        return ResponseEntity.ok(queryResponse);

    }
}
