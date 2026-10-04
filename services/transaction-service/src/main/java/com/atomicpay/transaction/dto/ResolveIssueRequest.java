package com.atomicpay.transaction.dto;

import jakarta.validation.constraints.NotBlank;

public class ResolveIssueRequest {

    @NotBlank(message = "Resolution details are required")
    private String resolution;

    public ResolveIssueRequest() {}

    public ResolveIssueRequest(String resolution) {
        this.resolution = resolution;
    }

    public String getResolution() { return resolution; }
    public void setResolution(String resolution) { this.resolution = resolution; }
}
