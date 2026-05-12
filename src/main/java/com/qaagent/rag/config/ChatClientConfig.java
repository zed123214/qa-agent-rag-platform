package com.qaagent.rag.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.ai.rag.preretrieval.query.transformation.RewriteQueryTransformer;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for ChatClient with advanced RAG patterns.
 *
 * <p>This configuration showcases Spring AI 1.1's modular RAG architecture with:
 *
 * <ul>
 *   <li>Query rewriting for improved retrieval
 *   <li>Conversation memory with JDBC persistence
 *   <li>Vector store retrieval with similarity thresholds
 *   <li>Logging advisor for observability
 * </ul>
 *
 * @author Arvind Menon
 */
@Configuration
public class ChatClientConfig {

  private static final String DEFAULT_SYSTEM_PROMPT =
      """
      You are a helpful AI assistant with access to a knowledge base of documents.
      When answering questions:
      1. Use information from the provided context when available
      2. Be precise and cite specific details from documents
      3. If the context doesn't contain relevant information, say so clearly
      4. Provide structured, easy-to-read responses
      """;

  /**
   * Creates a ChatClient with modular RAG pipeline and conversation memory.
   *
   * <p>The RAG pipeline uses:
   *
   * <ul>
   *   <li>{@link RewriteQueryTransformer} - Rewrites user queries for better retrieval
   *   <li>{@link VectorStoreDocumentRetriever} - Retrieves relevant documents
   *   <li>{@link MessageChatMemoryAdvisor} - Maintains conversation history
   * </ul>
   */
  @Bean
  ChatClient chatClient(
      ChatClient.Builder chatClientBuilder, VectorStore vectorStore, ChatMemory chatMemory) {

    // Build the modular RAG advisor with query transformation
    var ragAdvisor =
        RetrievalAugmentationAdvisor.builder()
            .queryTransformers(
                RewriteQueryTransformer.builder()
                    .chatClientBuilder(chatClientBuilder.build().mutate())
                    .build())
            .documentRetriever(
                VectorStoreDocumentRetriever.builder()
                    .vectorStore(vectorStore)
                    .similarityThreshold(0.5)
                    .topK(5)
                    .build())
            .build();

    return chatClientBuilder
        .defaultSystem(DEFAULT_SYSTEM_PROMPT)
        .defaultAdvisors(
            // Chat memory for conversation continuity
            MessageChatMemoryAdvisor.builder(chatMemory).build(),
            // RAG for document-grounded responses
            ragAdvisor,
            // Logging for observability
            new SimpleLoggerAdvisor())
        .build();
  }

  /** Creates a basic ChatClient without RAG for simple chat operations. */
  @Bean
  ChatClient simpleChatClient(ChatClient.Builder chatClientBuilder, ChatMemory chatMemory) {
    return chatClientBuilder
        .defaultSystem("You are a helpful AI assistant.")
        .defaultAdvisors(
            MessageChatMemoryAdvisor.builder(chatMemory).build(), new SimpleLoggerAdvisor())
        .build();
  }
}
