package rag_document.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int MAX_CHUNK_SIZE = 800; //CHARACTERS
    private static final int OVERLAP_SIZE = 100;

    public List<String> chunkText(String text){
        List<String> chunks = new ArrayList<>();

        text = text.replaceAll("\\s+"," ").trim();

        if(text.length() <= MAX_CHUNK_SIZE){
            chunks.add(text);
            return chunks;
        }

        int start =0 ;
        while(start < text.length()){
            int end = Math.min(start + MAX_CHUNK_SIZE, text.length());

            // try to break at sentence boundary like . ! ?
            if (end < text.length()){
                int lastPeriod = text.lastIndexOf('.' , end);
                int lastQuestion = text.lastIndexOf('?' , end);
                int lastExclamation = text.lastIndexOf('!' , end);

                int breakPoint = Math.max(lastPeriod , Math.max(lastExclamation , lastExclamation));

                if (breakPoint > start + 200){ // Don't make chunks too small
                    end = breakPoint + 1;
                }
            }

            chunks.add(text.substring(start,end).trim());
            start = end- OVERLAP_SIZE; // overlap for context

        }

        return chunks;

    }


}
