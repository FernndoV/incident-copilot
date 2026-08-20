package com.vefe.incident_copilot.incident;

import java.time.Instant;

public record IncidentResponse(
        Long id,
        String title,
        String description,
        IncidentStatus status,
        Instant createdAt
) {
    static IncidentResponse from(Incident incident) {
        return new IncidentResponse(
                incident.getId(),
                incident.getTitle(),
                incident.getDescription(),
                incident.getStatus(),
                incident.getCreatedAt()
        );
    }
}
