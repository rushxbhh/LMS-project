package com.edu.lms.module.service;

import com.edu.lms.common.exception.ResourceNotFoundException;
import com.edu.lms.module.dto.CreateModuleRequest;
import com.edu.lms.module.dto.ModuleDto;
import com.edu.lms.module.dto.UpdateModuleRequest;
import com.edu.lms.course.entity.Course;
import com.edu.lms.module.entity.CourseModule;
import com.edu.lms.course.repository.CourseRepository;
import com.edu.lms.module.repository.ModuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ModuleServiceImpl
        implements ModuleService {

    private final CourseRepository courseRepository;
    private final ModuleRepository moduleRepository;

    @Override
    public ModuleDto createModule(UUID courseId,
                                  CreateModuleRequest request) {

        Course course =
                courseRepository.findById(courseId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Course not found"));

        CourseModule module =
                CourseModule.builder()
                        .title(request.getTitle())
                        .orderIndex(request.getOrderIndex())
                        .course(course)
                        .build();

        module = moduleRepository.save(module);

        return mapToDto(module);
    }

    @Override
    public ModuleDto updateModule(UUID moduleId,
                                  UpdateModuleRequest request) {

        CourseModule module =
                moduleRepository.findById(moduleId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Module not found"));

        module.setTitle(request.getTitle());
        module.setOrderIndex(request.getOrderIndex());

        return mapToDto(
                moduleRepository.save(module));
    }

    @Override
    public void deleteModule(UUID moduleId) {

        moduleRepository.deleteById(moduleId);
    }

    private ModuleDto mapToDto(
            CourseModule module) {

        return ModuleDto.builder()
                .id(module.getId())
                .title(module.getTitle())
                .orderIndex(module.getOrderIndex())
                .build();
    }
}
