package com.vefe.incident_copilot.analysis.adapter.in.web;

import com.vefe.incident_copilot.analysis.application.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice(assignableTypes = AnalysisController.class)
public class AnalysisExceptionHandler {
    @ExceptionHandler(InvalidIncidentIdException.class)
    ResponseEntity<ProblemDetail> invalid() { return problem(HttpStatus.BAD_REQUEST, "INVALID_INCIDENT_ID", "El identificador de la incidencia debe ser positivo"); }
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ProblemDetail> malformed() { return problem(HttpStatus.BAD_REQUEST, "INVALID_INCIDENT_ID", "El identificador de la incidencia no es válido"); }
    @ExceptionHandler(IncidentNotFoundException.class)
    ResponseEntity<ProblemDetail> notFound() { return problem(HttpStatus.NOT_FOUND, "INCIDENT_NOT_FOUND", "La incidencia no existe"); }
    @ExceptionHandler(AllAgentsFailedException.class)
    ResponseEntity<ProblemDetail> unavailable() { return problem(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS_UNAVAILABLE", "No fue posible analizar la incidencia"); }
    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> coordinatorError() { return problem(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYSIS_COORDINATOR_ERROR", "No fue posible completar el análisis"); }
    private ResponseEntity<ProblemDetail> problem(HttpStatus status, String code, String detail) {
        var p = ProblemDetail.forStatusAndDetail(status, detail); p.setType(java.net.URI.create("urn:incident-copilot:error:" + code.toLowerCase(java.util.Locale.ROOT))); p.setTitle(status.getReasonPhrase()); p.setProperty("code", code); return ResponseEntity.status(status).body(p);
    }
}

