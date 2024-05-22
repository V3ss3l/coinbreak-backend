package com.example.coinbreakbackend.service;

import org.springframework.stereotype.Service;

public interface CurrencyService {
    Object balance(String currency, String wallet);
    Object balanceAll(String wallet);
}
