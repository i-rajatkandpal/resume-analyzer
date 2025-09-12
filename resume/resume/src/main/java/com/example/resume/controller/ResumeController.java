package com.example.resume.controller;

import com.example.resume.dto.ResumeAnalysisResponse;
import com.example.resume.dto.ResumeUploadResponse;
import com.example.resume.dto.ResumeJson;
import com.example.resume.service.ResumeParsingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import com.example.resume.dto.ResumeScoreResponse;
import com.example.resume.service.ResumeScoringService;


import java.io.File;
import java.util.UUID;

@RestController
@RequestMapping("/api/resume")
public class ResumeController {

    @Autowired
    private ResumeParsingService parsingService;

    // Read upload directory from application.properties
    @Value("${resume.upload.dir}")
    private String uploadDirPath;

    @PostMapping("/upload")
    public ResponseEntity<ResumeUploadResponse> uploadResume(@RequestParam("file") MultipartFile file) {
        try {
            // Generate unique resume ID
            String resumeId = UUID.randomUUID().toString();

            // Ensure uploads directory exists
            File uploadDir = new File(uploadDirPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Extract file extension
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            // Save file as {resumeId}.extension
            File savedFile = new File(uploadDir, resumeId + extension);
            file.transferTo(savedFile);

            // Return response
            return new ResponseEntity<>(
                    new ResumeUploadResponse(resumeId, "uploaded as " + savedFile.getName()),
                    HttpStatus.CREATED
            );

        } catch (Exception e) {
            e.printStackTrace(); // log error
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<ResumeAnalysisResponse> analyzeResume(@RequestParam("resumeId") String resumeId) {
        String analysisId = UUID.randomUUID().toString();
        // Mock response for now
        return new ResponseEntity<>(new ResumeAnalysisResponse(analysisId, "processing"), HttpStatus.ACCEPTED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> getResumeAnalysis(@PathVariable("id") String id) {
        // Mock response for now
        String mockAnalysis = "Mock analysis for resume ID " + id + ": The resume highlights skills in Java and Spring Boot.";
        return new ResponseEntity<>(mockAnalysis, HttpStatus.OK);
    }

    @PostMapping("/parse/{resumeId}")
    public ResponseEntity<ResumeJson> parseResume(@PathVariable String resumeId) {
        ResumeJson parsed = parsingService.parse(resumeId);
        return ResponseEntity.ok(parsed);
    }

    @Autowired
    private ResumeScoringService scoringService;

    @PostMapping("/score/{resumeId}")
    public ResponseEntity<ResumeScoreResponse> scoreResume(@PathVariable String resumeId) {
        ResumeJson parsed = parsingService.parse(resumeId);
        ResumeScoreResponse scoreResponse = scoringService.scoreResume(parsed);
        return ResponseEntity.ok(scoreResponse);
    }

}
