package com.vefe.incident_copilot.analysis.adapter.in.web;

import com.vefe.incident_copilot.analysis.domain.model.IncidentAnalysis;
import com.vefe.incident_copilot.analysis.domain.port.in.AnalyzeIncidentUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/incidents")
public class AnalysisController {
    private final AnalyzeIncidentUseCase useCase;
    public AnalysisController(AnalyzeIncidentUseCase useCase) { this.useCase = useCase; }
    @PostMapping("/{id}/analysis")
    public ResponseEntity<IncidentAnalysis> analyze(@PathVariable Long id) {
        if (id == null || id <= 0) throw new com.vefe.incident_copilot.analysis.application.InvalidIncidentIdException();
        return ResponseEntity.ok(useCase.analyze(id));
    }
}

