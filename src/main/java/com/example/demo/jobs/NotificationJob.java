package com.example.demo.jobs;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;

public class NotificationJob implements Job {
    private static final Logger logger = LoggerFactory.getLogger(NotificationJob.class);

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        String message = (String) context.getJobDetail().getJobDataMap().get("message");
        logger.info("📢 Notificación: {} - Ejecutada en {}", message, LocalDateTime.now());
    }
}
