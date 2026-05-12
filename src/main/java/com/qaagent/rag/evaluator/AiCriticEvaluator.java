package com.qaagent.rag.evaluator;

import com.qaagent.rag.dto.EvaluationReport;
import com.qaagent.rag.dto.EvaluationRequest;
import com.qaagent.rag.dto.TestCaseItem;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo AI Critic evaluator for test case quality assessment.
 * Evaluates on four dimensions: coverage, logic, duplicates, and standards.
 * 
 * This is a demo implementation returning mock evaluation results.
 * Production would use an LLM with structured evaluation prompts.
 */
public class AiCriticEvaluator {

    private static final Logger LOG = LoggerFactory.getLogger(AiCriticEvaluator.class);

    public EvaluationReport evaluate(EvaluationRequest request) {
        LOG.info("Evaluating {} test cases...", request.testCases().size());

        List<String> coverageIssues = checkCoverage(request);
        List<String> logicIssues = checkLogic(request.testCases());
        List<String> duplicateIssues = checkDuplicates(request.testCases());
        List<String> suggestions = generateSuggestions(coverageIssues, logicIssues, duplicateIssues);

        double score = calculateScore(coverageIssues, logicIssues, duplicateIssues);

        String summary = String.format(
            "Score: %.1f/100 | Coverage: %d issues | Logic: %d issues | Duplicates: %d issues",
            score, coverageIssues.size(), logicIssues.size(), duplicateIssues.size()
        );

        return new EvaluationReport(score, coverageIssues, logicIssues, duplicateIssues, suggestions, summary);
    }

    private List<String> checkCoverage(EvaluationRequest request) {
        List<String> issues = new ArrayList<>();
        
        // Check if normal flow is covered
        boolean hasNormalFlow = request.testCases().stream()
            .anyMatch(tc -> tc.tags() != null && tc.tags().contains("normal"));
        if (!hasNormalFlow) {
            issues.add("Missing normal flow test case");
        }

        // Check if exception flow is covered
        boolean hasException = request.testCases().stream()
            .anyMatch(tc -> tc.tags() != null && tc.tags().contains("exception"));
        if (!hasException) {
            issues.add("Missing exception/error flow test case");
        }

        // Check if boundary test is covered
        boolean hasBoundary = request.testCases().stream()
            .anyMatch(tc -> tc.tags() != null && tc.tags().contains("boundary"));
        if (!hasBoundary) {
            issues.add("Missing boundary value test case");
        }

        return issues;
    }

    private List<String> checkLogic(List<TestCaseItem> cases) {
        List<String> issues = new ArrayList<>();
        
        for (TestCaseItem tc : cases) {
            if (tc.steps() == null || tc.steps().isEmpty()) {
                issues.add("Case [" + tc.caseId() + "] has no test steps");
            }
            if (tc.expected() == null || tc.expected().isBlank()) {
                issues.add("Case [" + tc.caseId() + "] has no expected result");
            }
        }
        
        return issues;
    }

    private List<String> checkDuplicates(List<TestCaseItem> cases) {
        List<String> issues = new ArrayList<>();
        List<String> titles = cases.stream().map(TestCaseItem::title).toList();
        
        for (int i = 0; i < titles.size(); i++) {
            for (int j = i + 1; j < titles.size(); j++) {
                if (titles.get(i).equals(titles.get(j))) {
                    issues.add("Duplicate: case [" + cases.get(i).caseId() + "] and [" + cases.get(j).caseId() + "]");
                }
            }
        }
        
        return issues;
    }

    private List<String> generateSuggestions(List<String> coverage, List<String> logic, List<String> duplicates) {
        List<String> suggestions = new ArrayList<>();
        
        if (!coverage.isEmpty()) {
            suggestions.add("Add missing test scenarios: " + String.join(", ", coverage));
        }
        if (!logic.isEmpty()) {
            suggestions.add("Review test case completeness: " + logic.size() + " logic issues found");
        }
        if (!duplicates.isEmpty()) {
            suggestions.add("Remove duplicate test cases: " + duplicates.size() + " duplicates found");
        }
        if (suggestions.isEmpty()) {
            suggestions.add("Test cases look comprehensive. Consider adding performance and security tests.");
        }
        
        return suggestions;
    }

    private double calculateScore(List<String> coverage, List<String> logic, List<String> duplicates) {
        int penalty = coverage.size() * 15 + logic.size() * 5 + duplicates.size() * 10;
        return Math.max(0, 100 - penalty);
    }
}
