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

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(60, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ResumeAiSuggestionsResponse enhanceResume(ResumeJson resumeJson) {
        try {
            // Prompt that tells Gemini to infer the JD if missing
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

            // Build request JSON
            String requestBody = "{\n"
                    + "  \"contents\": [{\"parts\":[{\"text\": " + mapper.writeValueAsString(prompt) + "}]}]\n"
                    + "}";

            Request request = new Request.Builder()
                    .url(GEMINI_URL)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("X-goog-api-key", apiKey)
                    .post(RequestBody.create(requestBody, MediaType.parse("application/json")))
                    .build();


            // Execute call
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null ? response.body().string() : "no body";
                throw new RuntimeException("Gemini API failed: " + response.code() + " - " + errorBody);
            }


            // Extract text from Gemini
            String rawJson = response.body().string();
            String aiText = mapper.readTree(rawJson)
                    .path("candidates").get(0)
                    .path("content").path("parts").get(0)
                    .path("text").asText();

            // For now: naive mapping
            ResumeAiSuggestionsResponse result = new ResumeAiSuggestionsResponse();
            result.setGrammarSuggestions(extractSection(aiText, "Grammar"));
            result.setMissingSkills(extractSection(aiText, "Missing"));
            result.setCertifications(extractSection(aiText, "Certification"));
            result.setProjects(extractSection(aiText, "Project"));
            result.setRewrittenSummary(extractSummary(aiText));

            return result;

        } catch (Exception e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    // Utility to extract section lines from AI text
    private List<String> extractSection(String text, String keyword) {
        List<String> lines = new ArrayList<>();
        for (String line : text.split("\n")) {
            if (line.toLowerCase().contains(keyword.toLowerCase()) || line.startsWith("-") || line.startsWith("*")) {
                lines.add(line.trim());
            }
        }
        return lines;
    }

    private String extractSummary(String text) {
        // crude method: grab last section after "Summary"
        int idx = text.toLowerCase().lastIndexOf("summary");
        if (idx != -1) {
            return text.substring(idx).trim();
        }
        return "Summary not found in AI response.";
    }
}
