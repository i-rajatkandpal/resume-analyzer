package com.example.resume.service.impl;

import com.example.resume.dto.ResumeAiSuggestionsResponse;
import com.example.resume.dto.ResumeJson;
import com.example.resume.service.AiEnhancementService;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
public class GeminiEnhancementServiceImpl implements AiEnhancementService {

    @Value("${gemini.api.key}")
    private String geminiApiKey;

    private static final String GEMINI_API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

    private final OkHttpClient client = new OkHttpClient();

    @Override
    public ResumeAiSuggestionsResponse enhanceResume(ResumeJson resumeJson) {
        try {
            String prompt = """
You are an experienced career coach and resume analyst.
Your job is to improve the candidate’s resume with practical, market-ready advice.

Analyze the following resume JSON and return feedback in valid JSON format with exactly these keys:
- GrammarSuggestions: list of grammar or wording improvements
- MissingSkills: list of important industry-relevant skills the candidate should add (be specific, e.g., "Hibernate", "Spring Boot", "Docker")
- Certifications: list of certifications that would strengthen the resume (e.g., "Oracle Certified Professional: Java SE", "AWS Certified Developer")
- Projects: list of concrete project ideas the candidate could include to show applied skills (e.g., "E-commerce website using Spring Boot and MySQL")
- RewrittenSummary: a stronger, polished version of the resume’s professional summary

Rules:
- Output only a valid JSON object, no explanations outside it.
- Provide practical, actionable suggestions, not generic advice.
- If information is missing in the resume, assume a typical candidate in that role and suggest accordingly.
- If no grammar issues are found, still include at least 3 suggestions for clarity, conciseness, or formatting improvements.

Resume JSON:
""" + resumeJson.toString();


            // Build request JSON
            JSONObject content = new JSONObject()
                    .put("contents", new JSONArray()
                            .put(new JSONObject()
                                    .put("parts", new JSONArray()
                                            .put(new JSONObject().put("text", prompt)))));

            RequestBody body = RequestBody.create(content.toString(), MediaType.parse("application/json"));

            Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + geminiApiKey)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    throw new RuntimeException("Gemini API failed: " + response);
                }

                String responseBody = response.body().string();
                JSONObject json = new JSONObject(responseBody);

                // extract AI text output
                String rawText = json
                        .getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text");

                // Parse into DTO
                return parseAiResponse(rawText);
            }

        } catch (IOException e) {
            throw new RuntimeException("Error calling Gemini API: " + e.getMessage(), e);
        }
    }

    private ResumeAiSuggestionsResponse parseAiResponse(String rawText) {
        ResumeAiSuggestionsResponse response = new ResumeAiSuggestionsResponse();

        try {
            // clean possible markdown code block fencess
            String cleaned = rawText.trim();
            if (cleaned.startsWith("```")) {
                int firstBrace = cleaned.indexOf("{");
                int lastBrace = cleaned.lastIndexOf("}");
                if (firstBrace != -1 && lastBrace != -1) {
                    cleaned = cleaned.substring(firstBrace, lastBrace + 1);
                }
            }

            JSONObject obj = new JSONObject(cleaned);

            response.setGrammarSuggestions(toList(obj.optJSONArray("GrammarSuggestions")));
            response.setMissingSkills(toList(obj.optJSONArray("MissingSkills")));
            response.setCertifications(toList(obj.optJSONArray("Certifications")));
            response.setProjects(toList(obj.optJSONArray("Projects")));
            response.setRewrittenSummary(obj.optString("RewrittenSummary", null));

        } catch (Exception e) {
            List<String> fallback = new ArrayList<>();
            fallback.add("Could not parse AI response: " + e.getMessage());
            response.setGrammarSuggestions(fallback);
        }

        return response;
    }

    private List<String> toList(JSONArray arr) {
        List<String> list = new ArrayList<>();
        if (arr != null) {
            for (int i = 0; i < arr.length(); i++) {
                list.add(arr.optString(i));
            }
        }
        return list;
    }
}
