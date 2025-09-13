package com.example.resume.dto;

import java.util.List;

public class ResumeAiSuggestionsResponse {
    private List<String> grammarSuggestions;
    private List<String> missingSkills;
    private List<String> certifications;
    private List<String> projects;
    private String rewrittenSummary;

    public List<String> getGrammarSuggestions() { return grammarSuggestions; }
    public void setGrammarSuggestions(List<String> grammarSuggestions) { this.grammarSuggestions = grammarSuggestions; }

    public List<String> getMissingSkills() { return missingSkills; }
    public void setMissingSkills(List<String> missingSkills) { this.missingSkills = missingSkills; }

    public List<String> getCertifications() { return certifications; }
    public void setCertifications(List<String> certifications) { this.certifications = certifications; }

    public List<String> getProjects() { return projects; }
    public void setProjects(List<String> projects) { this.projects = projects; }

    public String getRewrittenSummary() { return rewrittenSummary; }
    public void setRewrittenSummary(String rewrittenSummary) { this.rewrittenSummary = rewrittenSummary; }
}
