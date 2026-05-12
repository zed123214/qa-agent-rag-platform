package com.qaagent.rag.service;

import com.qaagent.rag.dto.TestCaseGenerateRequest;
import com.qaagent.rag.dto.TestCaseItem;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo service for generating structured test cases from PRD text.
 * In production, this would call an LLM with a structured prompt.
 * Currently returns mock test cases for demonstration.
 */
public class TestCaseGenerationService {

    private static final Logger LOG = LoggerFactory.getLogger(TestCaseGenerationService.class);

    public List<TestCaseItem> generate(TestCaseGenerateRequest request) {
        LOG.info("Generating test cases from PRD (length={})...", request.prdText().length());

        List<TestCaseItem> cases = new ArrayList<>();

        // Normal flow
        cases.add(new TestCaseItem(
            UUID.randomUUID().toString().substring(0, 8),
            "[Normal] Valid login with correct credentials",
            "User has a valid account",
            List.of("Open login page", "Enter valid username and password", "Click login"),
            "Login successful, redirect to homepage, token returned",
            "P0",
            List.of("smoke", "login", "normal")
        ));

        // Exception flow
        cases.add(new TestCaseItem(
            UUID.randomUUID().toString().substring(0, 8),
            "[Exception] Login with incorrect password",
            "User account exists",
            List.of("Open login page", "Enter valid username", "Enter incorrect password", "Click login"),
            "Login failed, error message 'Invalid credentials' displayed",
            "P1",
            List.of("login", "exception", "security")
        ));

        // Boundary
        cases.add(new TestCaseItem(
            UUID.randomUUID().toString().substring(0, 8),
            "[Boundary] Password field max length exceeded",
            "Password max length is 128 chars",
            List.of("Enter username", "Enter password with 129 characters", "Click login"),
            "Input validation error, 'Password too long' message shown",
            "P2",
            List.of("login", "boundary", "validation")
        ));

        // Permission
        cases.add(new TestCaseItem(
            UUID.randomUUID().toString().substring(0, 8),
            "[Permission] Login with disabled account",
            "Admin has disabled the test account",
            List.of("Enter credentials of disabled account", "Click login"),
            "Login rejected, 'Account disabled' message shown",
            "P1",
            List.of("login", "permission", "security")
        ));

        LOG.info("Generated {} test cases", cases.size());
        return cases;
    }
}
