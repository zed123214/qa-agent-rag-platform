package com.qaagent.rag.controller;

import com.qaagent.rag.model.DocumentUploadResponse;
import com.qaagent.rag.service.DocumentService;
import java.util.List;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * REST controller for document management operations.
 *
 * <p>Provides endpoints for:
 *
 * <ul>
 *   <li>Uploading documents (PDF, Markdown, Text)
 *   <li>Batch document uploads
 *   <li>Deleting documents from the vector store
 * </ul>
 *
 * @author Arvind Menon
 */
@RestController
@RequestMapping("/api/v2/documents")
public class DocumentController {

  private final DocumentService documentService;

  public DocumentController(DocumentService documentService) {
    this.documentService = documentService;
  }

  /**
   * Uploads a single document for RAG processing.
   *
   * <p>Supported formats:
   *
   * <ul>
   *   <li>.pdf - PDF documents
   *   <li>.md - Markdown files
   *   <li>.txt - Plain text files
   * </ul>
   *
   * @param file The document file to upload
   * @return DocumentUploadResponse with status and document ID
   */
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public DocumentUploadResponse uploadDocument(@RequestParam("file") MultipartFile file) {
    return documentService.uploadDocument(file);
  }

  /**
   * Uploads multiple documents in a single request.
   *
   * @param files Array of document files to upload
   * @return List of DocumentUploadResponse for each file
   */
  @PostMapping(value = "/batch", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public List<DocumentUploadResponse> uploadDocuments(
      @RequestParam("files") MultipartFile[] files) {
    return java.util.Arrays.stream(files).map(documentService::uploadDocument).toList();
  }

  /**
   * Deletes a document and all its chunks from the vector store.
   *
   * @param documentId The document ID to delete
   */
  @DeleteMapping("/{documentId}")
  public void deleteDocument(@PathVariable String documentId) {
    documentService.deleteDocument(documentId);
  }
}
