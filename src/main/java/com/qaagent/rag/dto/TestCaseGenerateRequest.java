package com.qaagent.rag.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

/**
 * Request for test case generation.
 * Accepts PRD text, API doc references, and optional user instructions.
 */
public record TestCaseGenerateRequest(
    @NotBlank String prdText,
    List<String> ragContext,
    String userInstruction,
    String format
) {
    public TestCaseGenerateRequest {
        if (format == null || format.isBlank()) {
            format = "json";
        }
    }
}
