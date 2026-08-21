package com.vefe.incident_copilot.analysis.domain.port.out;

import com.vefe.incident_copilot.analysis.domain.model.IncidentContext;
import java.util.Optional;

public interface IncidentQueryPort { Optional<IncidentContext> findById(Long incidentId); }
