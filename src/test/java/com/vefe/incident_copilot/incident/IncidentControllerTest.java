package com.vefe.incident_copilot.incident;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;


import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(IncidentController.class)
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IncidentService service;

    @Test
    void createsIncidentAndReturnsLocation() throws Exception {
        when(service.create(any(CreateIncidentRequest.class)))
                .thenReturn(new IncidentResponse(15L, "Error", "No responde", IncidentStatus.OPEN, Instant.parse("2026-08-20T10:00:00Z")));

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"  Error  \",\"description\":\"  No responde  \"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/incidents/15"))
                .andExpect(jsonPath("$.id").value(15))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.createdAt").value("2026-08-20T10:00:00Z"));
    }

    @Test
    void rejectsBlankTitle() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"   \",\"description\":\"Description válida\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void rejectsTooLongDescription() throws Exception {
        var description = "x".repeat(2001);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Error\",\"description\":\"" + description + "\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rejectsUnsupportedContentType() throws Exception {
        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.TEXT_PLAIN)
                        .content("title=Error"))
                .andExpect(status().isUnsupportedMediaType());
    }
}
