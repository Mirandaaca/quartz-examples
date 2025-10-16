package com.example.demo.service;

import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    private final ClientRepository clientRepository;
    private final SchedulerService schedulerService;


    public void notifyClientTurn(Client client) {
        log.info("Sending notification to client: {} ({})", client.getName(), client.getEmail());

        try {
            // Simulate sending notification (email, SMS, push notification)
            boolean success = sendNotification(client, "YOUR_TURN",
                    "It's your turn! Please proceed to the counter.");

            if (success) {
                client.setStatus(Client.ClientStatus.NOTIFIED);
                client.setNotifiedAt(LocalDateTime.now());
                clientRepository.save(client);
                log.info("Successfully notified client: {}", client.getName());
            } else {
                // UC22: Schedule retry on failure
                log.warn("Failed to notify client: {}. Scheduling retry...", client.getName());
                schedulerService.scheduleNotificationRetry(client.getId(), "YOUR_TURN", 1);
            }

        } catch (Exception e) {
            log.error("Error sending notification to client: {}", client.getId(), e);
            schedulerService.scheduleNotificationRetry(client.getId(), "YOUR_TURN", 1);
        }
    }

    public void notifySuccessJoinQueue(Client client) {
        log.info("Sending join success notification to client: {}", client.getName());

        try {
            String message = String.format("You have successfully joined the queue '%s'. Your position is %d.",
                    client.getQueue().getName(), client.getPosition());

            sendNotification(client, "JOIN_SUCCESS", message);

        } catch (Exception e) {
            log.error("Error sending join success notification", e);
        }
    }

    public void notifyFailureJoinQueue(String email, String reason) {
        log.info("Sending join failure notification to: {}", email);

        try {
            String message = String.format("Failed to join queue. Reason: %s", reason);
            // Simulate sending notification
            log.info("Would send email to {} with message: {}", email, message);

        } catch (Exception e) {
            log.error("Error sending join failure notification", e);
        }
    }

    public boolean retryFailedNotification(Long clientId, String notificationType) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

        log.info("Retrying notification type {} for client: {}", notificationType, client.getName());

        return sendNotification(client, notificationType, "Retry notification");
    }

    private boolean sendNotification(Client client, String type, String message) {
        // Simulate "90%" success rate
        boolean success = Math.random() > 0.1;

        if (success) {
            log.info("[NOTIFICATION SENT] Type: {}, To: {} ({}), Message: {}",
                    type, client.getName(), client.getEmail(), message);
        } else {
            log.warn("[NOTIFICATION FAILED] Type: {}, To: {} ({})",
                    type, client.getName(), client.getEmail());
        }

        return success;
    }
}
