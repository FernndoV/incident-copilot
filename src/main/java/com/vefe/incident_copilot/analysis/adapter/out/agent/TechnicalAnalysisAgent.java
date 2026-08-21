package com.vefe.incident_copilot.analysis.adapter.out.agent;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.out.SpecializedAgent;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class TechnicalAnalysisAgent implements SpecializedAgent {
    public String agentId() { return "technical-analysis-agent"; }
    public AgentContribution analyze(IncidentContext context) {
        var text = (context.title() + " " + context.description()).toLowerCase();
        var database = text.contains("database") || text.contains("base de datos") || text.contains("sql");
        return new AgentContribution(agentId(), ContributionStatus.SUCCESS, database ? Category.DATABASE : Category.APPLICATION, Severity.HIGH, .85,
                "Análisis técnico determinista", database ? List.of("Pool de conexiones agotado", "Base de datos no disponible") : List.of("Error en la lógica de aplicación"),
                database ? List.of("Revisar métricas del pool", "Comprobar conectividad con la base de datos") : List.of("Revisar logs de la aplicación", "Reproducir el error en un entorno controlado"), null);
    }
}
