package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.entity.Queue;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.QueueRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {
    private final QueueRepository queueRepository;
    private final ClientRepository clientRepository;

    @Transactional
    public void recalculateWaitingTimes(Long queueId) {
        Queue queue = queueRepository.findById(queueId)
                .orElseThrow(() -> new RuntimeException("Queue not found: " + queueId));

        List<Client> waitingClients = clientRepository
                .findByQueueIdAndStatus(queueId, Client.ClientStatus.WAITING);

        int avgServiceTime = queue.getAverageServiceTimeMinutes() != null
                ? queue.getAverageServiceTimeMinutes()
                : 10; // default 10 minutes

        for (int i = 0; i < waitingClients.size(); i++) {
            Client client = waitingClients.get(i);
            int estimatedWaitMinutes = (i + 1) * avgServiceTime;
            log.debug("Client {} at position {} - estimated wait: {} minutes",
                    client.getName(), i + 1, estimatedWaitMinutes);
        }

        queue.setUpdatedAt(LocalDateTime.now());
        queueRepository.save(queue);
    }

    @Transactional
    public int cleanExpiredClients() {
        List<Client> expiredClients = clientRepository.findByStatus(Client.ClientStatus.WAITING);
        LocalDateTime expirationThreshold = LocalDateTime.now().minusMinutes(30);

        int cleanedCount = 0;
        for (Client client : expiredClients) {
            if (client.getJoinedAt().isBefore(expirationThreshold) &&
                    client.getNotifiedAt() == null) {
                client.setStatus(Client.ClientStatus.EXPIRED);
                clientRepository.save(client);
                cleanedCount++;
            }
        }

        log.info("Cleaned {} expired clients", cleanedCount);
        return cleanedCount;
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
