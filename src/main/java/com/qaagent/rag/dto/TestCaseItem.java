package com.qaagent.rag.dto;

import java.util.List;

/**
 * Structured test case item.
 */
public record TestCaseItem(
    String caseId,
    String title,
    String precondition,
    List<String> steps,
    String expected,
    String priority,
    List<String> tags
) {}
