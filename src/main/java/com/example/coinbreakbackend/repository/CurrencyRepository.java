package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
    Currency getCurrencyBySymbol(String symbol);
    Currency getCurrencyByName(String name);
}
