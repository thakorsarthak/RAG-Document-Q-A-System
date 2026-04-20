package rag_document.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int MAX_CHUNK_SIZE = 1000; //CHARACTERS
    private static final int OVERLAP_SIZE = 200;

    public List<String> chunkText(String text){
        List<String> chunks = new ArrayList<>();

        text = text.replaceAll("\\s+"," ").trim();

        if(text.length() <= MAX_CHUNK_SIZE){
            chunks.add(text);
            return chunks;
        }

        int position = 0;
        int textLength = text.length();
        while(position < text.length()){
            int end = Math.min(position + MAX_CHUNK_SIZE, textLength);

            // try to break at sentence boundary like . ! ?
            if (end < textLength){
                int searchStart = Math.max(position + 500, end - 200); // Search last 200 chars
                int lastBreak = -1;

                // Look for sentence breaks in a limited range
                for (int i = end; i >= searchStart; i--) {
                    char c = text.charAt(i);
                    if (c == '.' || c == '?' || c == '!') {
                        lastBreak = i + 1;
                        break;
                    }
                }

                if (lastBreak > position){ // Don't make chunks too small
                    end = lastBreak;
                }
            }

            // Extract chunk directly without creating intermediate strings
            String chunk = text.substring(position, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }


            // Move forward with overlap
            position = end - OVERLAP_SIZE;


            // Ensure we make progress
            if (position <= end - MAX_CHUNK_SIZE) {
                position = end;
            }

        }

        return chunks;

    }


}
