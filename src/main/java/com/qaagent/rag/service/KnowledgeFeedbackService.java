package com.qaagent.rag.service;

import com.qaagent.rag.dto.TestCaseItem;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demo service for feeding confirmed test cases back into the knowledge base.
 * In production, this would vectorize confirmed cases and add to the vector store.
 * Currently logs the feedback for demonstration purposes.
 */
public class KnowledgeFeedbackService {

    private static final Logger LOG = LoggerFactory.getLogger(KnowledgeFeedbackService.class);

    /**
     * Simulates feeding confirmed test cases back to the knowledge base.
     */
    public String feedback(List<TestCaseItem> confirmedCases, String source) {
        LOG.info("Feeding back {} confirmed test cases from: {}", confirmedCases.size(), source);

        StringBuilder summary = new StringBuilder("Knowledge feedback summary:\n");
        for (TestCaseItem tc : confirmedCases) {
            summary.append("  - [").append(tc.caseId()).append("] ")
                   .append(tc.title()).append("\n");
            LOG.debug("Feedback: case={}, priority={}, tags={}",
                tc.caseId(), tc.priority(), tc.tags());
        }

        LOG.info("Feedback complete. {} cases would be vectorized and stored.", confirmedCases.size());
        return summary.toString();
    }
}
