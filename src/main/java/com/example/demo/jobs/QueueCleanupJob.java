package com.example.demo.jobs;

import com.example.demo.entity.Queue;
import com.example.demo.repository.QueueRepository;
import com.example.demo.service.QueueService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
public class QueueCleanupJob implements Job {
    @Autowired
    private QueueRepository queueRepository;

    @Autowired
    private QueueService queueService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing WaitingTimeCalculatorJob at {}", LocalDateTime.now());

        try {
            List<Queue> activeQueues = queueRepository.findByStatus(Queue.QueueStatus.ACTIVE);
            log.info("Found {} active queues to process", activeQueues.size());

            for (Queue queue : activeQueues) {
                queueService.recalculateWaitingTimes(queue.getId());
                log.debug("Recalculated waiting times for queue: {}", queue.getName());
            }

            log.info("Successfully completed waiting time calculation for all queues");

        } catch (Exception e) {
            log.error("Error executing WaitingTimeCalculatorJob", e);
            throw new JobExecutionException(e);
        }
    }
}
