package com.example.coinbreakbackend.service;

import com.example.coinbreakbackend.model.WalletDto;
import org.web3j.crypto.Credentials;

public interface WalletService {
    WalletDto generate(String password);

    void deposit();

    void withdraw();

    void balance();

    Credentials restore(String mnemonic, String password);
}
