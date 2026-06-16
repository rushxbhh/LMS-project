package com.edu.lms.module.dto;

import com.edu.lms.lesson.dto.LessonCurriculumDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class ModuleCurriculumDto {
    private UUID id;
    private String title;
    private String description;   // ← was missing
    private Integer orderIndex;
    private List<LessonCurriculumDto> lessons;
}