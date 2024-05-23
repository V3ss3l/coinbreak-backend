package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long> {
    TransactionType getTransactionTypeByName(String name);
}
