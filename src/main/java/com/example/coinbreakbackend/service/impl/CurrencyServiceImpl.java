package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.Currency;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.service.CurrencyService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.example.coinbreakbackend.util.HumanStandardToken;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;
import org.web3j.tx.ClientTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

import javax.crypto.Cipher;
import java.util.*;

@Service
public class CurrencyServiceImpl implements CurrencyService {
    private final CurrencyRepository currencyRepository;
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final Cipher cipher;
    private HumanStandardToken token;

    @Value("${cipher.password:#{''}}")
    private String cipherPassword;

    public CurrencyServiceImpl(CurrencyRepository currencyRepository, Web3j web3j, ContractGasProvider gasProvider, Cipher cipher) {
        this.currencyRepository = currencyRepository;
        this.web3j = web3j;
        this.gasProvider = gasProvider;
        this.cipher = cipher;
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
                return result.getBalance().longValue();
            } else {
                String contractAddress = System.getProperty(String.format("contract.erc20.%s", currency));
                token = HumanStandardToken.load(contractAddress, web3j, txManager, gasProvider);
                var balance = token.balanceOf(decryptedWallet);
                return balance.send().longValue();
            }
        }catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
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
            List<Map<String, Object>> resultList = new ArrayList<>();
            for (Currency currency: currencyList) {
                if(currency.getSymbol().equals("eth")) {
                    var result = new EthGetBalance();
                    result = this.web3j.ethGetBalance(decryptedWallet,
                                    DefaultBlockParameter.valueOf("latest"))
                            .sendAsync()
                            .get();
                    resultList.add(Collections.singletonMap(currency.getName(), result.getBalance().longValue()));
                } else {
                    String contractAddress = System.getProperty(String.format("contract.erc20.%s", currency.getSymbol()));
                    token = HumanStandardToken.load(contractAddress, web3j, txManager, gasProvider);
                    var balance = token.balanceOf(decryptedWallet);
                    resultList.add(Collections.singletonMap(currency.getName(), balance.send().longValue()));
                }
            }
            return resultList;
        }catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }
}
