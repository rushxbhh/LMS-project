package com.edu.lms.lesson.repository;

import com.edu.lms.lesson.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LessonRepository
        extends JpaRepository<Lesson, UUID> {

    List<Lesson> findByModuleId(UUID moduleId);
}