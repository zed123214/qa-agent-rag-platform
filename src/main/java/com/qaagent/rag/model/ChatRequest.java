package com.qaagent.rag.model;

import jakarta.validation.constraints.NotBlank;

/**
 * Request model for chat interactions.
 *
 * @param message The user's message/query
 * @param conversationId Optional conversation ID for multi-turn conversations
 * @param useRag Whether to use RAG (retrieval-augmented generation)
 * @author Arvind Menon
 */
public record ChatRequest(
    @NotBlank(message = "Message cannot be blank") String message,
    String conversationId,
    boolean useRag) {
  /** Creates a ChatRequest with default values. */
  public ChatRequest {
    if (conversationId == null || conversationId.isBlank()) {
      conversationId = "default";
    }
  }

  /** Convenience constructor for simple messages. */
  public ChatRequest(String message) {
    this(message, "default", true);
  }
}
