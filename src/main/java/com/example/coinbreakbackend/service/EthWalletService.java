package com.example.coinbreakbackend.service;

public interface EthWalletService extends WalletService{
    Object generate(String password, String seed);
    Object deposit(Long amount, String currency);
    Object withdraw(Long amount, String currency, String toAddress);
}
