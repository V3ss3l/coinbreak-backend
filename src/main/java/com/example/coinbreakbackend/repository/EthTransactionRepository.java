package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.EthTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EthTransactionRepository extends JpaRepository<EthTransaction, Long> {
    EthTransaction getEthTransactionBySender(String sender);
}
