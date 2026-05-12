package com.qaagent.rag.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request for agent-based analysis.
 */
public record AgentAnalysisRequest(
    @NotBlank String task,
    String query,
    List<String> tools,
    String logSnippet
) {}
