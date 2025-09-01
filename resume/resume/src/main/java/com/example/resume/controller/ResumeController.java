package com.example.resume.controller;

import com.example.resume.dto.ResumeAnalysisResponse;
import com.example.resume.dto.ResumeUploadResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @PostMapping("/upload")
    public ResponseEntity<ResumeUploadResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        String resumeId = UUID.randomUUID().toString();
        // This is a mock response as per the plan. No actual upload logic yet.
        return new ResponseEntity<>(new ResumeUploadResponse(resumeId, "uploaded"), HttpStatus.CREATED);
    }

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestParam("resumeId") String resumeId) {
        String analysisId = UUID.randomUUID().toString();
        // This is a mock response. No actual analysis logic yet.
        return new ResponseEntity<>(new ResumeAnalysisResponse(analysisId, "processing"), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getResumeAnalysis(@PathVariable("id") String id) {
        // This is a mock response. The real implementation would return parsed data.
        String mockAnalysis = "Mock analysis for resume ID " + id + ": The resume highlights skills in Java and Spring Boot.";
        return new ResponseEntity<>(mockAnalysis, HttpStatus.OK);
    }
}
