package com.example.resume.service;

import com.example.resume.dto.ResumeJson;
import com.example.resume.dto.ResumeScoreResponse;

public interface ResumeScoringService {
    ResumeScoreResponse scoreResume(ResumeJson resumeJson);
}
