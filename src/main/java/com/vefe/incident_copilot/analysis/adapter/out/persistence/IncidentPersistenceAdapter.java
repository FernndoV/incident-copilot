package com.vefe.incident_copilot.analysis.adapter.out.persistence;

import com.vefe.incident_copilot.analysis.domain.model.IncidentContext;
import com.vefe.incident_copilot.analysis.domain.port.out.IncidentQueryPort;
import com.vefe.incident_copilot.incident.IncidentRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;

@Component
public class IncidentPersistenceAdapter implements IncidentQueryPort {
    private final IncidentRepository repository;
    public IncidentPersistenceAdapter(IncidentRepository repository) { this.repository = repository; }
    public Optional<IncidentContext> findById(Long incidentId) {
        return repository.findById(incidentId).map(i -> new IncidentContext(i.getId(), i.getTitle(), i.getDescription(), null, i.getCreatedAt()));
    }
}
