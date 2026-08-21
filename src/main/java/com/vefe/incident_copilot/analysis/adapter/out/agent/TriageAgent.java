package com.vefe.incident_copilot.analysis.adapter.out.agent;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.out.SpecializedAgent;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TriageAgent implements SpecializedAgent {
    public String agentId() { return "triage-agent"; }
    public AgentContribution analyze(IncidentContext context) {
        var text = (context.title() + " " + context.description()).toLowerCase();
        var category = text.contains("database") || text.contains("base de datos") || text.contains("sql") ? Category.DATABASE
                : text.contains("network") || text.contains("red") || text.contains("timeout") ? Category.NETWORK : Category.APPLICATION;
        var severity = text.contains("caído") || text.contains("critical") || text.contains("todos") ? Severity.CRITICAL
                : text.contains("error") || text.contains("500") ? Severity.HIGH : Severity.MEDIUM;
        return new AgentContribution(agentId(), ContributionStatus.SUCCESS, category, severity, .8, "Clasificación inicial de la incidencia", List.of(), List.of(), null);
    }
}
