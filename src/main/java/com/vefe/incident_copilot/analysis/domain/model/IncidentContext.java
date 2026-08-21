package com.vefe.incident_copilot.analysis.domain.model;

import java.time.Instant;

public record IncidentContext(Long incidentId, String title, String description, Object location, Instant createdAt) { }
