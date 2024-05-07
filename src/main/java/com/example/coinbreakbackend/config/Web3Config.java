package com.example.coinbreakbackend.config;

import com.example.coinbreakbackend.util.HumanStandardToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.math.BigInteger;

@Component
public class Web3Config {

    @Value("${eth.endpoint.url:#{''}}")
    private String ethNode;

    @Bean(name = "ethInstance")
    public Web3j initWeb3j(){
        return Web3j.build(new HttpService(ethNode));
    }

    @Bean
    public ContractGasProvider initGasProvider(@Value("${eth.gas.limit:#{BigInteger.valueOf(9000000L)}}")BigInteger gasLimit,
                                               @Value("${eth.gas.limit:#{BigInteger.valueOf(4100000000L)}}")BigInteger gasPrice) {
        return new StaticGasProvider(gasPrice, gasLimit);
    }
}
