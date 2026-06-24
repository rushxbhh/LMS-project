package com.edu.lms.instructor.dto;

import com.edu.lms.instructor.entity.InstructorApplication;
import com.edu.lms.instructor.entity.InstructorApplicationStatus;
import com.edu.lms.instructor.entity.InstructorApplicationType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class InstructorApplicationDto {

    private UUID id;
    private InstructorApplicationType type;
    private InstructorApplicationStatus status;

    private UUID applicantId;
    private String applicantName;
    private String applicantEmail;

    // Experienced track
    private String resumeUrl;
    private String linkedinUrl;
    private String teachingExperience;
    private String portfolioUrl;

    // Newbie track
    private UUID proposedCourseId;
    private String proposedCourseTitle;

    private String adminNote;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private LocalDateTime createdAt;

    public static InstructorApplicationDto from(InstructorApplication app) {
        return InstructorApplicationDto.builder()
                .id(app.getId())
                .type(app.getType())
                .status(app.getStatus())
                .applicantId(app.getApplicant().getId())
                .applicantName(app.getApplicant().getName())
                .applicantEmail(app.getApplicant().getEmail())
                .resumeUrl(app.getResumeUrl())
                .linkedinUrl(app.getLinkedinUrl())
                .teachingExperience(app.getTeachingExperience())
                .proposedCourseId(app.getProposedCourse() != null ? app.getProposedCourse().getId() : null)
                .proposedCourseTitle(app.getProposedCourse() != null ? app.getProposedCourse().getTitle() : null)
                .adminNote(app.getAdminNote())
                .reviewedByName(app.getReviewedBy() != null ? app.getReviewedBy().getName() : null)
                .reviewedAt(app.getReviewedAt())
                .createdAt(app.getCreatedAt())
                .build();
    }
}
