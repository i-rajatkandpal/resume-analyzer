package com.example.resume.service.impl;

import com.example.resume.dto.ResumeAiSuggestionsResponse;
import com.example.resume.dto.ResumeJson;
import com.example.resume.service.AiEnhancementService;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiEnhancementServiceImpl implements AiEnhancementService {

    @Value("${gemini.api.key}")
    private String apiKey;

    // Use Gemini 2.0 Flash
    private static final String GEMINI_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResumeAiSuggestionsResponse enhanceResume(ResumeJson resumeJson) {
        try {
            // Build the prompt
            String prompt = "You are a world-class, meticulous, and empathetic career coach and resume analyst. "
                    + "Your task is to provide a comprehensive, actionable, and highly personalized review of the user's resume. "
                    + "If no job description is provided, you must infer the most likely job role from the resume content and align your feedback accordingly.\n\n"
                    + "### INSTRUCTIONS ###\n"
                    + "Your analysis must be structured into the following five sections:\n\n"
                    + "1. Grammar, Formatting, and Readability Fixes\n"
                    + "2. Missing Essential Skills & Keywords\n"
                    + "3. Recommended Certifications & Courses\n"
                    + "4. Suggested Projects to Showcase Skills\n"
                    + "5. A Rewritten, Stronger Professional Summary\n\n"
                    + "### CONSTRAINTS ###\n"
                    + "* Do not invent skills or experiences not present.\n"
                    + "* Use Markdown with clear headings and bullet points.\n"
                    + "* Be encouraging and professional.\n\n"
                    + "### INPUTS ###\n"
                    + "**Resume JSON:**\n" + mapper.writeValueAsString(resumeJson) + "\n\n"
                    + "**Job Description:**\nNot provided. Please infer the most relevant job role.";

            // Build request
            String requestBody = "{\n"
                    + "  \"contents\": [{\"parts\":[{\"text\": " + mapper.writeValueAsString(prompt) + "}]}]\n"
                    + "}";

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-goog-api-key", apiKey)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();

            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no body";
                throw new RuntimeException("Gemini API failed: " + response.code() + " - " + errorBody);
            }

            String rawJson = response.body().string();
            String aiText = mapper.readTree(rawJson)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // Parse AI response into structured sections
            return parseAiResponse(aiText);

        } catch (Exception e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    private ResumeAiSuggestionsResponse parseAiResponse(String aiText) {
        ResumeAiSuggestionsResponse result = new ResumeAiSuggestionsResponse();

        // Split by headings (### sections)
        String[] sections = aiText.split("###");

        for (String section : sections) {
            String lower = section.toLowerCase();

            if (lower.contains("grammar")) {
                result.setGrammarSuggestions(extractBullets(section));
            } else if (lower.contains("missing")) {
                result.setMissingSkills(extractBullets(section));
            } else if (lower.contains("certification")) {
                result.setCertifications(extractBullets(section));
            } else if (lower.contains("project")) {
                result.setProjects(extractBullets(section));
            } else if (lower.contains("summary")) {
                result.setRewrittenSummary(extractSummaryText(section));
            }
        }

        return result;
    }

    private List<String> extractBullets(String section) {
        List<String> bullets = new ArrayList<>();
        for (String line : section.split("\n")) {
            if (line.trim().startsWith("*")) {
                bullets.add(line.trim().replaceFirst("^\\*+", "").trim());
            }
        }
        return bullets;
    }

    private String extractSummaryText(String section) {
        // Grab last non-empty non-bullet line
        String[] lines = section.split("\n");
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i].trim();
            if (!line.isEmpty() && !line.startsWith("*")) {
                return line;
            }
        }
        return "Summary not found";
    }
}
