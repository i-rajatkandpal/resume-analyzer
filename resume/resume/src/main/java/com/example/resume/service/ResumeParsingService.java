package com.example.resume.service;

import com.example.resume.dto.ResumeJson;

public interface ResumeParsingService {
    ResumeJson parse(String resumeId);
}