package com.example.demo.jobs;

import com.example.demo.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class NotificationRetryJob implements Job {
    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing NotificationRetryJob at {}", LocalDateTime.now());

        Long clientId = context.getJobDetail().getJobDataMap().getLong("clientId");
        String notificationType = context.getJobDetail().getJobDataMap().getString("notificationType");
        int retryCount = context.getJobDetail().getJobDataMap().getInt("retryCount");

        try {
            log.info("Retrying notification for client: {}, type: {}, attempt: {}",
                    clientId, notificationType, retryCount);

            boolean success = notificationService.retryFailedNotification(clientId, notificationType);

            if (success) {
                log.info("Successfully sent notification on retry attempt {}", retryCount);
            } else {
                log.warn("Failed to send notification on retry attempt {}", retryCount);
            }

        } catch (Exception e) {
            log.error("Error executing NotificationRetryJob for client: {}", clientId, e);
            throw new JobExecutionException(e);
        }
    }
}
