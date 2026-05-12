package com.qaagent.rag.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.qaagent.rag.dto.TestCaseItem;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Exports test cases to JSON, YAML, or Markdown format.
 * Uses Jackson for JSON/YAML serialization.
 */
public class TestCaseExportService {

    private static final Logger LOG = LoggerFactory.getLogger(TestCaseExportService.class);
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final YAMLMapper yamlMapper = new YAMLMapper();

    public String export(List<TestCaseItem> cases, String format) {
        LOG.info("Exporting {} test cases as {}...", cases.size(), format);
        try {
            return switch (format.toLowerCase()) {
                case "json" -> jsonMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(Map.of("testCases", cases));
                case "yaml", "yml" -> yamlMapper.writerWithDefaultPrettyPrinter()
                    .writeValueAsString(Map.of("testCases", cases));
                case "markdown", "md" -> toMarkdown(cases);
                default -> throw new IllegalArgumentException("Unsupported format: " + format);
            };
        } catch (Exception e) {
            LOG.error("Export failed", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private String toMarkdown(List<TestCaseItem> cases) {
        StringBuilder sb = new StringBuilder("# Test Cases\n\n");
        for (int i = 0; i < cases.size(); i++) {
            TestCaseItem tc = cases.get(i);
            sb.append("## ").append(i + 1).append(". ").append(tc.title()).append("\n\n");
            sb.append("- **ID:** ").append(tc.caseId()).append("\n");
            sb.append("- **Priority:** ").append(tc.priority()).append("\n");
            sb.append("- **Tags:** ").append(String.join(", ", tc.tags())).append("\n");
            sb.append("- **Precondition:** ").append(tc.precondition()).append("\n");
            sb.append("- **Steps:**\n");
            for (String step : tc.steps()) {
                sb.append("  1. ").append(step).append("\n");
            }
            sb.append("- **Expected:** ").append(tc.expected()).append("\n\n");
        }
        return sb.toString();
    }
}
