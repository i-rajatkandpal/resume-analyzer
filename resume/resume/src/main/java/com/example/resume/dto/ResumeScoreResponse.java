package com.example.resume.dto;

import java.util.List;

public class ResumeScoreResponse {
    private int atsScore;
    private int suggestedBoost;
    private int finalScore;
    private List<String> issues;

    // Getters and Setters
    public int getAtsScore() {
        return atsScore;
    }

    public void setAtsScore(int atsScore) {
        this.atsScore = atsScore;
    }

    public int getSuggestedBoost() {
        return suggestedBoost;
    }

    public void setSuggestedBoost(int suggestedBoost) {
        this.suggestedBoost = suggestedBoost;
    }

    public int getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(int finalScore) {
        this.finalScore = finalScore;
    }

    public List<String> getIssues() {
        return issues;
    }

    public void setIssues(List<String> issues) {
        this.issues = issues;
    }
}
