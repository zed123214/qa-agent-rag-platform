package com.qaagent.rag.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo RAG filter service implementing a two-stage funnel:
 * Stage 1: Vector TopK coarse retrieval (delegated to existing VectorStore)
 * Stage 2: Rule-based or mock LLM fine filtering to remove low-relevance chunks
 *
 * This is a demo implementation. Production would use an LLM for the second stage.
 */
public class RagFilterService {

    private static final Logger LOG = LoggerFactory.getLogger(RagFilterService.class);

    /**
     * Simulates the second-stage filtering.
     * Removes chunks that appear irrelevant (e.g., table of contents, copyright notices).
     */
    public List<String> filter(List<String> rawChunks, String query) {
        LOG.info("Filtering {} chunks for query relevance...", rawChunks.size());

        List<String> filtered = new ArrayList<>();
        List<String> noisePatterns = List.of(
            "table of contents", "copyright", "all rights reserved",
            "confidential", "page", "chapter"
        );

        for (String chunk : rawChunks) {
            String lower = chunk.toLowerCase();
            boolean isNoise = noisePatterns.stream().anyMatch(lower::contains);
            if (!isNoise && lower.length() > 20) {
                filtered.add(chunk);
            } else {
                LOG.debug("Filtered out noise chunk: {}...", chunk.substring(0, Math.min(50, chunk.length())));
            }
        }

        LOG.info("Filtered: {} -> {} relevant chunks", rawChunks.size(), filtered.size());
        return filtered;
    }
}
