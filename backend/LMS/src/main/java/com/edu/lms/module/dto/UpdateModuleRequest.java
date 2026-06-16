package com.edu.lms.module.dto;

import lombok.Data;

@Data
public class UpdateModuleRequest {

    private String title;

    private Integer orderIndex;
}