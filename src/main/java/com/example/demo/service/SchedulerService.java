package com.example.demo.service;

import com.example.demo.dto.PostponeClientRequest;
import com.example.demo.dto.SchedulerResponse;
import com.example.demo.jobs.NotificationRetryJob;
import com.example.demo.jobs.PostponedClientJob;
import com.example.demo.jobs.QueueCleanupJob;
import com.example.demo.jobs.WaitingTimeCalculatorJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Slf4j
@Service
@RequiredArgsConstructor
public class SchedulerService {
    private final Scheduler scheduler;

    public SchedulerResponse schedulePostponeClient(PostponeClientRequest request) {
        try {
            String jobName = "postpone-client-" + request.getClientId();
            String jobGroup = "postponed-clients";

            JobDetail jobDetail = JobBuilder.newJob(PostponedClientJob.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData("clientId", request.getClientId())
                    .usingJobData("queueId", request.getQueueId())
                    .build();

            LocalDateTime triggerTime = LocalDateTime.now().plusMinutes(request.getPostponeMinutes());
            Date triggerDate = Date.from(triggerTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "-trigger", jobGroup)
                    .startAt(triggerDate)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled postpone job for client {} to run at {}",
                    request.getClientId(), triggerTime);

            return SchedulerResponse.builder()
                    .jobName(jobName)
                    .jobGroup(jobGroup)
                    .message("Client postponed successfully")
                    .scheduledTime(LocalDateTime.now())
                    .triggerTime(triggerTime)
                    .success(true)
                    .build();

        } catch (SchedulerException e) {
            log.error("Error scheduling postpone client job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule postpone job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    public SchedulerResponse scheduleWaitingTimeCalculator() {
        try {
            String jobName = "waiting-time-calculator";
            String jobGroup = "recurring-jobs";

            JobDetail jobDetail = JobBuilder.newJob(WaitingTimeCalculatorJob.class)
                    .withIdentity(jobName, jobGroup)
                    .storeDurably()
                    .build();

            // Cron: Every 5 minutes
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "-trigger", jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * * * ?"))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled WaitingTimeCalculator job with cron: 0 */5 * * * ?");

            return SchedulerResponse.builder()
                    .jobName(jobName)
                    .jobGroup(jobGroup)
                    .message("Waiting time calculator scheduled successfully")
                    .scheduledTime(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (SchedulerException e) {
            log.error("Error scheduling waiting time calculator job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    public SchedulerResponse scheduleQueueCleanup() {
        try {
            String jobName = "queue-cleanup";
            String jobGroup = "maintenance-jobs";

            JobDetail jobDetail = JobBuilder.newJob(QueueCleanupJob.class)
                    .withIdentity(jobName, jobGroup)
                    .storeDurably()
                    .build();

            // Cron: Every day at 2:00 AM
            CronTrigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "-trigger", jobGroup)
                    .withSchedule(CronScheduleBuilder.cronSchedule("0 0 2 * * ?"))
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled QueueCleanup job with cron: 0 0 2 * * ?");

            return SchedulerResponse.builder()
                    .jobName(jobName)
                    .jobGroup(jobGroup)
                    .message("Queue cleanup scheduled successfully")
                    .scheduledTime(LocalDateTime.now())
                    .success(true)
                    .build();

        } catch (SchedulerException e) {
            log.error("Error scheduling queue cleanup job", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule job: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }

    public SchedulerResponse scheduleNotificationRetry(Long clientId, String notificationType, int retryCount) {
        try {
            String jobName = "notification-retry-" + clientId + "-" + retryCount;
            String jobGroup = "notification-retries";

            JobDetail jobDetail = JobBuilder.newJob(NotificationRetryJob.class)
                    .withIdentity(jobName, jobGroup)
                    .usingJobData("clientId", clientId)
                    .usingJobData("notificationType", notificationType)
                    .usingJobData("retryCount", retryCount)
                    .build();

            // Exponential backoff: 1min, 5min, 15min
            int delayMinutes = (int) Math.pow(5, retryCount - 1);
            LocalDateTime triggerTime = LocalDateTime.now().plusMinutes(delayMinutes);
            Date triggerDate = Date.from(triggerTime.atZone(ZoneId.systemDefault()).toInstant());

            Trigger trigger = TriggerBuilder.newTrigger()
                    .withIdentity(jobName + "-trigger", jobGroup)
                    .startAt(triggerDate)
                    .build();

            scheduler.scheduleJob(jobDetail, trigger);

            log.info("Scheduled notification retry for client {} (attempt {}) to run in {} minutes",
                    clientId, retryCount, delayMinutes);

            return SchedulerResponse.builder()
                    .jobName(jobName)
                    .jobGroup(jobGroup)
                    .message("Notification retry scheduled")
                    .scheduledTime(LocalDateTime.now())
                    .triggerTime(triggerTime)
                    .success(true)
                    .build();

        } catch (SchedulerException e) {
            log.error("Error scheduling notification retry", e);
            return SchedulerResponse.builder()
                    .message("Failed to schedule retry: " + e.getMessage())
                    .success(false)
                    .build();
        }
    }
}
