package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.EthTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EthTransactionRepository extends JpaRepository<EthTransaction, Long> {
}
