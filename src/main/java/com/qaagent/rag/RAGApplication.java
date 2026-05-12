package com.qaagent.rag;

import com.qaagent.rag.service.DocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

/**
 * Spring AI RAG Demo Application.
 *
 * <p>A comprehensive demonstration of Retrieval-Augmented Generation (RAG) patterns using Spring AI
 * 1.1, featuring:
 *
 * <ul>
 *   <li>Modular RAG pipeline with query transformation
 *   <li>Conversation memory with JDBC persistence
 *   <li>Streaming responses via Server-Sent Events
 *   <li>Tool/function calling for document operations
 *   <li>Flexible document ingestion (PDF, Markdown, Text)
 * </ul>
 *
 * @author Arvind Menon
 * @see <a href="https://docs.spring.io/spring-ai/reference/">Spring AI Documentation</a>
 */
@SpringBootApplication
public class RAGApplication {

  private static final Logger LOG = LoggerFactory.getLogger(RAGApplication.class);

  public static void main(String[] args) {
    SpringApplication.run(RAGApplication.class, args);
  }

  /** Loads sample documents on application startup if vector store is empty. */
  @Bean
  CommandLineRunner initializeSampleDocuments(
      DocumentService documentService, @Value("classpath:/docs/") Resource docsFolder) {
    return args -> {
      LOG.info("=".repeat(60));
      LOG.info("Spring AI RAG Demo - Starting Up");
      LOG.info("=".repeat(60));
      LOG.info("");
      LOG.info("API Endpoints:");
      LOG.info("  GET  /api/health             - Health check");
      LOG.info("  POST /api/v2/chat             - Chat with RAG");
      LOG.info("  GET  /api/v2/chat/stream      - Streaming chat (SSE)");
      LOG.info("  POST /api/v2/documents        - Upload documents");
      LOG.info("  POST /api/testcase/generate   - Generate test cases (demo)");
      LOG.info("  POST /api/testcase/evaluate   - AI Critic evaluation (demo)");
      LOG.info("  POST /api/agent/analyze       - Agent analysis (demo)");
      LOG.info("=".repeat(60));
    };
  }
}
