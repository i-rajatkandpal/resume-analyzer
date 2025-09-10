package com.example.resume.service.impl;

import com.example.resume.dto.ResumeJson;
import com.example.resume.service.ResumeParsingService;
import com.example.resume.service.SkillDetectionService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Service
public class ResumeParsingServiceImpl implements ResumeParsingService {

    @Value("${resume.upload.dir}")
    private String uploadDirPath;

    @Autowired
    private SkillDetectionService skillDetectionService;

    @Override
    public ResumeJson parse(String resumeId) {
        try {
            // find uploaded file by resumeId
            File folder = new File(uploadDirPath);
            File[] matches = folder.listFiles((dir, name) -> name.startsWith(resumeId));
            if (matches == null || matches.length == 0) {
                throw new RuntimeException("No file found for resumeId: " + resumeId);
            }

            File file = matches[0];

            Tika tika = new Tika();
            String text = tika.parseToString(file);

            ResumeJson resumeJson = new ResumeJson();
            resumeJson.setSummary(text.substring(0, Math.min(200, text.length())));

            // ✅ detect skills
            resumeJson.setSkills(skillDetectionService.detectSkills(text));

            // ✅ improved education extraction (section-based)
            resumeJson.setEducation(extractEducation(text));

            // still using keyword-based for experiences for now
            resumeJson.setExperiences(extractByKeyword(text, new String[]{
                    "Experience", "Intern", "Engineer", "Developer", "Manager", "Analyst"
            }));

            return resumeJson;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing resume: " + e.getMessage(), e);
        }
    }

    // Utility: simple keyword-based line extraction
    private List<String> extractByKeyword(String text, String[] keywords) {
        List<String> results = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        for (String line : lines) {
            String lower = line.toLowerCase();
            for (String keyword : keywords) {
                if (lower.contains(keyword.toLowerCase())) {
                    results.add(line.trim());
                    break;
                }
            }
        }
        return results;
    }

    // ✅ Section-based extractor for Education
    private List<String> extractEducation(String text) {
        List<String> education = new ArrayList<>();
        String[] lines = text.split("\\r?\\n");
        boolean inEducation = false;

        for (String rawLine : lines) {
            String line = rawLine.trim();
            String lower = line.toLowerCase();

            if (lower.startsWith("education")) {
                inEducation = true;
                continue; // skip the header itself
            }

            if (inEducation) {
                // stop if new section starts
                if (lower.startsWith("experience") || lower.startsWith("skills") ||
                        lower.startsWith("projects") || lower.startsWith("certifications")) {
                    break;
                }
                if (!line.isEmpty()) {
                    education.add(line);
                }
            }
        }

        return education;
    }
}
