package com.example.coinbreakbackend.service;

import com.example.coinbreakbackend.model.Currency;
import com.example.coinbreakbackend.model.WalletDto;
import org.web3j.crypto.Credentials;

import java.util.Map;

public interface WalletService {
    WalletDto generate(String password);
    void deposit(Long amount, String currency);
    void withdraw(Long amount, String currency, String toAddress);
    Long balance(String currency);
    Credentials restore(String password);
    String generateSeed(Integer size, String language);
}
