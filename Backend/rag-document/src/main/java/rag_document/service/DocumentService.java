package rag_document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rag_document.entity.Document;
import rag_document.repository.DocumentRepository;

import javax.print.Doc;
import java.io.IOException;
import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final TextExtractionService textExtractionService;
    private final ChunkingService chunkingService;

    public Document uploadDocument(MultipartFile file) throws IOException{

        log.info("Processing file: {}", file.getOriginalFilename());

        String extractedText = textExtractionService.extractText(file);
        log.info("Extracted {} texts" , extractedText.length());

        List<String> chunks = chunkingService.chunkText(extractedText);
        log.info("Created {} chunks " , chunks.size());

        //save document metadata
        Document document = Document.builder()
                .filename(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .totalChunks(chunks.size())
                .status("PROCESSING")
                .build();

        document = documentRepository.save(document);
        log.info("Document saved with ID : {}" , document.getId());

        // TODO: Store chunks in ChromaDB (next step)

        document.setStatus("COMPLETED");
        return documentRepository.save(document);

    }

}
