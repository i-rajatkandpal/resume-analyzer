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
        List<String> issues = new ArrayList<>();        if (resumeJson.getSummary() == null || resumeJson.getSummary().length() < 50) {
            issues.add("Summary is too short or missing");
        } else {
            score += 10;
        }

        if (resumeJson.getSkills() == null || resumeJson.getSkills().isEmpty()) {
            issues.add("No skills detected");
        } else {
            score += Math.min(resumeJson.getSkills().size() * 2, 20); // cap at 20
        }

        if (resumeJson.getEducation() == null || resumeJson.getEducation().isEmpty()) {
            issues.add("No education details found");
        } else {
            score += 20;
        }

        if (resumeJson.getExperiences() == null || resumeJson.getExperiences().isEmpty()) {
            issues.add("No work experience found");
        } else {
            score += 30;
        }

        String allText = String.join(" ", resumeJson.getExperiences());
        if (allText.matches(".*(\\d+%?|\\+).*")) {
            score += 20;
        } else {
            issues.add("No quantified achievements detected (add metrics like % or numbers)");
        }

        score = Math.min(score, 100);

        ResumeScoreResponse response = new ResumeScoreResponse();
        response.setAtsScore(score);
        response.setSuggestedBoost(20); // placeholder, AI will refine later
        response.setFinalScore(Math.min(score + response.getSuggestedBoost(), 100));
        response.setIssues(issues);

        return response;
    }
}
