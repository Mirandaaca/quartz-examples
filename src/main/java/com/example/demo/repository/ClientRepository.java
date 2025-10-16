package com.example.demo.repository;

import com.example.demo.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    List<Client> findByQueueIdAndStatus(Long queueId, Client.ClientStatus status);
    List<Client> findByStatus(Client.ClientStatus status);
}
