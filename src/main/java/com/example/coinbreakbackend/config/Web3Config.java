package com.example.coinbreakbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.http.HttpService;

@Component
public class Web3Config {

    @Value("${eth.endpoint.url:#{''}}")
    private String ethNode;

    @Bean(name = "ethInstance")
    public Web3j initWeb3j(){
        return Web3j.build(new HttpService(ethNode));
    }

}
