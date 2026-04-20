package rag_document.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Service
@Slf4j
public class TextExtractionService {

    public String extractText(MultipartFile file) throws IOException{

        String contentType = file.getContentType();
        if("application/pdf".equals(contentType)){
            return extractFromPdf(file);
        } else if ("text/plan".equals(contentType)) {
            return new String(file.getBytes());
        }else {
            throw new IllegalArgumentException("Unsupported file type: " + contentType);
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
