package com.edu.lms.module.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class ModuleDto {

    private UUID id;

    private String title;

    private Integer orderIndex;
}
