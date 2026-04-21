package rag_document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import rag_document.exception.UnsupportedFileTypeException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;


@Service
@Slf4j
public class TextExtractionService {

    public String extractText(MultipartFile file) throws IOException{

        String contentType = file.getContentType();
        if("application/pdf".equals(contentType)){
            return extractFromPdf(file);
        } else if ("text/plain".equals(contentType)) {
            return new String(file.getBytes(), StandardCharsets.UTF_8);
        }else {
            throw new UnsupportedFileTypeException(contentType);
        }
    }

    public String extractFromPdf(MultipartFile file) throws IOException{

        try(PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);
            log.info("Extracted {} characters from PDF", text.length());
            return text;
        }

    }

}
