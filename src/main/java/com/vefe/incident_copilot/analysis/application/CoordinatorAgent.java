package com.vefe.incident_copilot.analysis.application;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.in.AnalyzeIncidentUseCase;
import com.vefe.incident_copilot.analysis.domain.port.out.*;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CoordinatorAgent implements AnalyzeIncidentUseCase {
    private final IncidentQueryPort incidents;
    private final List<SpecializedAgent> agents;

    @Autowired
    public CoordinatorAgent(IncidentQueryPort incidents, List<SpecializedAgent> agents) {
        this.incidents = Objects.requireNonNull(incidents, "incidents");
        this.agents = List.copyOf(agents);
    }

    CoordinatorAgent(List<SpecializedAgent> agents) { this.incidents = null; this.agents = List.copyOf(agents); }

    @Override public IncidentAnalysis analyze(Long incidentId) {
        if (incidentId == null || incidentId <= 0) throw new InvalidIncidentIdException();
        var context = incidents == null ? null : incidents.findById(incidentId).orElseThrow(IncidentNotFoundException::new);
        return analyze(context);
    }

    public IncidentAnalysis analyze(IncidentContext context) {
        var contributions = new ArrayList<AgentContribution>();
        for (var agent : agents) {
            try { contributions.add(validate(agent, Optional.ofNullable(agent.analyze(context)).orElseThrow(() -> new AgentExecutionException("AGENT_UNAVAILABLE")))); }
            catch (AgentExecutionException ex) { contributions.add(AgentContribution.failed(agent.agentId(), ex.errorCode())); }
            catch (RuntimeException ex) { contributions.add(AgentContribution.failed(agent.agentId(), "AGENT_UNAVAILABLE")); }
        }
        var successful = contributions.stream().filter(c -> c.status() == ContributionStatus.SUCCESS).toList();
        if (successful.isEmpty()) throw new AllAgentsFailedException();
        var category = successful.stream().filter(c -> c.agentId().equals("triage-agent")).findFirst().map(AgentContribution::category)
                .orElseGet(() -> successful.stream().filter(c -> c.category() != null).max(Comparator.comparingDouble(c -> valid(c.confidence()) ? c.confidence() : -1)).map(AgentContribution::category).orElse(Category.UNKNOWN));
        var severity = successful.stream().map(AgentContribution::severity).filter(Objects::nonNull).max(Comparator.comparingInt(Enum::ordinal)).orElse(Severity.LOW);
        var hypotheses = distinct(successful.stream().flatMap(c -> c.hypotheses().stream()).toList());
        var recommendations = distinct(successful.stream().flatMap(c -> c.recommendations().stream()).toList());
        var warnings = contributions.stream().filter(c -> c.status() == ContributionStatus.FAILED).map(c -> c.agentId() + ": " + c.errorCode()).toList();
        var confidence = successful.stream().map(AgentContribution::confidence).filter(this::valid).mapToDouble(Double::doubleValue).average().orElse(0);
        confidence = Math.round(confidence * 100.0) / 100.0;
        return new IncidentAnalysis(context.incidentId(), category, severity,
                "La incidencia parece relacionada con " + category.name().toLowerCase(Locale.ROOT) + " y tiene severidad " + severity.name().toLowerCase(Locale.ROOT) + ".",
                hypotheses, recommendations, warnings, successful.stream().map(AgentContribution::agentId).toList(), contributions, confidence);
    }
    private AgentContribution validate(SpecializedAgent agent, AgentContribution contribution) {
        if (!agent.agentId().equals(contribution.agentId()) || contribution.status() != ContributionStatus.SUCCESS || contribution.category() == null || contribution.severity() == null || !valid(contribution.confidence())) {
            return AgentContribution.failed(agent.agentId(), "AGENT_INVALID_RESPONSE");
        }
        return contribution;
    }
    private boolean valid(Double value) { return value != null && value >= 0 && value <= 1; }
    private List<String> distinct(List<String> values) { var seen = new LinkedHashMap<String,String>(); values.forEach(v -> { var k=v.trim().toLowerCase(Locale.ROOT); seen.putIfAbsent(k, v.trim()); }); return List.copyOf(seen.values()); }
}





