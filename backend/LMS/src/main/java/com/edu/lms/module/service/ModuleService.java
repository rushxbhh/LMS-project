package com.edu.lms.module.service;

import com.edu.lms.module.dto.CreateModuleRequest;
import com.edu.lms.module.dto.ModuleDto;
import com.edu.lms.module.dto.UpdateModuleRequest;

import java.util.UUID;

public interface ModuleService {

    ModuleDto createModule(UUID courseId,
                           CreateModuleRequest request);

    ModuleDto updateModule(UUID moduleId,
                           UpdateModuleRequest request);

    void deleteModule(UUID moduleId);

}
