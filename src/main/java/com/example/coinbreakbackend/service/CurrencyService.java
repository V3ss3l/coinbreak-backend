package com.example.coinbreakbackend.service;

public interface CurrencyService {
    Object balance(String currency, String wallet);
    Object balanceAll(String wallet);
}
