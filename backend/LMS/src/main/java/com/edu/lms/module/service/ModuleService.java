package com.edu.lms.module.service;

import com.edu.lms.module.dto.CreateModuleRequest;
import com.edu.lms.module.dto.ModuleDto;
import com.edu.lms.module.dto.UpdateModuleRequest;

import java.util.UUID;

public interface ModuleService {

    ModuleDto createModule(UUID courseId, CreateModuleRequest request);

    /** courseId is validated against the module's actual parent course */
    ModuleDto updateModule(UUID courseId, UUID moduleId, UpdateModuleRequest request);

    /** courseId is validated against the module's actual parent course */
    void deleteModule(UUID courseId, UUID moduleId);
}