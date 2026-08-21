package com.vefe.incident_copilot.analysis.domain.port.out;

import com.vefe.incident_copilot.analysis.domain.model.AgentContribution;
import com.vefe.incident_copilot.analysis.domain.model.IncidentContext;

public interface SpecializedAgent {
    String agentId();
    AgentContribution analyze(IncidentContext context);
}
