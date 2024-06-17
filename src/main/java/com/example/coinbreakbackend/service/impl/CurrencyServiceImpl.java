package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.*;
import com.example.coinbreakbackend.model.Currency;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.service.CurrencyService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.example.coinbreakbackend.util.HumanStandardToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import javax.crypto.Cipher;
import java.math.BigInteger;
import java.util.*;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final Cipher cipher;
    private HumanStandardToken token;
    private ObjectMapper objectMapper;

    @Value("${cipher.password:#{''}}")
    private String cipherPassword;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository,
                               Web3j web3j,
                               ContractGasProvider gasProvider,
                               Cipher cipher,
                               ObjectMapper objectMapper) {
        this.currencyRepository = currencyRepository;
        this.web3j = web3j;
        this.gasProvider = gasProvider;
        this.cipher = cipher;
        this.objectMapper = objectMapper;
    }

    /**
     * метод для получения баланса по определенной валюте
     */
    @Override
    public Object balance(String currency, String wallet) {
        try{
            var decryptedWallet = CoinWalletUtils.decrypt(wallet, cipher, cipherPassword);
            TransactionManager txManager = new ClientTransactionManager(web3j, decryptedWallet);
            Currency entityCurr = currencyRepository.getCurrencyByName(currency);
            if(entityCurr.getSymbol().equals("ETH")){
                var result = new EthGetBalance();
                result = this.web3j.ethGetBalance(decryptedWallet,
                                DefaultBlockParameter.valueOf("latest"))
                        .sendAsync()
                        .get();
                return Map.of("value", result.getBalance().longValue());
            } else {
                String contractAddress = System.getProperty(String.format("contract.erc20.%s", currency));
                token = HumanStandardToken.load(contractAddress, web3j, txManager, gasProvider);
                var balance = token.balanceOf(decryptedWallet);
                return Map.of("value", balance.send().longValue());
            }
        }catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }

    /**
     * метод для получения баланса всех активов данного кошелька
     */
    @Override
    public Object balanceAll(String wallet) {
        try{
            var decryptedWallet = CoinWalletUtils.decrypt(wallet, cipher, cipherPassword);
            TransactionManager txManager = new ClientTransactionManager(web3j, decryptedWallet);
            List<Currency> currencyList = currencyRepository.findAll();
            List<String> resultList = new ArrayList<>();
            for (Currency currency: currencyList) {
                BalanceDto balanceDto  = BalanceDto.builder()
                        .currency(currency)
                        .wallet(decryptedWallet)
                        .build();
                if(currency.getSymbol().equals("eth")) {
                    var result = new EthGetBalance();
                    result = this.web3j.ethGetBalance(decryptedWallet,
                                    DefaultBlockParameter.valueOf("latest"))
                            .sendAsync()
                            .get();
                    balanceDto.setAmount(result.getBalance());
                } else {
                    String contractAddress = System.getProperty(String.format("contract.erc20.%s", currency.getSymbol()));
                    token = HumanStandardToken.load(contractAddress, web3j, txManager, gasProvider);
                    var balance = token.balanceOf(decryptedWallet);
                    balanceDto.setAmount(BigInteger.valueOf(balance.send().longValue()));
                }
                byte[] salt = CoinWalletUtils.generateSalt();
                CoinWalletUtils.initToEncryptModeCipher(cipher, cipherPassword, salt);
                String str = objectMapper.writeValueAsString(balanceDto);
                String encrypted = CoinWalletUtils.encrypt(str, cipher, salt);
                resultList.add(encrypted);
            }
            return resultList;
        }catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }
}
