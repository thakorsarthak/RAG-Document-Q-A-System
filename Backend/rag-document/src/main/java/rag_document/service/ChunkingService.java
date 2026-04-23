package rag_document.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int MAX_CHUNK_SIZE = 1000;
    private static final int OVERLAP_SIZE = 200;
    private static final int MIN_CHUNK_SIZE = 500; // safety

    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();

        // Clean text
        text = text.replaceAll("\\s+", " ").trim();

        int textLength = text.length();
        int position = 0;

        while (position < textLength) {

            int end = Math.min(position + MAX_CHUNK_SIZE, textLength);

            // Try to adjust to sentence boundary
            if (end < textLength) {
                int adjustedEnd = findSentenceEnd(text, position, end);

                // Only adjust if chunk is not too small
                if (adjustedEnd != -1 && (adjustedEnd - position) >= MIN_CHUNK_SIZE) {
                    end = adjustedEnd;
                }
            }

            String chunk = text.substring(position, end).trim();

            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }

            //  Move forward safely
            int nextPosition = end - OVERLAP_SIZE;

            // Ensure progress (no infinite loop)
            if (nextPosition <= position) {
                nextPosition = end;
            }

            position = nextPosition;
        }

        return chunks;
    }

    private int findSentenceEnd(String text, int start, int end) {

        int searchStart = Math.max(start + MIN_CHUNK_SIZE, end - 200);

        for (int i = end - 1; i >= searchStart; i--) {
            char c = text.charAt(i);
            if (c == '.' || c == '?' || c == '!') {
                return i + 1;
            }
        }

        return -1;
    }
}