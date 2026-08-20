package com.vefe.incident_copilot.incident;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class IncidentService {

    private final IncidentRepository repository;

    public IncidentService(IncidentRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public IncidentResponse create(CreateIncidentRequest request) {
        var incident = new Incident(
                null,
                request.title().trim(),
                request.description().trim(),
                IncidentStatus.OPEN,
                Instant.now()
        );

        return IncidentResponse.from(repository.save(incident));
    }
}
