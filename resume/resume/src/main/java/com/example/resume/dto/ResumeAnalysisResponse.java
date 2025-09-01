package com.example.resume.dto;

public class ResumeAnalysisResponse {
    private String analysisId;
    private String status;

    public ResumeAnalysisResponse(String analysisId, String status) {
        this.analysisId = analysisId;
        this.status = status;
    }

    // Getters and Setters
    public String getAnalysisId() {
        return analysisId;
    }

    public void setAnalysisId(String analysisId) {
        this.analysisId = analysisId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
