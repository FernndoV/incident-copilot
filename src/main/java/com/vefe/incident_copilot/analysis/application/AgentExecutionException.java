package com.vefe.incident_copilot.analysis.application;
public class AgentExecutionException extends RuntimeException {
    private final String errorCode;
    public AgentExecutionException(String errorCode) { this.errorCode = errorCode; }
    public String errorCode() { return errorCode; }
}
