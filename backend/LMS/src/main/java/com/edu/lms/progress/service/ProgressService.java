package com.edu.lms.progress.service;

import com.edu.lms.progress.dto.CourseProgressDto;
import com.edu.lms.progress.dto.SaveProgressRequest;

import java.util.UUID;

public interface ProgressService {

    void openLesson(UUID lessonId);

    void saveProgress(
            UUID lessonId,
            SaveProgressRequest request
    );

    void completeLesson(UUID lessonId);

    CourseProgressDto getCourseProgress(
            UUID courseId
    );
}