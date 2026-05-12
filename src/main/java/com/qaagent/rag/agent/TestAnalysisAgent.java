package com.qaagent.rag.agent;

import com.qaagent.rag.common.ToolRegistry;
import com.qaagent.rag.dto.AgentAnalysisRequest;
import com.qaagent.rag.dto.AgentAnalysisResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo test analysis agent that orchestrates knowledge retrieval,
 * defect recall, log search, and AI evaluation.
 * 
 * Demonstrates the agent tool orchestration pattern.
 * In production, this would use a ReAct or LangGraph-based loop.
 */
public class TestAnalysisAgent {

    private static final Logger LOG = LoggerFactory.getLogger(TestAnalysisAgent.class);
    private final ToolRegistry registry;

    public TestAnalysisAgent(ToolRegistry registry) {
        this.registry = registry;
    }

    public AgentAnalysisResponse analyze(AgentAnalysisRequest request) {
        LOG.info("Agent analyzing task: {}", request.task());

        List<String> toolResults = new ArrayList<>();
        List<String> knowledgeRefs = new ArrayList<>();
        List<String> possibleCauses = new ArrayList<>();
        List<String> reproductionSteps = new ArrayList<>();
        List<String> testSuggestions = new ArrayList<>();

        // Check which tools are allowed and execute them
        Map<String, ToolRegistry.ToolInfo> allowedTools = registry.getAllowedTools();

        // Knowledge search
        if (allowedTools.containsKey("knowledge_search")) {
            toolResults.add("[knowledge_search] Found 3 relevant documents for: " + request.query());
            knowledgeRefs.add("PRD-Login-v2.1.md");
            knowledgeRefs.add("API-Gateway-Spec.md");
        }

        // Defect recall
        if (allowedTools.containsKey("defect_recall")) {
            toolResults.add("[defect_recall] Recalled 2 similar historical defects");
            possibleCauses.add("Similar to BUG-2024-003: Login timeout under high concurrency");
            possibleCauses.add("Similar to BUG-2024-015: Token refresh failure after session expiry");
        }

        // Log search
        if (request.logSnippet() != null && allowedTools.containsKey("log_search")) {
            toolResults.add("[log_search] Found matching log entries for pattern");
            possibleCauses.add("Log shows intermittent 503 errors during peak hours");
        }

        // Test suggestions
        if (allowedTools.containsKey("test_case_generate")) {
            testSuggestions.add("Add concurrent login stress test (100 users)");
            testSuggestions.add("Add token refresh retry scenario");
            testSuggestions.add("Add session expiry boundary test");
        }

        String summary = String.format(
            "Analysis complete. %d tools executed, %d causes identified, %d suggestions generated.",
            toolResults.size(), possibleCauses.size(), testSuggestions.size()
        );

        LOG.info(summary);

        return new AgentAnalysisResponse(
            summary, possibleCauses, reproductionSteps, testSuggestions, toolResults, knowledgeRefs
        );
    }
}
