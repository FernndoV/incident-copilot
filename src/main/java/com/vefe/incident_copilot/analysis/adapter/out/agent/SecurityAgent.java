package com.vefe.incident_copilot.analysis.adapter.out.agent;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.out.SpecializedAgent;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SecurityAgent implements SpecializedAgent {
    public String agentId() { return "security-agent"; }
    public AgentContribution analyze(IncidentContext context) {
        var text = (context.title() + " " + context.description()).toLowerCase();
        var security = text.contains("password") || text.contains("token") || text.contains("credencial") || text.contains("security");
        return new AgentContribution(agentId(), ContributionStatus.SUCCESS, security ? Category.SECURITY : Category.UNKNOWN, security ? Severity.HIGH : Severity.LOW, .75,
                "Evaluación de seguridad determinista", security ? List.of("Posible exposición de credenciales") : List.of(), security ? List.of("Rotar credenciales potencialmente expuestas") : List.of(), null);
    }
}
