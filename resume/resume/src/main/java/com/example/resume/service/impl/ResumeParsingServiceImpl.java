package com.example.resume.service.impl;

import com.example.resume.dto.ResumeJson;
import com.example.resume.service.ResumeParsingService;
import org.apache.tika.Tika;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.ArrayList;

@Service
public class ResumeParsingServiceImpl implements ResumeParsingService {

    // Inject upload directory from application.properties
    @Value("${resume.upload.dir}")
    private String uploadDirPath;

    @Override
    public ResumeJson parse(String resumeId) {
        try {
            File folder = new File(uploadDirPath);

            // Find the file with given resumeId (any extension)
            File[] matches = folder.listFiles((dir, name) -> name.startsWith(resumeId));
            if (matches == null || matches.length == 0) {
                throw new RuntimeException("No file found for resumeId: " + resumeId);
            }

            File file = matches[0]; // first match
            System.out.println("Parsing file: " + file.getAbsolutePath());

            // Use Apache Tika to extract text
            Tika tika = new Tika();
            String text = tika.parseToString(file);

            // Build ResumeJson (basic structure)
            ResumeJson resumeJson = new ResumeJson();
            resumeJson.setSummary(text.substring(0, Math.min(200, text.length())));
            resumeJson.setSkills(new ArrayList<>());      // TODO: extract real skills later
            resumeJson.setExperiences(new ArrayList<>()); // TODO: extract real experiences later
            resumeJson.setEducation(new ArrayList<>());   // TODO: extract real education later

            return resumeJson;

        } catch (Exception e) {
            throw new RuntimeException("Error parsing resume: " + e.getMessage(), e);
        }
    }
}
