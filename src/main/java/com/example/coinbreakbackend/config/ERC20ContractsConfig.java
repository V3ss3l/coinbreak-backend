package com.example.coinbreakbackend.config;

import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;


@Component
public class ERC20ContractsConfig {

    private final Web3j web3j;

    public ERC20ContractsConfig(Web3j web3j) {
        this.web3j = web3j;
    }
}
