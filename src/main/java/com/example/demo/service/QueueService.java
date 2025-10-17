package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Queue;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.QueueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;
    private final ClientRepository clientRepository;

    /**
     * Recurring job: Recalculate waiting times for all active queues
     * Called by JobRunr every 5 minutes
     */
    @Job(name = "Recalculate Waiting Times for All Queues")
    @Transactional
    public void recalculateAllWaitingTimes() {
        log.info("Executing recalculateAllWaitingTimes at {}", LocalDateTime.now());

        try {
            List<Queue> activeQueues = queueRepository.findByStatus(Queue.QueueStatus.ACTIVE);
            log.info("Found {} active queues to process", activeQueues.size());

            for (Queue queue : activeQueues) {
                recalculateWaitingTimes(queue.getId());
                log.debug("Recalculated waiting times for queue: {}", queue.getName());
            }

            log.info("Successfully completed waiting time calculation for all queues");

        } catch (Exception e) {
            log.error("Error recalculating waiting times", e);
            throw e;
        }
    }

    @Transactional
    public void recalculateWaitingTimes(Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue not found: " + queueId));

        List<Client> waitingClients = clientRepository
                .findByQueueIdAndStatus(queueId, Client.ClientStatus.WAITING);

        int avgServiceTime = queue.getAverageServiceTimeMinutes() != null
                ? queue.getAverageServiceTimeMinutes()
                : 10;

        for (int i = 0; i < waitingClients.size(); i++) {
            Client client = waitingClients.get(i);
            int estimatedWaitMinutes = (i + 1) * avgServiceTime;
            log.debug("Client {} at position {} - estimated wait: {} minutes",
                    client.getName(), i + 1, estimatedWaitMinutes);
        }

        queue.setUpdatedAt(LocalDateTime.now());
        queueRepository.save(queue);
    }

    /**
     * Recurring job: Clean expired clients (not attended for 30+ minutes)
     * Called by JobRunr based on cron schedule
     */
    @Job(name = "Clean Expired Clients")
    @Transactional
    public int cleanExpiredClients() {
        log.info("Executing cleanExpiredClients at {}", LocalDateTime.now());

        try {
            List<Client> expiredClients = clientRepository.findByStatus(Client.ClientStatus.WAITING);
            LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(30);

            int cleanedCount = 0;
            for (Client client : expiredClients) {
                if (client.getJoinedAt().isBefore(expirationThreshold) &&
                        client.getNotifiedAt() == null) {
                    client.setStatus(Client.ClientStatus.EXPIRED);
                    clientRepository.save(client);
                    cleanedCount++;
                    log.debug("Marked client {} as EXPIRED", client.getName());
                }
            }

            log.info("Cleaned {} expired clients", cleanedCount);
            return cleanedCount;

        } catch (Exception e) {
            log.error("Error cleaning expired clients", e);
            throw e;
        }
    }

    public Queue getQueue(Long queueId) {
        return queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue not found: " + queueId));
    }

    public List<Queue> getAllQueues() {
        return queueRepository.findAll();
    }

    public List<Queue> getActiveQueues() {
        return queueRepository.findByStatus(Queue.QueueStatus.ACTIVE);
    }
}
