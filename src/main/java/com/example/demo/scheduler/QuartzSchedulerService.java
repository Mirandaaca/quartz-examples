package com.example.demo.scheduler;
import com.example.demo.jobs.NotificationJob;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QuartzSchedulerService {
    @Autowired
    private Scheduler scheduler;

    public String scheduleNotification(String message, int intervalInSeconds) throws SchedulerException {
        JobDetail jobDetail = JobBuilder.newJob(NotificationJob.class)
                .withIdentity("notificationJob", "group1")
                .usingJobData("message", message)
                .build();

        Trigger trigger = TriggerBuilder.newTrigger()
                .withIdentity("notificationTrigger", "group1")
                .startNow()
                .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                        .withIntervalInSeconds(intervalInSeconds)
                        .repeatForever())
                .build();

        scheduler.scheduleJob(jobDetail, trigger);
        return "Job programado con éxito. Se ejecutará cada " + intervalInSeconds + " segundos.";
    }
}
