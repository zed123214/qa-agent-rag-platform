package com.qaagent.rag.model;

import java.time.Instant;
import java.util.List;

/**
 * Response model for chat interactions.
 *
 * @param content The AI-generated response content
 * @param conversationId The conversation ID for tracking
 * @param sources List of source documents used (if RAG was enabled)
 * @param timestamp Response generation timestamp
 * @author Arvind Menon
 */
public record ChatResponse(
    String content, String conversationId, List<String> sources, Instant timestamp) {
  /** Creates a simple response without sources. */
  public static ChatResponse of(String content, String conversationId) {
    return new ChatResponse(content, conversationId, List.of(), Instant.now());
  }

  /** Creates a response with source documents. */
  public static ChatResponse withSources(
      String content, String conversationId, List<String> sources) {
    return new ChatResponse(content, conversationId, sources, Instant.now());
  }
}
