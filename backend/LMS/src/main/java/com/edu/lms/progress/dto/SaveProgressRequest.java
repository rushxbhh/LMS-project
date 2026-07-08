package com.edu.lms.progress.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class SaveProgressRequest {

    @Min(0)
    private Integer watchedSeconds;

    @Min(0)
    @Max(100)
    private Integer watchedPercentage;
}