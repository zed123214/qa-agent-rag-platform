package com.qaagent.rag.config;

import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for chat memory with JDBC persistence.
 *
 * <p>This demonstrates Spring AI 1.1's chat memory capabilities:
 *
 * <ul>
 *   <li>JDBC-backed persistence for conversation history
 *   <li>Configurable message window size
 *   <li>Automatic conversation management
 * </ul>
 *
 * @author Arvind Menon
 */
@Configuration
public class MemoryConfig {

  /**
   * Maximum number of messages to retain in the conversation window. Older messages are
   * automatically pruned.
   */
  private static final int MAX_MESSAGES = 20;

  /**
   * Creates a ChatMemory with message windowing backed by JDBC repository.
   *
   * <p>The memory automatically:
   *
   * <ul>
   *   <li>Persists conversations to PostgreSQL
   *   <li>Maintains a sliding window of recent messages
   *   <li>Supports multiple concurrent conversations via conversation IDs
   * </ul>
   */
  @Bean
  ChatMemory chatMemory(ChatMemoryRepository chatMemoryRepository) {
    return MessageWindowChatMemory.builder()
        .chatMemoryRepository(chatMemoryRepository)
        .maxMessages(MAX_MESSAGES)
        .build();
  }
}
