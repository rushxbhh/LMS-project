package com.edu.lms.instructor.service;

import com.edu.lms.instructor.dto.*;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface InstructorApplicationService {

    InstructorApplicationDto submitExperienced(UUID applicantId, SubmitExperiencedApplicationRequest request);

    InstructorApplicationDto submitNewbie(UUID applicantId, SubmitNewbieApplicationRequest request);

    List<InstructorApplicationDto> getMyApplications(UUID applicantId);

    Page<InstructorApplicationDto> getApplicationsForAdmin(
            InstructorApplicationType type, InstructorApplicationStatus status, Pageable pageable);

    InstructorApplicationDto approve(UUID applicationId, UUID adminId, String note);

    InstructorApplicationDto reject(UUID applicationId, UUID adminId, String reason);
}
