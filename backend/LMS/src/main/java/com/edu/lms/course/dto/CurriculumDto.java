package com.edu.lms.course.dto;

import com.edu.lms.module.dto.ModuleCurriculumDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class CurriculumDto {

    // So the frontend knows the course title without a second call
    private String courseTitle;

    // Tells the frontend which state to render — enrolled vs preview
    private Boolean enrolled;

    private List<ModuleCurriculumDto> modules;
}