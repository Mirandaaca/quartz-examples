package com.example.demo.controller;

import com.example.demo.dto.SchedulerResponse;
import com.example.demo.service.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
public class SchedulerController {

    private final SchedulerService schedulerService;
    private final Scheduler scheduler;


    @PostMapping("/init")
    public ResponseEntity<Map<String, SchedulerResponse>> initializeJobs() {
        Map<String, SchedulerResponse> responses = new HashMap<>();

        // Schedule waiting time calculator (every 5 minutes)
        SchedulerResponse waitingTimeResponse = schedulerService.scheduleWaitingTimeCalculator();
        responses.put("waitingTimeCalculator", waitingTimeResponse);

        // Schedule queue cleanup (daily at 2 AM)
        SchedulerResponse cleanupResponse = schedulerService.scheduleQueueCleanup();
        responses.put("queueCleanup", cleanupResponse);

        log.info("Initialized all recurring jobs");

        return ResponseEntity.ok(responses);
    }

    @GetMapping("/jobs")
    public ResponseEntity<List<JobInfo>> getAllJobs() throws SchedulerException {
        List<JobInfo> jobs = new ArrayList<>();

        for (String groupName : scheduler.getJobGroupNames()) {
            for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);

                for (Trigger trigger : triggers) {
                    JobInfo jobInfo = JobInfo.builder()
                            .jobName(jobKey.getName())
                            .jobGroup(jobKey.getGroup())
                            .jobClass(jobDetail.getJobClass().getSimpleName())
                            .triggerState(scheduler.getTriggerState(trigger.getKey()).name())
                            .nextFireTime(trigger.getNextFireTime())
                            .previousFireTime(trigger.getPreviousFireTime())
                            .build();
                    jobs.add(jobInfo);
                }
            }
        }

        return ResponseEntity.ok(jobs);
    }

    @PostMapping("/jobs/{jobName}/pause")
    public ResponseEntity<String> pauseJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "postponed-clients") String jobGroup) throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.pauseJob(jobKey);

        log.info("Paused job: {} in group: {}", jobName, jobGroup);

        return ResponseEntity.ok("Job paused successfully");
    }


    @PostMapping("/jobs/{jobName}/resume")
    public ResponseEntity<String> resumeJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "postponed-clients") String jobGroup) throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.resumeJob(jobKey);

        log.info("Resumed job: {} in group: {}", jobName, jobGroup);

        return ResponseEntity.ok("Job resumed successfully");
    }

    @DeleteMapping("/jobs/{jobName}")
    public ResponseEntity<String> deleteJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "postponed-clients") String jobGroup) throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        boolean deleted = scheduler.deleteJob(jobKey);

        if (deleted) {
            log.info("Deleted job: {} in group: {}", jobName, jobGroup);
            return ResponseEntity.ok("Job deleted successfully");
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/jobs/{jobName}/trigger")
    public ResponseEntity<String> triggerJob(
            @PathVariable String jobName,
            @RequestParam(defaultValue = "recurring-jobs") String jobGroup) throws SchedulerException {

        JobKey jobKey = new JobKey(jobName, jobGroup);
        scheduler.triggerJob(jobKey);

        log.info("Manually triggered job: {} in group: {}", jobName, jobGroup);

        return ResponseEntity.ok("Job triggered successfully");
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class JobInfo {
        private String jobName;
        private String jobGroup;
        private String jobClass;
        private String triggerState;
        private Date nextFireTime;
        private Date previousFireTime;
    }
}
