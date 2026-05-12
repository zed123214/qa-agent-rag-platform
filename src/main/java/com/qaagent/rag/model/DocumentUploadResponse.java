package com.qaagent.rag.model;

import java.time.Instant;

/**
 * Response model for document upload operations.
 *
 * @param documentId Unique identifier for the uploaded document
 * @param filename Original filename
 * @param chunksCreated Number of text chunks created from the document
 * @param status Upload status (SUCCESS, FAILED)
 * @param message Additional status message
 * @param timestamp Upload timestamp
 * @author Arvind Menon
 */
public record DocumentUploadResponse(
    String documentId,
    String filename,
    int chunksCreated,
    String status,
    String message,
    Instant timestamp) {
  public static DocumentUploadResponse success(String documentId, String filename, int chunks) {
    return new DocumentUploadResponse(
        documentId,
        filename,
        chunks,
        "SUCCESS",
        "Document successfully processed and indexed",
        Instant.now());
  }

  public static DocumentUploadResponse failure(String filename, String errorMessage) {
    return new DocumentUploadResponse(null, filename, 0, "FAILED", errorMessage, Instant.now());
  }
}
