package com.example.resume.dto;

public class ResumeUploadResponse {
    private String resumeId;
    private String status;

    public ResumeUploadResponse(String resumeId, String status) {
        this.resumeId = resumeId;
        this.status = status;
    }

    // Getters and Setters
    public String getResumeId() {
        return resumeId;
    }

    public void setResumeId(String resumeId) {
        this.resumeId = resumeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
