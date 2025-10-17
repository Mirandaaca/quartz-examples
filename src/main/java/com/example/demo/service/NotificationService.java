package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.jobrunr.jobs.annotations.Job;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ClientRepository clientRepository;

    /**
     * UC13: Notify postponed client
     * @Job annotation helps JobRunr track this method
     * Automatic retry with exponential backoff (3 retries)
     */
    @Job(name = "Notify Postponed Client", retries = 3)
    @Transactional
    public void notifyPostponedClient(Long clientId, Long queueId) {
        log.info("Executing notifyPostponedClient for client: {} in queue: {}", clientId, queueId);

        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

            log.info("Processing postponed client: {} in queue: {}", client.getName(), queueId);

            // Update client status
            client.setStatus(Client.ClientStatus.NOTIFIED);
            client.setNotifiedAt(LocalDateTime.now());
            clientRepository.save(client);

            // Send notification (simulate)
            boolean success = sendNotification(client, "YOUR_TURN",
                    "It's your turn! Please proceed to the counter.");

            if (success) {
                log.info("Successfully notified postponed client: {}", client.getName());
            } else {
                // JobRunr will automatically retry (up to 3 times with exponential backoff)
                throw new RuntimeException("Failed to send notification - JobRunr will retry");
            }

        } catch (Exception e) {
            log.error("Error notifying postponed client: {}", clientId, e);
            throw e; // JobRunr handles retry automatically
        }
    }

    /**
     * UC21: Notify success join to queue
     */
    @Job(name = "Notify Join Success")
    public void notifySuccessJoinQueue(Client client) {
        log.info("Sending join success notification to client: {}", client.getName());

        String message = String.format("You have successfully joined the queue '%s'. Your position is %d.",
                client.getQueue().getName(), client.getPosition());

        sendNotification(client, "JOIN_SUCCESS", message);
    }

    /**
     * UC22: Notify failure join to queue
     */
    @Job(name = "Notify Join Failure")
    public void notifyFailureJoinQueue(String email, String reason) {
        log.info("Sending join failure notification to: {}", email);

        String message = String.format("Failed to join queue. Reason: %s", reason);
        log.info("[NOTIFICATION] Would send email to {} with message: {}", email, message);
    }

    /**
     * Simulate sending notification
     * 90% success rate to demonstrate JobRunr's retry mechanism
     */
    private boolean sendNotification(Client client, String type, String message) {
        // Simulate 90% success rate
        boolean success = Math.random() > 0.1;

        if (success) {
            log.info("[NOTIFICATION SENT] Type: {}, To: {} ({}), Message: {}",
                    type, client.getName(), client.getEmail(), message);
        } else {
            log.warn("[NOTIFICATION FAILED] Type: {}, To: {} ({}) - Will retry automatically",
                    type, client.getName(), client.getEmail());
        }

        return success;
    }
}
