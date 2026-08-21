package com.vefe.incident_copilot.analysis.domain.model;

import java.util.List;

public record IncidentAnalysis(Long incidentId, Category category, Severity severity, String summary,
                               List<String> rootCauseHypotheses, List<String> recommendedActions,
                               List<String> warnings, List<String> participatingAgents,
                               List<AgentContribution> agentContributions, double confidence) {
    public IncidentAnalysis {
        rootCauseHypotheses = List.copyOf(rootCauseHypotheses);
        recommendedActions = List.copyOf(recommendedActions);
        warnings = List.copyOf(warnings);
        participatingAgents = List.copyOf(participatingAgents);
        agentContributions = List.copyOf(agentContributions);
    }
}
