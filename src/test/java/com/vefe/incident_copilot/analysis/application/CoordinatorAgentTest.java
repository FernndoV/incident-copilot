package com.vefe.incident_copilot.analysis.application;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.out.SpecializedAgent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CoordinatorAgentTest {

    private static final IncidentContext CONTEXT = new IncidentContext(1L, "DB error", "Database timeout", null, Instant.parse("2026-08-20T10:00:00Z"));

    @Test
    void aggregatesContributionsDeterministically() {
        var agents = List.of(
                agent("triage-agent", contribution("triage-agent", Category.DATABASE, Severity.HIGH, .8, "DB classification", List.of(), List.of())),
                agent("technical-analysis-agent", contribution("technical-analysis-agent", Category.UNKNOWN, Severity.CRITICAL, .9, "Technical", List.of("Pool exhausted", "Database unavailable"), List.of("Review pool", "Check connectivity"))),
                agent("security-agent", contribution("security-agent", Category.SECURITY, Severity.MEDIUM, .7, "Security", List.of(), List.of("Review pool")))
        );

        var result = new CoordinatorAgent(agents).analyze(CONTEXT);

        assertThat(result.category()).isEqualTo(Category.DATABASE);
        assertThat(result.severity()).isEqualTo(Severity.CRITICAL);
        assertThat(result.rootCauseHypotheses()).containsExactly("Pool exhausted", "Database unavailable");
        assertThat(result.recommendedActions()).containsExactly("Review pool", "Check connectivity");
        assertThat(result.confidence()).isEqualTo(.8);
        assertThat(result.participatingAgents()).containsExactly("triage-agent", "technical-analysis-agent", "security-agent");
    }

    @Test
    void returnsPartialResultAndRecordsFailedAgent() {
        var agents = List.of(
                agent("triage-agent", contribution("triage-agent", Category.DATABASE, Severity.HIGH, .8, "DB", List.of(), List.of())),
                failingAgent("technical-analysis-agent"),
                agent("security-agent", contribution("security-agent", Category.SECURITY, Severity.LOW, .6, "Security", List.of(), List.of("Rotate credentials")))
        );

        var result = new CoordinatorAgent(agents).analyze(CONTEXT);

        assertThat(result.warnings()).containsExactly("technical-analysis-agent: AGENT_UNAVAILABLE");
        assertThat(result.agentContributions().get(1).status()).isEqualTo(ContributionStatus.FAILED);
        assertThat(result.participatingAgents()).doesNotContain("technical-analysis-agent");
    }

    @Test
    void failsWhenAllAgentsFail() {
        var agents = List.of(failingAgent("triage-agent"), failingAgent("technical-analysis-agent"));

        assertThatThrownBy(() -> new CoordinatorAgent(agents).analyze(CONTEXT))
                .isInstanceOf(AllAgentsFailedException.class);
    }

    private static SpecializedAgent agent(String id, AgentContribution result) {
        return new SpecializedAgent() { public String agentId() { return id; } public AgentContribution analyze(IncidentContext context) { return result; } };
    }

    private static SpecializedAgent failingAgent(String id) {
        return new SpecializedAgent() { public String agentId() { return id; } public AgentContribution analyze(IncidentContext context) { throw new AgentExecutionException("AGENT_UNAVAILABLE"); } };
    }

    private static AgentContribution contribution(String id, Category category, Severity severity, double confidence, String summary, List<String> hypotheses, List<String> recommendations) {
        return new AgentContribution(id, ContributionStatus.SUCCESS, category, severity, confidence, summary, hypotheses, recommendations, null);
    }
}
