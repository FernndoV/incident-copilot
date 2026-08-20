package com.vefe.incident_copilot.incident;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncidentServiceTest {

    @Mock
    private IncidentRepository repository;

    @InjectMocks
    private IncidentService service;

    @Test
    void createsIncidentWithTrimmedValuesAndOpenStatus() {
        var saved = new Incident(1L, "La aplicación no responde", "Usuarios reciben errores 500", IncidentStatus.OPEN, Instant.parse("2026-08-20T10:00:00Z"));
        when(repository.save(any(Incident.class))).thenReturn(saved);

        var result = service.create(new CreateIncidentRequest("  La aplicación no responde  ", "  Usuarios reciben errores 500  "));

        assertThat(result.id()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("La aplicación no responde");
        assertThat(result.description()).isEqualTo("Usuarios reciben errores 500");
        assertThat(result.status()).isEqualTo(IncidentStatus.OPEN);
        assertThat(result.createdAt()).isEqualTo(Instant.parse("2026-08-20T10:00:00Z"));
        verify(repository).save(any(Incident.class));
    }
}
