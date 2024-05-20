package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.EthWallet;
import com.example.coinbreakbackend.model.WalletDto;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.repository.WalletRepository;
import com.example.coinbreakbackend.service.EthWalletService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.example.coinbreakbackend.util.HumanStandardToken;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import javax.crypto.Cipher;
import java.io.File;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

@Service
public class EthWalletServiceImpl implements EthWalletService {
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final Web3j web3j;
    private final ContractGasProvider gasProvider;
    private final Cipher cipher;

    private ObjectMapper objectMapper;
    private Credentials credentials;

    @Value("${cipher.password:#{''}}")
    private String cipherPassword;

    public EthWalletServiceImpl(WalletRepository walletRepository,
                                CurrencyRepository currencyRepository,
                                Web3j web3j,
                                ContractGasProvider gasProvider,
                                Cipher cipher) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.web3j = web3j;
        this.gasProvider = gasProvider;
        this.cipher = cipher;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Object withdraw(Long amount, String currency, String toAddress) {
        try {
            if(Objects.isNull(credentials)) return null;

            String decryptedAddress = CoinWalletUtils.decrypt(toAddress, cipher, cipherPassword);

            BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();

            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                    credentials.getAddress(), DefaultBlockParameterName.LATEST).sendAsync().get();
            BigInteger nonce = ethGetTransactionCount.getTransactionCount();

            RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasProvider.getGasPrice(), gasProvider.getGasLimit(),
                    decryptedAddress, value);
            byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);
            String hexValue = Numeric.toHexString(signedMessage);
            EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

            return Collections.singletonMap("result", ethSendTransaction.getTransactionHash());
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
            else{
                BigInteger value = Convert.toWei(amount.toString(), Convert.Unit.ETHER).toBigInteger();
            }
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

    /**
     * метод для восстановления информации о кошельке из пароля
     */
    @Override
    public Object restore(String password){
        if (credentials != null) return credentials;
        try{
            String decodedPassword = CoinWalletUtils.decrypt(password, cipher, cipherPassword);
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
            byte[] salt = CoinWalletUtils.generateSalt();
            CoinWalletUtils.initToEncryptModeCipher(cipher, cipherPassword, salt);
            String result = CoinWalletUtils.encrypt(seed, cipher, salt);
            return Collections.singletonMap("seed", result);
        } catch (Exception e){
            e.printStackTrace();
            var stackTrace = CoinWalletUtils.getStackTrace(e);
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }


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
            walletRepository.save(entity);
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
            return Map.of("value", null, "stackTrace", stackTrace);
        }
    }
}
