package com.example.resume.service.impl;

import com.example.resume.dto.ResumeJson;
import com.example.resume.dto.ResumeScoreResponse;
import com.example.resume.service.ResumeScoringService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeScoringServiceImpl implements ResumeScoringService {

    @Override
    public ResumeScoreResponse scoreResume(ResumeJson resumeJson) {
        int score = 0;
        List<String> issues = new ArrayList<>();

        // check summary length, short summaries look weak
        if (resumeJson.getSummary() == null || resumeJson.getSummary().length() < 50) {
            issues.add("Summary is too short or missing");
        } else {
            score += 10;
        }

        // check skills section, more skills increase score but cap to avoid inflation
        if (resumeJson.getSkills() == null || resumeJson.getSkills().isEmpty()) {
            issues.add("No skills detected");
        } else {
            score += Math.min(resumeJson.getSkills().size() * 2, 20);
        }

        // check education section, must have at least one entry
        if (resumeJson.getEducation() == null || resumeJson.getEducation().isEmpty()) {
            issues.add("No education details found");
        } else {
            score += 20;
        }

        // check experience section, must have at least one
        if (resumeJson.getExperiences() == null || resumeJson.getExperiences().isEmpty()) {
            issues.add("No work experience found");
        } else {
            score += 30;
        }

        // look for quantified achievements, numbers or percentages make resumes stronger
        String allText = String.join(" ", resumeJson.getExperiences());
        if (allText.matches(".*(\\d+%?|\\+).*")) {
            score += 20;
        } else {
            issues.add("No quantified achievements detected (add metrics like % or numbers)");
        }

        // resume length check, too short = weak, too long = cluttered
        String fullResumeText = resumeJson.getSummary() + " "
                + String.join(" ", resumeJson.getSkills()) + " "
                + String.join(" ", resumeJson.getExperiences()) + " "
                + String.join(" ", resumeJson.getEducation());

        int wordCount = fullResumeText.trim().split("\\s+").length;
        if (wordCount < 100) {
            issues.add("Resume looks too short, consider adding more details");
            score -= 10;
        } else if (wordCount > 2000) {
            issues.add("Resume looks too long, consider keeping it concise");
            score -= 10;
        }

        // action verbs check, resumes should start experiences with strong verbs
        String[] actionVerbs = {"developed", "built", "designed", "led", "implemented", "managed", "improved", "created"};
        boolean hasActionVerb = false;
        for (String verb : actionVerbs) {
            if (allText.toLowerCase().contains(verb)) {
                hasActionVerb = true;
                break;
            }
        }
        if (!hasActionVerb) {
            issues.add("No strong action verbs detected in experience section");
            score -= 5;
        }

        // basic grammar-ish check, just detect repeated words for now
        if (fullResumeText.matches(".*\\b(\\w+)\\s+\\1\\b.*")) {
            issues.add("Repeated words detected, check grammar");
            score -= 5;
        }

        // cap score between 0 and 100
        if (score < 0) score = 0;
        if (score > 100) score = 100;

        // build final response object
        ResumeScoreResponse response = new ResumeScoreResponse();
        response.setAtsScore(score);
        response.setSuggestedBoost(20); // placeholder until AI provides smarter suggestions
        response.setFinalScore(Math.min(score + response.getSuggestedBoost(), 100));
        response.setIssues(issues);

        return response;
    }
}
