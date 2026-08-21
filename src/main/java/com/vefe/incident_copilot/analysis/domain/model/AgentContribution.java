package com.vefe.incident_copilot.analysis.domain.model;

import java.util.List;

public record AgentContribution(String agentId, ContributionStatus status, Category category, Severity severity,
                                Double confidence, String summary, List<String> hypotheses,
                                List<String> recommendations, String errorCode) {
    public AgentContribution {
        hypotheses = hypotheses == null ? List.of() : List.copyOf(hypotheses);
        recommendations = recommendations == null ? List.of() : List.copyOf(recommendations);
    }

    public static AgentContribution failed(String agentId, String errorCode) {
        return new AgentContribution(agentId, ContributionStatus.FAILED, null, null, 0d, null, List.of(), List.of(), errorCode);
    }
}

