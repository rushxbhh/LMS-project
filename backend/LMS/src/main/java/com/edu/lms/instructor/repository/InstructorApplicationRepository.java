package com.edu.lms.instructor.repository;

import com.edu.lms.instructor.entity.InstructorApplication;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InstructorApplicationRepository extends JpaRepository<InstructorApplication, UUID> {

    boolean existsByApplicantIdAndStatus(UUID applicantId, InstructorApplicationStatus status);

    List<InstructorApplication> findByApplicantIdOrderByCreatedAtDesc(UUID applicantId);

    Page<InstructorApplication> findByStatus(InstructorApplicationStatus status, Pageable pageable);

    Page<InstructorApplication> findByType(InstructorApplicationType type, Pageable pageable);

    Page<InstructorApplication> findByTypeAndStatus(
            InstructorApplicationType type, InstructorApplicationStatus status, Pageable pageable);

    long countByTypeAndStatus(InstructorApplicationType type, InstructorApplicationStatus status);
}
