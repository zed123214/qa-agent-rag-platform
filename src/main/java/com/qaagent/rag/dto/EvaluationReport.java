package com.qaagent.rag.dto;

import java.util.List;

/**
 * AI Critic evaluation report.
 */
public record EvaluationReport(
    double score,
    List<String> coverageIssues,
    List<String> logicIssues,
    List<String> duplicateIssues,
    List<String> suggestions,
    String summary
) {}
