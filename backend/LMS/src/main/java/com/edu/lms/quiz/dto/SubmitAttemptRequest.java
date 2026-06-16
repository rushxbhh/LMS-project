package com.edu.lms.quiz.dto;

import lombok.Data;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class SubmitAttemptRequest {
    // questionId → list of chosen option indices (0-based)
    private Map<UUID, List<Integer>> answers;
    private Integer timeTakenSeconds;
}