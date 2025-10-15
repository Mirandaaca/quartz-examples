package com.example.demo.controller;

import com.example.demo.scheduler.QuartzSchedulerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.quartz.SchedulerException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    @Autowired
    private QuartzSchedulerService schedulerService;

    @PostMapping("/schedule")
    public String scheduleNotification(@RequestParam String message, @RequestParam int interval)
            throws SchedulerException {
        return schedulerService.scheduleNotification(message, interval);
    }
}
