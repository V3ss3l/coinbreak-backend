package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.*;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.repository.EthTransactionRepository;
import com.example.coinbreakbackend.repository.TransactionTypeRepository;
import com.example.coinbreakbackend.repository.EthWalletRepository;
import com.example.coinbreakbackend.service.EthWalletService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.example.coinbreakbackend.util.HumanStandardToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import java.io.File;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
public class EthWalletServiceImpl implements EthWalletService {
    private final EthWalletRepository ethWalletRepository;
    private final EthTransactionRepository ethTransactionRepository;
    private final CurrencyRepository currencyRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final Cipher cipher;

    private ObjectMapper objectMapper;
    private Credentials credentials;

    @Value("${cipher.password:#{''}}")
    private String cipherPassword;

    public EthWalletServiceImpl(EthWalletRepository ethWalletRepository,
                                EthTransactionRepository ethTransactionRepository,
                                CurrencyRepository currencyRepository,
                                TransactionTypeRepository transactionTypeRepository,
                                Web3j web3j,
                                ContractGasProvider gasProvider,
                                Cipher cipher,
                                ObjectMapper objectMapper) {
        this.ethWalletRepository = ethWalletRepository;
        this.ethTransactionRepository = ethTransactionRepository;
        this.currencyRepository = currencyRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.web3j = web3j;
        this.gasProvider = gasProvider;
        this.cipher = cipher;
        this.objectMapper = objectMapper;
    }

    /**
     * метод для перевода валюты на другой адрес кошелька
     */
    @Override
    public Object withdraw(Long amount, String currency, String toAddress) {
        try {
            if(Objects.isNull(credentials)) return null;

            String decryptedAddress = CoinWalletUtils.decrypt(toAddress, cipher, cipherPassword);
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            TransactionType type = transactionTypeRepository.getTransactionTypeByName("WITHDRAW");
            EthTransaction result = EthTransaction.builder()
                    .gasLimit(gasProvider.getGasLimit())
                    .gasPrice(gasProvider.getGasPrice())
                    .sender(credentials.getAddress())
                    .addressTo(decryptedAddress)
                    .transactionType(type)
                    .nonce(nonce)
                    .build();
            Map<String, Object> resultMap = new HashMap<>();
            Currency entityCurr = currencyRepository.getCurrencyByName(currency);

            if(entityCurr.getSymbol().equals("ETH")){
                BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        decryptedAddress, value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
                result.setValue(value);
                result.setTransactionHash(ethSendTransaction.getTransactionHash());
                resultMap.put("resultHash", ethSendTransaction.getTransactionHash());
            }
            else {
                String contractAddress = System.getProperty(String.format("contract.erc20.%s", entityCurr.getName()));
                HumanStandardToken token = HumanStandardToken.load(contractAddress, web3j, credentials, gasProvider);
                BigInteger tokenDecimals = token.decimals().send();
                BigDecimal decimal = BigDecimal.valueOf(tokenDecimals.longValue());
                BigInteger value = BigDecimal.valueOf(amount).multiply(decimal).toBigInteger();
                RemoteCall<TransactionReceipt> remoteCall = token.transfer(decryptedAddress, value);
                TransactionReceipt transactionReceipt = remoteCall.send();
                result.setValue(value);
                result.setTransactionHash(transactionReceipt.getTransactionHash());
                resultMap.put("resultHash", transactionReceipt.getTransactionHash());
            }
            ethTransactionRepository.save(result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }

    /**
     * метод для депозита валюты на кошелек
     */
    @Override
    public Object deposit(Long amount, String currency) {
        try {
            if(Objects.isNull(credentials)) return null;
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();
            TransactionType type = transactionTypeRepository.getTransactionTypeByName("DEPOSIT");
            EthTransaction result = EthTransaction.builder()
                    .gasLimit(gasProvider.getGasLimit())
                    .gasPrice(gasProvider.getGasPrice())
                    .sender(credentials.getAddress())
                    .addressTo(credentials.getAddress())
                    .transactionType(type)
                    .nonce(nonce)
                    .build();
            Map<String, Object> resultMap = new HashMap<>();
            Currency entityCurr = currencyRepository.getCurrencyByName(currency);
            if(entityCurr.getSymbol().equals("ETH")) {
                BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
                RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasProvider.getGasPrice(),
                        gasProvider.getGasLimit(),
                        credentials.getAddress(), value);
                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
                String hexValue = Numeric.toHexString(signedMessage);
                EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();
                result.setValue(value);
                result.setTransactionHash(ethSendTransaction.getTransactionHash());
                resultMap.put("resultHash", ethSendTransaction.getTransactionHash());
            }
            else {
                String contractAddress = System.getProperty(String.format("contract.erc20.%s", entityCurr.getName()));
                HumanStandardToken token = HumanStandardToken.load(contractAddress, web3j, credentials, gasProvider);
                BigInteger tokenDecimals = token.decimals().send();
                BigDecimal decimal = BigDecimal.valueOf(tokenDecimals.longValue());
                BigInteger value = BigDecimal.valueOf(amount).multiply(decimal).toBigInteger();
                RemoteCall<TransactionReceipt> remoteCall = token.transfer(credentials.getAddress(), value);
                TransactionReceipt transactionReceipt = remoteCall.send();
                result.setValue(value);
                result.setTransactionHash(transactionReceipt.getTransactionHash());
                resultMap.put("resultHash", transactionReceipt.getTransactionHash());
            }
            ethTransactionRepository.save(result);
            return resultMap;
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }

    /**
     * метод для восстановления информации о кошельке из пароля
     */
    @Override
    public Object restore(String password){
        if (credentials != null) return Map.of("value", credentials.getAddress());
        try{
            String decodedPassword = CoinWalletUtils.decrypt(password, cipher, cipherPassword);
            var entity = ethWalletRepository.getWalletEntityByPassword(decodedPassword);
            if(Objects.isNull(entity)){
                log.info("Не найдено кошелька с таким паролем");
                throw new Exception("Не найдено кошелька с таким паролем");
            }
            Credentials credentials = WalletUtils.loadCredentials(
                    decodedPassword, new File("src/main/resources/wallet/"+entity.getWalletFileName()));
            this.credentials = credentials;
            return Map.of("value", credentials.getAddress());
        } catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }

    /**
     * метод для генерации мнемонической фразы
     */
    @Override
    public Object generateSeed(Integer size, String language){
        try {
            String seed = CoinWalletUtils.generateSeed(size, language);
            byte[] salt = CoinWalletUtils.generateSalt();
            CoinWalletUtils.initToEncryptModeCipher(cipher, cipherPassword, salt);
            String result = CoinWalletUtils.encrypt(seed, cipher, salt);
            return Collections.singletonMap("seed", result);
        } catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }

    /**
     * метод для генерации крипто-кошелька из пароля и мнемонической фразы
     */
    @Override
    public Object generate(String password, String seed) {
        try {
            String decodedPassword = CoinWalletUtils.decrypt(password, cipher, cipherPassword);
            String decodedSeed = CoinWalletUtils.decrypt(seed, cipher, cipherPassword);
            Bip39Wallet wallet = WalletUtils.generateBip39WalletFromMnemonic(decodedPassword, decodedSeed, new File("src/main/resources/wallet"));
            Credentials credentials = WalletUtils.loadBip39Credentials(decodedPassword, seed);
            EthWallet entity = EthWallet.builder()
                    .publicKey(credentials.getAddress())
                    .seed(seed)
                    .password(decodedPassword)
                    .walletFileName(wallet.getFilename())
                    .build();
            ethWalletRepository.save(entity);
            this.credentials = credentials;
            WalletDto dto = WalletDto.builder()
                    .id(entity.getId())
                    .publicKey(entity.getPublicKey())
                    .seed(decodedSeed)
                    .build();
            String serialized = objectMapper.writeValueAsString(dto);
            byte[] salt = CoinWalletUtils.generateSalt();
            CoinWalletUtils.initToEncryptModeCipher(cipher, cipherPassword, salt);
            var encrypted = CoinWalletUtils.encrypt(serialized, cipher, salt);
            return Collections.singletonMap("value", encrypted);
        } catch (Exception e) {
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", "ERROR", "stackTrace", stackTrace);
        }
    }
}
