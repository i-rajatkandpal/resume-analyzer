package com.example.resume.service.impl;

import com.example.resume.service.SkillDetectionService;
import com.example.resume.util.SkillDictionary;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SkillDetectionServiceImpl implements SkillDetectionService {

    @Override
    public List<String> detectSkills(String text) {
        List<String> foundSkills = new ArrayList<>();
        String lowerText = text.toLowerCase();

        for (String skill : SkillDictionary.SKILLS) {
            if (lowerText.contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }
}
