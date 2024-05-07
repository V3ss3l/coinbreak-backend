package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.WalletDto;
import com.example.coinbreakbackend.model.WalletEntity;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.repository.WalletRepository;
import com.example.coinbreakbackend.service.WalletService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.example.coinbreakbackend.util.HumanStandardToken;
import com.fasterxml.jackson.databind.util.ExceptionUtil;
import io.reactivex.exceptions.Exceptions;
import org.apache.el.util.ExceptionUtils;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.tx.Transfer;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final Cipher cipher;

    private Credentials credentials;
    private HumanStandardToken token;

    public WalletServiceImpl(WalletRepository walletRepository,
                             CurrencyRepository currencyRepository,
                             Web3j web3j,
                             ContractGasProvider gasProvider,
                             Cipher cipher) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.web3j = web3j;
        this.gasProvider = gasProvider;
        this.cipher = cipher;
    }

    @Override
    public Object generate(String password) {
        try {
            String decodedPassword = CoinWalletUtils.decodeInfo(password);
            String seed = CoinWalletUtils.generateSeed(12, "english");
            Bip39Wallet wallet = WalletUtils.generateBip39WalletFromMnemonic(decodedPassword, seed, new File("src/main/resources/wallet"));
            Credentials credentials = WalletUtils.loadBip39Credentials(decodedPassword, seed);
            WalletEntity entity = WalletEntity.builder()
                    .publicKey(credentials.getAddress())
                    .seed(CoinWalletUtils.encodeInfo(seed))
                    .password(decodedPassword)
                    .walletFileName(wallet.getFilename())
                    .build();
            walletRepository.save(entity);
            this.credentials = credentials;
            WalletDto dto = WalletDto.builder()
                    .id(entity.getId())
                    .publicKey(entity.getPublicKey())
                    .seed(seed)
                    .build();
            byte[] serialized = CoinWalletUtils.serialize(dto);
            CoinWalletUtils.initToEncryptModeCipher(cipher, decodedPassword);
            var encrypted = cipher.doFinal(serialized);
            return Collections.singletonMap("value", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }

    @Override
    public Object deposit(Long amount, String currency) {
        try {
            if(Objects.isNull(credentials)) return null;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            RawTransaction rawTransaction = null;
            if(currency.equals("eth")) {
                BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
                rawTransaction = RawTransaction.createEtherTransaction(nonce, gasProvider.getGasPrice(), gasProvider.getGasLimit(),
                        credentials.getAddress(), value);
            }
//            else{
//                BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.WEI).toBigInteger();
//                rawTransaction = RawTransaction.createContractTransaction(nonce, gasProvider.getGasPrice(), gasProvider.getGasLimit(),
//                        credentials.getAddress(), value);
//            }
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
            return ethSendTransaction.getTransactionHash();
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }

    @Override
    public Object withdraw(Long amount, String currency, String toAddress) {
        try {
            if(Objects.isNull(credentials)) return null;
            BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasProvider.getGasPrice(), gasProvider.getGasLimit(),
                    toAddress, value);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
            return ethSendTransaction.getTransactionHash();
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }

    @Override
    public Object balance(String currency) {
        if(Objects.isNull(credentials)) return null;
        try{
            if(currency.equals("eth")){
                var result = new EthGetBalance();
                result = this.web3j.ethGetBalance(credentials.getAddress(),
                                DefaultBlockParameter.valueOf("latest"))
                        .sendAsync()
                        .get();
                return result.getBalance().longValue();
            } else {
                String contractAddress = System.getProperty(String.format("contract.erc20.%s", currency));
                token = HumanStandardToken.load(contractAddress, web3j, credentials, gasProvider);
                var balance = token.balanceOf(credentials.getAddress());
                return balance.send().longValue();
            }
        }catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }

    /**
     * метод для восстановления информации о кошельке из пароля
     */
    @Override
    public Object restore(String password){
        if (credentials != null) return credentials;
        try{
            String decodedPassword = CoinWalletUtils.decodeInfo(password);
            var entity = walletRepository.getWalletEntityByPassword(decodedPassword);
            Credentials credentials = WalletUtils.loadCredentials(
                    decodedPassword, new File("src/main/resources/wallet/"+entity.getWalletFileName()));
            this.credentials = credentials;
            return Map.of("value", credentials.getAddress());
        } catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }

    @Override
    public Object generateSeed(Integer size, String language){
        try {
            String seed = CoinWalletUtils.generateSeed(size, language);
            byte[] seedBytes = seed.getBytes(StandardCharsets.UTF_8);
            CoinWalletUtils.initToEncryptModeCipher(cipher, "password");
            return Collections.singletonMap("seed", cipher.doFinal(seedBytes));
        } catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }


}
