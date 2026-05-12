package com.qaagent.rag.dto;

import java.util.List;

/**
 * Response from agent analysis.
 */
public record AgentAnalysisResponse(
    String summary,
    List<String> possibleCauses,
    List<String> reproductionSteps,
    List<String> testSuggestions,
    List<String> toolResults,
    List<String> knowledgeRefs
) {}
