package com.example.resume.service;

import java.util.List;

public interface SkillDetectionService {
    List<String> detectSkills(String text);
}
