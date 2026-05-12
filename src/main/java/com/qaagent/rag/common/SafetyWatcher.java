package com.qaagent.rag.common;

import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic safety checker for tool calls.
 * Checks for dangerous commands, suspicious patterns, and empty params.
 * This is a demo-level implementation; production would need Docker sandbox.
 */
public class SafetyWatcher {

    private static final Logger LOG = LoggerFactory.getLogger(SafetyWatcher.class);

    private static final Set<String> DANGEROUS_COMMANDS = Set.of(
        "rm -rf", "dd if=", "mkfs", ":(){ :|:& };:",
        "chmod 777 /", "wget -O /etc/", "curl | sh"
    );

    private static final Set<String> SENSITIVE_PATHS = Set.of(
        "/etc/passwd", "/etc/shadow", "/root/", "C:\\Windows\\System32"
    );

    public record SafetyResult(boolean allow, String reason) {
        public static SafetyResult allow() { return new SafetyResult(true, "OK"); }
        public static SafetyResult block(String reason) { return new SafetyResult(false, reason); }
    }

    public SafetyResult check(String toolName, String toolParams) {
        // Check empty tool name
        if (toolName == null || toolName.isBlank()) {
            return SafetyResult.block("Tool name is empty");
        }

        // Check empty params
        if (toolParams == null || toolParams.isBlank()) {
            return SafetyResult.block("Tool params are empty for: " + toolName);
        }

        // Check dangerous commands
        for (String cmd : DANGEROUS_COMMANDS) {
            if (toolParams.toLowerCase().contains(cmd.toLowerCase())) {
                LOG.warn("Blocked dangerous command: {}", cmd);
                return SafetyResult.block("Dangerous command detected: " + cmd);
            }
        }

        // Check sensitive paths
        for (String path : SENSITIVE_PATHS) {
            if (toolParams.contains(path)) {
                LOG.warn("Blocked sensitive path: {}", path);
                return SafetyResult.block("Sensitive path detected: " + path);
            }
        }

        // Check potential prompt injection patterns
        List<String> injectionPatterns = List.of(
            "ignore previous instructions",
            "ignore all previous",
            "forget your rules",
            "you are now DAN",
            "pretend you are"
        );
        for (String pattern : injectionPatterns) {
            if (toolParams.toLowerCase().contains(pattern)) {
                LOG.warn("Blocked potential prompt injection: {}", pattern);
                return SafetyResult.block("Potential prompt injection detected");
            }
        }

        // Check suspicious URLs (internal IP ranges)
        if (toolParams.contains("http://10.") || toolParams.contains("http://172.") ||
            toolParams.contains("http://192.168.")) {
            LOG.warn("Blocked internal URL access");
            return SafetyResult.block("Access to internal network not allowed");
        }

        return SafetyResult.allow();
    }
}
