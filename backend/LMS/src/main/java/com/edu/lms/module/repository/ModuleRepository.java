package com.edu.lms.module.repository;

import com.edu.lms.module.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ModuleRepository
        extends JpaRepository<CourseModule, UUID> {
}