package com.qaagent.rag.controller;

import com.qaagent.rag.model.ChatRequest;
import com.qaagent.rag.model.ChatResponse;
import jakarta.validation.Valid;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * REST controller for chat interactions with advanced RAG capabilities.
 *
 * <p>Features:
 *
 * <ul>
 *   <li>Synchronous chat with RAG
 *   <li>Streaming responses via SSE
 *   <li>Conversation memory support
 *   <li>Simple chat without RAG
 * </ul>
 *
 * @author Arvind Menon
 */
@RestController
@RequestMapping("/api/v2/chat")
public class ChatController {

  private final ChatClient chatClient;
  private final ChatClient simpleChatClient;

  public ChatController(
      @Qualifier("chatClient") ChatClient chatClient,
      @Qualifier("simpleChatClient") ChatClient simpleChatClient) {
    this.chatClient = chatClient;
    this.simpleChatClient = simpleChatClient;
  }

  /**
   * Handles chat requests with RAG-augmented responses.
   *
   * @param request The chat request containing message and conversation context
   * @return ChatResponse with AI-generated content
   */
  @PostMapping
  public ChatResponse chat(@Valid @RequestBody ChatRequest request) {
    ChatClient client = request.useRag() ? chatClient : simpleChatClient;

    String response =
        client
            .prompt()
            .user(request.message())
            .advisors(
                advisor -> advisor.param(ChatMemory.CONVERSATION_ID, request.conversationId()))
            .call()
            .content();

    return ChatResponse.of(response, request.conversationId());
  }

  /**
   * Handles simple GET-based chat requests (convenience endpoint).
   *
   * @param message The user's message
   * @param conversationId Optional conversation ID (defaults to "default")
   * @return ChatResponse with AI-generated content
   */
  @GetMapping
  public ChatResponse chatGet(
      @RequestParam String message, @RequestParam(defaultValue = "default") String conversationId) {
    return chat(new ChatRequest(message, conversationId, true));
  }

  /**
   * Streaming chat endpoint with Server-Sent Events.
   *
   * <p>Returns a stream of response tokens as they're generated, enabling real-time display in chat
   * interfaces.
   *
   * @param message The user's message
   * @param conversationId Optional conversation ID
   * @return Flux of response content chunks
   */
  @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> streamChat(
      @RequestParam String message, @RequestParam(defaultValue = "default") String conversationId) {
    return chatClient
        .prompt()
        .user(message)
        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
        .stream()
        .content();
  }

  /**
   * Simple chat endpoint without RAG (for comparison/testing).
   *
   * @param message The user's message
   * @param conversationId Optional conversation ID
   * @return ChatResponse with AI-generated content
   */
  @GetMapping("/simple")
  public ChatResponse simpleChat(
      @RequestParam String message, @RequestParam(defaultValue = "default") String conversationId) {
    String response =
        simpleChatClient
            .prompt()
            .user(message)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .content();

    return ChatResponse.of(response, conversationId);
  }
}
