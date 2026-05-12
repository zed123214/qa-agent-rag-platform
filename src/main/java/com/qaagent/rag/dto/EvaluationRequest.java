package com.qaagent.rag.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request for AI Critic evaluation.
 */
public record EvaluationRequest(
    @NotBlank String prdText,
    List<String> ragContext,
    @NotBlank List<TestCaseItem> testCases,
    List<TestCaseItem> goldenCases
) {}
