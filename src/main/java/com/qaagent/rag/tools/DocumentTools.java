package com.qaagent.rag.tools;

import java.util.List;
import org.springframework.ai.document.Document;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;

/**
 * AI Tools for document search and analysis.
 *
 * <p>These tools enable the AI model to programmatically search and analyze documents in the vector
 * store during conversations. This demonstrates Spring AI 1.1's function calling capabilities.
 *
 * <p>The AI model can invoke these tools when it determines additional document context would help
 * answer a user's question.
 *
 * @author Arvind Menon
 */
@Component
public class DocumentTools {

  private final VectorStore vectorStore;

  public DocumentTools(VectorStore vectorStore) {
    this.vectorStore = vectorStore;
  }

  /**
   * Searches the document knowledge base for relevant information.
   *
   * <p>Use this tool when you need to find specific information from the uploaded documents that
   * isn't already in the conversation context.
   *
   * @param query The search query describing what information to find
   * @param maxResults Maximum number of results to return (default: 5)
   * @return Relevant document excerpts matching the query
   */
  @Tool(
      description =
          "Search the document knowledge base for information. "
              + "Use this when you need to find specific details from uploaded documents.")
  public String searchDocuments(
      @ToolParam(description = "The search query - describe what information you're looking for")
          String query,
      @ToolParam(description = "Maximum number of results to return", required = false)
          Integer maxResults) {

    int limit = maxResults != null ? maxResults : 5;

    List<Document> results =
        vectorStore.similaritySearch(SearchRequest.builder().query(query).topK(limit).build());

    if (results.isEmpty()) {
      return "No relevant documents found for: " + query;
    }

    StringBuilder sb = new StringBuilder("Found " + results.size() + " relevant documents:\n\n");
    for (int i = 0; i < results.size(); i++) {
      Document doc = results.get(i);
      String source = (String) doc.getMetadata().getOrDefault("source", "unknown");
      sb.append("--- Document ").append(i + 1).append(" (").append(source).append(") ---\n");
      sb.append(doc.getText()).append("\n\n");
    }

    return sb.toString();
  }

  /**
   * Lists all unique documents in the knowledge base.
   *
   * @return Summary of available documents
   */
  @Tool(
      description =
          "List all documents available in the knowledge base. "
              + "Use this to see what documents have been uploaded.")
  public String listDocuments() {
    // Search with empty query to get a sample of documents
    List<Document> results =
        vectorStore.similaritySearch(SearchRequest.builder().query("document").topK(100).build());

    if (results.isEmpty()) {
      return "No documents have been uploaded to the knowledge base.";
    }

    // Extract unique sources
    List<String> sources =
        results.stream()
            .map(doc -> (String) doc.getMetadata().getOrDefault("source", "unknown"))
            .distinct()
            .toList();

    StringBuilder sb = new StringBuilder("Documents in knowledge base:\n");
    for (String source : sources) {
      sb.append("- ").append(source).append("\n");
    }

    return sb.toString();
  }

  /**
   * Summarizes a specific document from the knowledge base.
   *
   * @param documentName The name of the document to summarize
   * @return Key points from the document
   */
  @Tool(
      description =
          "Get a summary of key points from a specific document. "
              + "Provide the document filename.")
  public String summarizeDocument(
      @ToolParam(description = "The filename of the document to summarize") String documentName) {

    List<Document> results =
        vectorStore.similaritySearch(
            SearchRequest.builder()
                .query("summary overview key points " + documentName)
                .topK(10)
                .filterExpression("source == '" + documentName + "'")
                .build());

    if (results.isEmpty()) {
      return "Document not found: " + documentName;
    }

    StringBuilder sb = new StringBuilder("Content from " + documentName + ":\n\n");
    for (Document doc : results) {
      sb.append(doc.getText()).append("\n\n");
    }

    return sb.toString();
  }
}
