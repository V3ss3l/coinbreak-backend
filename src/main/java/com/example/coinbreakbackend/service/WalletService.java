package com.example.coinbreakbackend.service;

public interface WalletService {
    Object restore(String password);
    Object generateSeed(Integer size, String language);
}
