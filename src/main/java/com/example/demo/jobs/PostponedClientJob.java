package com.example.demo.jobs;

import com.example.demo.entity.Client;
import com.example.demo.repository.ClientRepository;
import com.example.demo.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class PostponedClientJob implements Job {
    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private NotificationService notificationService;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        log.info("Executing PostponedClientJob at {}", LocalDateTime.now());

        Long clientId = context.getJobDetail().getJobDataMap().getLong("clientId");
        Long queueId = context.getJobDetail().getJobDataMap().getLong("queueId");

        try {
            Client client = clientRepository.findById(clientId)
                    .orElseThrow(() -> new RuntimeException("Client not found: " + clientId));

            log.info("Processing postponed client: {} in queue: {}", client.getName(), queueId);

            // Update client status
            client.setStatus(Client.ClientStatus.NOTIFIED);
            client.setNotifiedAt(LocalDateTime.now());
            clientRepository.save(client);

            // Trigger UC13: Notify next client turn
            notificationService.notifyClientTurn(client);

            log.info("Successfully notified postponed client: {}", client.getName());

        } catch (Exception e) {
            log.error("Error executing PostponedClientJob for client: {}", clientId, e);
            throw new JobExecutionException(e);
        }
    }
}
