package com.qaagent.rag.controller;

import com.qaagent.rag.agent.TestAnalysisAgent;
import com.qaagent.rag.common.SafetyWatcher;
import com.qaagent.rag.common.ToolRegistry;
import com.qaagent.rag.dto.*;
import com.qaagent.rag.evaluator.AiCriticEvaluator;
import com.qaagent.rag.service.KnowledgeFeedbackService;
import com.qaagent.rag.service.TestCaseExportService;
import com.qaagent.rag.service.TestCaseGenerationService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Demo controller for test intelligence features.
 * These endpoints showcase test case generation, AI evaluation,
 * agent analysis, and export capabilities.
 *
 * NOTE: Most endpoints return mock/demo data for demonstration purposes.
 * Production would integrate with real LLM calls and vector store.
 */
@RestController
@RequestMapping("/api")
public class DemoTestController {

    private static final Logger LOG = LoggerFactory.getLogger(DemoTestController.class);

    private final TestCaseGenerationService generationService;
    private final AiCriticEvaluator evaluator;
    private final TestCaseExportService exportService;
    private final KnowledgeFeedbackService feedbackService;
    private final TestAnalysisAgent agent;
    private final SafetyWatcher safetyWatcher;

    public DemoTestController() {
        this.generationService = new TestCaseGenerationService();
        this.evaluator = new AiCriticEvaluator();
        this.exportService = new TestCaseExportService();
        this.feedbackService = new KnowledgeFeedbackService();
        this.safetyWatcher = new SafetyWatcher();
        ToolRegistry registry = new ToolRegistry(Set.of(
            "knowledge_search", "defect_recall", "log_search",
            "test_case_generate", "ai_critic_evaluate", "test_case_export"
        ));
        this.agent = new TestAnalysisAgent(registry);
    }

    /** Health check endpoint */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "qa-agent-rag-platform",
            "demo-mode", "true"
        ));
    }

    /** Generate structured test cases from PRD text (demo) */
    @PostMapping("/testcase/generate")
    public ResponseEntity<List<TestCaseItem>> generateTestCases(
            @Valid @RequestBody TestCaseGenerateRequest request) {
        LOG.info("Generating test cases...");
        List<TestCaseItem> cases = generationService.generate(request);
        return ResponseEntity.ok(cases);
    }

    /** Evaluate test cases with AI Critic (demo) */
    @PostMapping("/testcase/evaluate")
    public ResponseEntity<EvaluationReport> evaluateTestCases(
            @Valid @RequestBody EvaluationRequest request) {
        LOG.info("Evaluating test cases...");
        EvaluationReport report = evaluator.evaluate(request);
        return ResponseEntity.ok(report);
    }

    /** Export test cases in specified format */
    @PostMapping("/testcase/export")
    public ResponseEntity<String> exportTestCases(
            @RequestBody List<TestCaseItem> cases,
            @RequestParam(defaultValue = "json") String format) {
        LOG.info("Exporting {} cases as {}...", cases.size(), format);
        String result = exportService.export(cases, format);
        return ResponseEntity.ok(result);
    }

    /** Agent-based test analysis (demo) */
    @PostMapping("/agent/analyze")
    public ResponseEntity<AgentAnalysisResponse> agentAnalyze(
            @Valid @RequestBody AgentAnalysisRequest request) {
        LOG.info("Agent analyzing: {}", request.task());

        // Safety check
        SafetyWatcher.SafetyResult safety = safetyWatcher.check("agent_analyze", request.task());
        if (!safety.allow()) {
            return ResponseEntity.badRequest().body(
                new AgentAnalysisResponse(
                    "Blocked: " + safety.reason(), List.of(), List.of(), List.of(), List.of(), List.of()
                )
            );
        }

        AgentAnalysisResponse response = agent.analyze(request);
        return ResponseEntity.ok(response);
    }
}
