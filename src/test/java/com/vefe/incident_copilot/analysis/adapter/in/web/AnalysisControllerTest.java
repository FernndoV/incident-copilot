package com.vefe.incident_copilot.analysis.adapter.in.web;

import com.vefe.incident_copilot.analysis.domain.model.*;
import com.vefe.incident_copilot.analysis.domain.port.in.AnalyzeIncidentUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AnalysisController.class)
class AnalysisControllerTest {
    @Autowired MockMvc mockMvc;
    @MockitoBean AnalyzeIncidentUseCase useCase;

    @Test
    void returnsAnalysisWithoutRequestBody() throws Exception {
        when(useCase.analyze(eq(1L))).thenReturn(analysis());

        mockMvc.perform(post("/api/incidents/1/analysis"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value("DATABASE"))
                .andExpect(jsonPath("$.agentContributions[0].agentId").value("triage-agent"));
    }

    @Test
    void rejectsNonPositiveId() throws Exception {
        mockMvc.perform(post("/api/incidents/0/analysis"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INCIDENT_ID"));
    }

    private IncidentAnalysis analysis() {
        return new IncidentAnalysis(1L, Category.DATABASE, Severity.HIGH, "Database analysis", List.of("Pool exhausted"), List.of("Review pool"), List.of(), List.of("triage-agent"), List.of(new AgentContribution("triage-agent", ContributionStatus.SUCCESS, Category.DATABASE, Severity.HIGH, .8, "DB", List.of(), List.of(), null)), .8);
    }
}
