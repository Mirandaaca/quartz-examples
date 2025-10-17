package com.example.demo.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerResponse {
    private String jobId;
    private String jobName;
    private String jobGroup;
    private String message;
    private LocalDateTime scheduledTime;
    private LocalDateTime triggerTime;
    private boolean success;
}
