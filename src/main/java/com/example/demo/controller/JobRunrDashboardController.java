package com.example.demo.controller;

import com.example.demo.dto.SchedulerResponse;
import com.example.demo.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class JobRunrDashboardController {

    private final SchedulerService schedulerService;

    /**
     * Initialize recurring jobs
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, SchedulerResponse>> initializeJobs() {
        Map<String, SchedulerResponse> responses = new HashMap<>();

        SchedulerResponse waitingTimeResponse = schedulerService.scheduleWaitingTimeCalculator();
        responses.put("waitingTimeCalculator", waitingTimeResponse);

        SchedulerResponse cleanupResponse = schedulerService.scheduleQueueCleanup();
        responses.put("queueCleanup", cleanupResponse);

        log.info("Initialized all recurring jobs");

        return ResponseEntity.ok(responses);
    }

    /**
     * Cancel a job
     */
    @DeleteMapping("/jobs/{jobId}")
    public ResponseEntity<String> cancelJob(@PathVariable String jobId) {
        boolean success = schedulerService.cancelJob(jobId);

        if (success) {
            return ResponseEntity.ok("Job cancelled successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Delete recurring job
     */
    @DeleteMapping("/recurring-jobs/{jobId}")
    public ResponseEntity<String> deleteRecurringJob(@PathVariable String jobId) {
        boolean success = schedulerService.deleteRecurringJob(jobId);

        if (success) {
            return ResponseEntity.ok("Recurring job deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Redirect info to JobRunr dashboard
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, String>> getDashboardInfo() {
        Map<String, String> response = new HashMap<>();
        response.put("dashboard_url", "http://localhost:8000");
        response.put("message", "Visit the dashboard to see all jobs in real-time");
        response.put("features", "View scheduled/processing/succeeded/failed jobs, retry failed jobs, see execution logs");
        return ResponseEntity.ok(response);
    }
}
