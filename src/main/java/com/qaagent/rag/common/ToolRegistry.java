package com.qaagent.rag.common;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo tool registry for agent tool orchestration.
 * Maintains available tools with descriptions and supports whitelist checking.
 */
public class ToolRegistry {

    private static final Logger LOG = LoggerFactory.getLogger(ToolRegistry.class);

    private final Map<String, ToolInfo> tools = new HashMap<>();
    private Set<String> whitelist;

    public ToolRegistry(Set<String> whitelist) {
        this.whitelist = whitelist;
        registerDefaults();
    }

    public record ToolInfo(String name, String description, String category) {}

    private void registerDefaults() {
        register("knowledge_search", "Search the knowledge base for relevant documents", "retrieval");
        register("defect_recall", "Recall historical defect cases by keywords", "retrieval");
        register("log_search", "Search test logs by traceId or error pattern", "retrieval");
        register("test_case_generate", "Generate structured test cases from PRD", "generation");
        register("ai_critic_evaluate", "Evaluate test case quality (coverage, logic, duplicates)", "evaluation");
        register("test_case_export", "Export test cases to JSON/YAML/Markdown", "export");
    }

    public void register(String name, String description, String category) {
        tools.put(name, new ToolInfo(name, description, category));
        LOG.debug("Registered tool: {}", name);
    }

    public ToolInfo getTool(String name) {
        return tools.get(name);
    }

    public boolean isAllowed(String toolName) {
        if (whitelist == null || whitelist.isEmpty()) return true;
        return whitelist.contains(toolName);
    }

    public Map<String, ToolInfo> getAllTools() {
        return new HashMap<>(tools);
    }

    public Map<String, ToolInfo> getAllowedTools() {
        Map<String, ToolInfo> allowed = new HashMap<>();
        tools.forEach((name, info) -> {
            if (isAllowed(name)) allowed.put(name, info);
        });
        return allowed;
    }
}
