package com.qaagent.rag.service;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.markdown.MarkdownDocumentReader;
import org.springframework.ai.reader.markdown.config.MarkdownDocumentReaderConfig;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.qaagent.rag.model.DocumentUploadResponse;

/**
 * Service for document ingestion and management.
 *
 * <p>Supports multiple document formats:
 *
 * <ul>
 *   <li>PDF documents
 *   <li>Markdown files
 *   <li>Plain text files
 * </ul>
 *
 * <p>Documents are split into chunks and stored in the vector store for retrieval-augmented
 * generation.
 *
 * @author Arvind Menon
 */
@Service
public class DocumentService {

  private static final Logger LOG = LoggerFactory.getLogger(DocumentService.class);

  private final VectorStore vectorStore;
  private final TokenTextSplitter textSplitter;

  public DocumentService(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
    this.textSplitter = new TokenTextSplitter();
  }

  /**
   * Uploads and processes a document file.
   *
   * @param file The multipart file to upload
   * @return Response containing upload status and details
   */
  public DocumentUploadResponse uploadDocument(MultipartFile file) {
    String filename = file.getOriginalFilename();
    String documentId = UUID.randomUUID().toString();

    try {
      List<Document> documents = parseDocument(file);

      // Add metadata to documents
      documents.forEach(
          doc -> {
            doc.getMetadata().put("document_id", documentId);
            doc.getMetadata().put("filename", filename);
            doc.getMetadata().put("source", filename);
          });

      // Split into chunks and add to vector store
      List<Document> chunks = textSplitter.apply(documents);
      vectorStore.add(chunks);

      LOG.info("Successfully ingested document: {} with {} chunks", filename, chunks.size());
      return DocumentUploadResponse.success(documentId, filename, chunks.size());

    } catch (Exception e) {
      LOG.error("Failed to ingest document: {}", filename, e);
      return DocumentUploadResponse.failure(filename, e.getMessage());
    }
  }

  /**
   * Uploads a document from a classpath resource.
   *
   * @param resource The resource to upload
   * @param filename The filename to use for metadata
   * @return Response containing upload status and details
   */
  public DocumentUploadResponse uploadResource(Resource resource, String filename) {
    String documentId = UUID.randomUUID().toString();

    try {
      List<Document> documents;

      if (filename.endsWith(".pdf")) {
        PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
        documents = reader.get();
      } else if (filename.endsWith(".md")) {
        MarkdownDocumentReader reader =
            new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
        documents = reader.get();
      } else {
        // Plain text
        String content = new String(resource.getInputStream().readAllBytes());
        documents = List.of(new Document(content));
      }

      // Add metadata
      documents.forEach(
          doc -> {
            doc.getMetadata().put("document_id", documentId);
            doc.getMetadata().put("filename", filename);
            doc.getMetadata().put("source", filename);
          });

      // Split and store
      List<Document> chunks = textSplitter.apply(documents);
      vectorStore.add(chunks);

      LOG.info("Successfully ingested resource: {} with {} chunks", filename, chunks.size());
      return DocumentUploadResponse.success(documentId, filename, chunks.size());

    } catch (Exception e) {
      LOG.error("Failed to ingest resource: {}", filename, e);
      return DocumentUploadResponse.failure(filename, e.getMessage());
    }
  }

  /**
   * Deletes all documents with a specific document ID.
   *
   * @param documentId The document ID to delete
   */
  public void deleteDocument(String documentId) {
    vectorStore.delete("document_id == '" + documentId + "'");
    LOG.info("Deleted document: {}", documentId);
  }

  private List<Document> parseDocument(MultipartFile file) throws IOException {
    String filename = file.getOriginalFilename();
    Resource resource = file.getResource();

    if (filename != null && filename.endsWith(".pdf")) {
      PagePdfDocumentReader reader = new PagePdfDocumentReader(resource);
      return reader.get();
    } else if (filename != null && filename.endsWith(".md")) {
      MarkdownDocumentReader reader =
          new MarkdownDocumentReader(resource, MarkdownDocumentReaderConfig.defaultConfig());
      return reader.get();
    } else {
      // Treat as plain text
      String content = new String(file.getBytes());
      return List.of(new Document(content));
    }
  }
}
