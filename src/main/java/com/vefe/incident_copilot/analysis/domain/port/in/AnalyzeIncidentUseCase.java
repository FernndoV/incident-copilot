package com.vefe.incident_copilot.analysis.domain.port.in;

import com.vefe.incident_copilot.analysis.domain.model.IncidentAnalysis;

public interface AnalyzeIncidentUseCase { IncidentAnalysis analyze(Long incidentId); }
