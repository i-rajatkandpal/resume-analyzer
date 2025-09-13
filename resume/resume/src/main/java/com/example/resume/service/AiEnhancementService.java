package com.example.resume.service;

import com.example.resume.dto.ResumeJson;
import com.example.resume.dto.ResumeAiSuggestionsResponse;

public interface AiEnhancementService {
    ResumeAiSuggestionsResponse enhanceResume(ResumeJson resumeJson);
}
