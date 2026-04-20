package rag_document.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import rag_document.entity.Document;
import rag_document.service.DocumentService;

import java.io.IOException;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping("/upload")
    public ResponseEntity<Document> uploadDocument(@RequestParam("file")MultipartFile file){

        try{
            Document document = documentService.uploadDocument(file);
            return ResponseEntity.ok(document);
        }
        catch (IOException e ){

            return ResponseEntity.badRequest().build();
        }
    }
}
