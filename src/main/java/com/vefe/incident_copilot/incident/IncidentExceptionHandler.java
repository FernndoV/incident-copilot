package com.vefe.incident_copilot.incident;

import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.UnsupportedMediaTypeStatusException;

import java.time.Instant;

@RestControllerAdvice
public class IncidentExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException exception) {
        return error(HttpStatus.BAD_REQUEST, "La petición contiene errores de validación");
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleMalformedJson() {
        return error(HttpStatus.BAD_REQUEST, "El cuerpo de la petición no es JSON válido");
    }

    @ExceptionHandler(UnsupportedMediaTypeStatusException.class)
    public org.springframework.http.ResponseEntity<ErrorResponse> handleUnsupportedMediaType() {
        return error(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "El Content-Type debe ser application/json");
    }

    private org.springframework.http.ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return org.springframework.http.ResponseEntity.status(status)
                .body(new ErrorResponse(status.value(), status.getReasonPhrase(), message, Instant.now()));
    }
}
