package com.example.demo.service;

import com.example.demo.dto.PostponeClientRequest;
import com.example.demo.dto.SchedulerResponse;
import org.jobrunr.scheduling.BackgroundJob;
import org.jobrunr.jobs.JobId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {

    private final NotificationService notificationService;
    private final QueueService queueService;

    public SchedulerResponse schedulePostponeClient(PostponeClientRequest request) {
        try {
            Instant triggerTime = Instant.now().plus(Duration.ofMinutes(request.getPostponeMinutes()));

            // JobRunr: Super simple API with lambda!
            // Automatic retry with exponential backoff
            JobId jobId = BackgroundJob.schedule(
                    triggerTime,
                    () -> notificationService.notifyPostponedClient(
                            request.getClientId(),
                            request.getQueueId()
                    )
            );

            log.info("Scheduled postpone job {} for client {} to run at {}",
                    jobId, request.getClientId(), triggerTime);

            return SchedulerResponse.builder()
                    .jobId(jobId.toString())
                    .jobName("postpone-client-" + request.getClientId())
                    .message("Client postponed successfully")
                    .scheduledTime(LocalDateTime.now())
                    .triggerTime(LocalDateTime.ofInstant(triggerTime, ZoneId.systemDefault()))
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error scheduling postpone client job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule postpone job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    /**
     * Schedule recurring job: Calculate waiting times every 5 minutes
     */
    public SchedulerResponse scheduleWaitingTimeCalculator() {
        try {
            // JobRunr recurring job with fixed interval
            BackgroundJob.scheduleRecurrently(
                    "waiting-time-calculator",
                    Duration.ofMinutes(5),
                    () -> queueService.recalculateAllWaitingTimes()
            );

            log.info("Scheduled recurring WaitingTimeCalculator job (every 5 minutes)");

            return SchedulerResponse.builder()
                    .jobName("waiting-time-calculator")
                    .jobGroup("recurring-jobs")
                    .message("Waiting time calculator scheduled successfully")
                    .scheduledTime(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error scheduling waiting time calculator job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    /**
     * Schedule daily cleanup job using cron expression
     * Runs every day at 2:00 AM
     */
    public SchedulerResponse scheduleQueueCleanup() {
        try {
            // JobRunr with cron expression
            BackgroundJob.scheduleRecurrently(
                    "queue-cleanup",
                    "0 0 2 * * ?",  // Cron: Every day at 2 AM
                    () -> queueService.cleanExpiredClients()
            );

            log.info("Scheduled QueueCleanup job (daily at 2:00 AM)");

            return SchedulerResponse.builder()
                    .jobName("queue-cleanup")
                    .jobGroup("maintenance-jobs")
                    .message("Queue cleanup scheduled successfully")
                    .scheduledTime(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (Exception e) {
            log.error("Error scheduling queue cleanup job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    /**
     * Delete/Cancel a scheduled job by ID
     */
    public boolean cancelJob(String jobId) {
        try {
            BackgroundJob.delete(JobId.parse(jobId));
            log.info("Cancelled job: {}", jobId);
            return true;
        } catch (Exception e) {
            log.error("Error cancelling job: {}", jobId, e);
            return false;
        }
    }

    /**
     * Delete recurring job by ID
     */
    public boolean deleteRecurringJob(String jobId) {
        try {
            org.jobrunr.scheduling.BackgroundJobRequest.deleteRecurringJob(jobId);
            log.info("Deleted recurring job: {}", jobId);
            return true;
        } catch (Exception e) {
            log.error("Error deleting recurring job: {}", jobId, e);
            return false;
        }
    }
}
