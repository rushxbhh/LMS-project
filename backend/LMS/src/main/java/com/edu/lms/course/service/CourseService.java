package com.edu.lms.course.service;
import com.edu.lms.course.dto.CourseDto;
import com.edu.lms.course.dto.CreateCourseRequest;
import com.edu.lms.course.dto.UpdateCourseRequest;
import com.edu.lms.course.entity.CourseLevel;
import com.edu.lms.course.entity.CourseStatus;
import com.edu.lms.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CourseService {

    CourseDto createCourse(CreateCourseRequest request, User teacher);

    List<CourseDto> getAllPublishedCourses();

    CourseDto getCourseById(UUID id);

    CourseDto updateCourse(UUID id,
                           UpdateCourseRequest request);

    void deleteCourse(UUID id);

    //List<CourseDto> getMyCourses();

    List<CourseDto> getMyCourses(CourseStatus status);

    CourseDto publishCourse(UUID id);

    Page<CourseDto> searchCourses(String search, String category, CourseLevel level,
                                  BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    CourseDto submitForReview(UUID id);

    CourseDto approveCourse(UUID id);

    CourseDto rejectCourse(UUID id, String reason);

    List<CourseDto> getCoursesByStatus(CourseStatus status); // admin
}