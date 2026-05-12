package com.qaagent.rag.controller;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.advisor.RetrievalAugmentationAdvisor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * OpenAI-compatible API controller for integration with chat UIs like Open WebUI.
 *
 * <p>Implements the OpenAI Chat Completions API specification to enable seamless integration with
 * existing chat interfaces and tools.
 *
 * <p>Exposes two models:
 *
 * <ul>
 *   <li><b>spring-ai-chat</b> - General conversation without RAG
 *   <li><b>spring-ai-rag</b> - Document-grounded responses using uploaded documents
 * </ul>
 *
 * @author Arvind Menon
 */
@RestController
@RequestMapping("/v1")
public class OpenAICompatibleController {

  private static final String MODEL_RAG = "spring-ai-rag";
  private static final String MODEL_CHAT = "spring-ai-chat";

  private final ChatClient ragChatClient;
  private final ChatClient simpleChatClient;

  public OpenAICompatibleController(
      @Qualifier("chatClient") ChatClient ragChatClient,
      @Qualifier("simpleChatClient") ChatClient simpleChatClient) {
    this.ragChatClient = ragChatClient;
    this.simpleChatClient = simpleChatClient;
  }

  /**
   * OpenAI-compatible chat completions endpoint.
   *
   * <p>Accepts requests in the OpenAI format and returns responses compatible with OpenAI's API,
   * enabling use with Open WebUI and similar tools.
   *
   * <p>Model selection:
   *
   * <ul>
   *   <li><b>spring-ai-rag</b> - Uses RAG to answer from uploaded documents (includes sources)
   *   <li><b>spring-ai-chat</b> (default) - General conversation with memory
   * </ul>
   */
  @PostMapping("/chat/completions")
  public ChatCompletionResponse chatCompletions(
      @RequestBody ChatCompletionRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    String conversationId = deriveConversationId(authHeader);
    String userMessage = extractLastUserMessage(request.messages());
    ChatClient client = selectClient(request.model());

    // Use chatClientResponse() to access document context for RAG
    ChatClientResponse clientResponse =
        client
            .prompt()
            .user(userMessage)
            .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
            .call()
            .chatClientResponse();

    String content = extractContent(clientResponse);

    // For RAG model, append sources if available
    if (MODEL_RAG.equalsIgnoreCase(request.model())) {
      String sources = extractSources(clientResponse);
      if (!sources.isEmpty()) {
        content = content + "\n\n---\n**Sources:** " + sources;
      }
    }

    return ChatCompletionResponse.of(request.model(), content);
  }

  /** Safely extracts content from ChatClientResponse. */
  private String extractContent(ChatClientResponse response) {
    var chatResponse = response.chatResponse();
    if (chatResponse == null) {
      return "";
    }
    var result = chatResponse.getResult();
    if (result == null) {
      return "";
    }
    var output = result.getOutput();
    if (output == null) {
      return "";
    }
    String text = output.getText();
    return text != null ? text : "";
  }

  /** Extracts source document names from the RAG context. */
  private String extractSources(ChatClientResponse response) {
    Object docsObj = response.context().get(RetrievalAugmentationAdvisor.DOCUMENT_CONTEXT);
    if (!(docsObj instanceof List<?> docsList) || docsList.isEmpty()) {
      return "";
    }
    Set<String> sourceNames = new TreeSet<>();
    for (Object doc : docsList) {
      if (doc instanceof Document document) {
        addSourceName(document, sourceNames);
      }
    }
    return String.join(", ", sourceNames);
  }

  /** Extracts and adds source name from document metadata. */
  private void addSourceName(Document document, Set<String> sourceNames) {
    Object source = document.getMetadata().get("source");
    if (source == null) {
      return;
    }
    String sourceName = source.toString();
    int lastSlash = sourceName.lastIndexOf('/');
    if (lastSlash >= 0) {
      sourceName = sourceName.substring(lastSlash + 1);
    }
    sourceNames.add(sourceName);
  }

  /** Streaming chat completions endpoint. */
  @PostMapping(
      value = "/chat/completions",
      params = "stream=true",
      produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public Flux<String> streamChatCompletions(
      @RequestBody ChatCompletionRequest request,
      @RequestHeader(value = "Authorization", required = false) String authHeader) {

    String conversationId = deriveConversationId(authHeader);
    String userMessage = extractLastUserMessage(request.messages());
    ChatClient client = selectClient(request.model());

    return client
        .prompt()
        .user(userMessage)
        .advisors(advisor -> advisor.param(ChatMemory.CONVERSATION_ID, conversationId))
        .stream()
        .content()
        .map(chunk -> formatStreamChunk(request.model(), chunk));
  }

  /**
   * Selects the appropriate ChatClient based on model name. Uses RAG client for "spring-ai-rag",
   * simple client for everything else.
   */
  private ChatClient selectClient(String model) {
    if (MODEL_RAG.equalsIgnoreCase(model)) {
      return ragChatClient;
    }
    return simpleChatClient;
  }

  /**
   * Derives a stable conversation ID from the authorization header. This allows Open WebUI sessions
   * to maintain conversation continuity.
   */
  private String deriveConversationId(String authHeader) {
    if (authHeader != null && !authHeader.isBlank()) {
      return "session-" + Integer.toHexString(authHeader.hashCode());
    }
    return "default-session";
  }

  /** Lists available models (required by Open WebUI). */
  @GetMapping("/models")
  public ModelsResponse listModels() {
    long now = System.currentTimeMillis() / 1000;
    return new ModelsResponse(
        "list",
        List.of(
            new Model(MODEL_CHAT, "model", now, "spring-ai"),
            new Model(MODEL_RAG, "model", now, "spring-ai")));
  }

  private String extractLastUserMessage(List<Message> messages) {
    if (messages == null || messages.isEmpty()) {
      return "";
    }
    // Find the last user message
    for (int i = messages.size() - 1; i >= 0; i--) {
      if ("user".equals(messages.get(i).role())) {
        return messages.get(i).content();
      }
    }
    return messages.get(messages.size() - 1).content();
  }

  private String formatStreamChunk(String model, String content) {
    return """
    data: {"id":"chatcmpl-%s","object":"chat.completion.chunk","model":"%s","choices":[{"delta":{"content":"%s"},"index":0}]}

    """
        .formatted(UUID.randomUUID().toString().substring(0, 8), model, escapeJson(content));
  }

  private String escapeJson(String text) {
    return text.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  // Request/Response DTOs following OpenAI API spec

  public record ChatCompletionRequest(
      String model,
      List<Message> messages,
      Double temperature,
      Integer max_tokens,
      Boolean stream) {}

  public record Message(String role, String content) {}

  public record ChatCompletionResponse(
      String id, String object, long created, String model, List<Choice> choices, Usage usage) {
    public static ChatCompletionResponse of(String model, String content) {
      return new ChatCompletionResponse(
          "chatcmpl-" + UUID.randomUUID().toString().substring(0, 8),
          "chat.completion",
          System.currentTimeMillis() / 1000,
          model != null ? model : MODEL_CHAT,
          List.of(new Choice(0, new ResponseMessage("assistant", content), "stop")),
          new Usage(0, 0, 0));
    }
  }

  public record Choice(int index, ResponseMessage message, String finish_reason) {}

  public record ResponseMessage(String role, String content) {}

  public record Usage(int prompt_tokens, int completion_tokens, int total_tokens) {}

  public record ModelsResponse(String object, List<Model> data) {
    public ModelsResponse {
      object = object != null ? object : "list";
      data = data != null ? data : List.of();
    }
  }

  public record Model(String id, String object, long created, String owned_by) {}
}
