package com.edu.lms.lesson.service;

import com.edu.lms.lesson.dto.CreateLessonRequest;
import com.edu.lms.lesson.dto.LessonDto;
import com.edu.lms.lesson.dto.UpdateLessonRequest;

import java.util.UUID;

public interface LessonService {

    LessonDto createLesson(UUID moduleId,
                           CreateLessonRequest request);

    LessonDto updateLesson(UUID lessonId,
                           UpdateLessonRequest request);

    LessonDto getLesson(UUID lessonId);

    void deleteLesson(UUID lessonId);
}
