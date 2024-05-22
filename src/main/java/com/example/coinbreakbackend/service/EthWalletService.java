package com.example.coinbreakbackend.service;

public interface EthWalletService {
    Object generate(String password, String seed);
    Object deposit(Long amount, String currency);
    Object withdraw(Long amount, String currency, String toAddress);
    Object restore(String password);
    Object generateSeed(Integer size, String language);
}
