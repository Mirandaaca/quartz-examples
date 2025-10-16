package com.example.demo.controller;

import com.example.demo.dto.PostponeClientRequest;
import com.example.demo.dto.SchedulerResponse;
import com.example.demo.entity.Client;
import com.example.demo.entity.Queue;
import com.example.demo.repository.ClientRepository;
import com.example.demo.repository.QueueRepository;
import com.example.demo.service.NotificationService;
import com.example.demo.service.QueueService;
import com.example.demo.service.SchedulerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/queues")
@RequiredArgsConstructor
public class QueueController {

    private final QueueService queueService;
    private final SchedulerService schedulerService;
    private final NotificationService notificationService;
    private final QueueRepository queueRepository;
    private final ClientRepository clientRepository;


    @GetMapping
    public ResponseEntity<List<Queue>> getAllQueues() {
        return ResponseEntity.ok(queueService.getAllQueues());
    }

    @GetMapping("/{queueId}")
    public ResponseEntity<Queue> getQueue(@PathVariable Long queueId) {
        return ResponseEntity.ok(queueService.getQueue(queueId));
    }

    @GetMapping("/{queueId}/clients")
    public ResponseEntity<List<Client>> getClientsInQueue(@PathVariable Long queueId) {
        List<Client> clients = clientRepository.findByQueueIdAndStatus(
                queueId, Client.ClientStatus.WAITING);
        return ResponseEntity.ok(clients);
    }

    @PostMapping("/{queueId}/join")
    public ResponseEntity<Client> joinQueue(
            @PathVariable Long queueId,
            @RequestParam String name,
            @RequestParam String email,
            @RequestParam String phone) {

        Queue queue = queueService.getQueue(queueId);

        if (queue.getStatus() != Queue.QueueStatus.ACTIVE) {
            notificationService.notifyFailureJoinQueue(email, "Queue is not active");
            return ResponseEntity.badRequest().build();
        }

        // Get current position
        List<Client> currentClients = clientRepository
                .findByQueueIdAndStatus(queueId, Client.ClientStatus.WAITING);
        int position = currentClients.size() + 1;

        Client client = Client.builder()
                .name(name)
                .email(email)
                .phone(phone)
                .queue(queue)
                .position(position)
                .status(Client.ClientStatus.WAITING)
                .joinedAt(LocalDateTime.now())
                .build();

        client = clientRepository.save(client);

        notificationService.notifySuccessJoinQueue(client);

        log.info("Client {} joined queue {} at position {}", name, queue.getName(), position);

        return ResponseEntity.ok(client);
    }

    @PostMapping("/postpone")
    public ResponseEntity<SchedulerResponse> postponeClient(
            @Valid @RequestBody PostponeClientRequest request) {

        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new RuntimeException("Client not found"));

        // Update client status
        client.setStatus(Client.ClientStatus.POSTPONED);
        client.setPostponedAt(LocalDateTime.now());
        client.setPostponeMinutes(request.getPostponeMinutes());
        clientRepository.save(client);

        SchedulerResponse response = schedulerService.schedulePostponeClient(request);

        log.info("Client {} postponed for {} minutes", client.getName(), request.getPostponeMinutes());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{queueId}/attend-next")
    public ResponseEntity<Client> attendNextClient(@PathVariable Long queueId) {
        List<Client> waitingClients = clientRepository
                .findByQueueIdAndStatus(queueId, Client.ClientStatus.WAITING);

        if (waitingClients.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Get next client (first in line)
        Client nextClient = waitingClients.get(0);
        nextClient.setStatus(Client.ClientStatus.ATTENDING);
        nextClient = clientRepository.save(nextClient);

        log.info("Now attending client: {} in queue: {}", nextClient.getName(), queueId);

        return ResponseEntity.ok(nextClient);
    }

    @PostMapping("/clients/{clientId}/attended")
    public ResponseEntity<Client> markClientAttended(@PathVariable Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setStatus(Client.ClientStatus.ATTENDED);
        client = clientRepository.save(client);

        log.info("Client {} marked as attended", client.getName());

        return ResponseEntity.ok(client);
    }

    @DeleteMapping("/clients/{clientId}")
    public ResponseEntity<Void> leaveQueue(@PathVariable Long clientId) {
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("Client not found"));

        client.setStatus(Client.ClientStatus.CANCELLED);
        clientRepository.save(client);

        log.info("Client {} left the queue", client.getName());

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{queueId}/status")
    public ResponseEntity<QueueStatusResponse> getQueueStatus(@PathVariable Long queueId) {
        Queue queue = queueService.getQueue(queueId);
        List<Client> waitingClients = clientRepository
                .findByQueueIdAndStatus(queueId, Client.ClientStatus.WAITING);

        int totalWaiting = waitingClients.size();
        int avgServiceTime = queue.getAverageServiceTimeMinutes() != null
                ? queue.getAverageServiceTimeMinutes()
                : 10;
        int estimatedWaitTime = totalWaiting * avgServiceTime;

        QueueStatusResponse response = QueueStatusResponse.builder()
                .queueId(queueId)
                .queueName(queue.getName())
                .status(queue.getStatus())
                .totalWaiting(totalWaiting)
                .averageServiceTimeMinutes(avgServiceTime)
                .estimatedWaitTimeMinutes(estimatedWaitTime)
                .build();

        return ResponseEntity.ok(response);
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class QueueStatusResponse {
        private Long queueId;
        private String queueName;
        private Queue.QueueStatus status;
        private int totalWaiting;
        private int averageServiceTimeMinutes;
        private int estimatedWaitTimeMinutes;
    }
}