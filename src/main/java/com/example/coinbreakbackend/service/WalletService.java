package com.example.coinbreakbackend.service;

import com.example.coinbreakbackend.model.Currency;
import com.example.coinbreakbackend.model.WalletDto;
import org.web3j.crypto.Credentials;

import java.util.Map;

public interface WalletService {
    Object generate(String password);
    Object deposit(Long amount, String currency);
    Object withdraw(Long amount, String currency, String toAddress);
    Object balance(String currency);
    Object restore(String password);
    Object generateSeed(Integer size, String language);
}
