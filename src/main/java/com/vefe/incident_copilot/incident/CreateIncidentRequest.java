package com.vefe.incident_copilot.incident;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateIncidentRequest(
        @NotBlank(message = "title es obligatorio")
        @Size(max = 120, message = "title no puede superar los 120 caracteres")
        String title,
        @NotBlank(message = "description es obligatorio")
        @Size(max = 2000, message = "description no puede superar los 2000 caracteres")
        String description
) {
}
