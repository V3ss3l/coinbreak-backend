package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.WalletDto;
import com.example.coinbreakbackend.model.WalletEntity;
import com.example.coinbreakbackend.repository.CurrencyRepository;
import com.example.coinbreakbackend.repository.WalletRepository;
import com.example.coinbreakbackend.service.WalletService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthGetBalance;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

@Service
public class WalletServiceImpl implements WalletService {
    private final WalletRepository walletRepository;
    private final CurrencyRepository currencyRepository;
    private final Web3j web3j;
    private Credentials credentials;

    public WalletServiceImpl(WalletRepository walletRepository, CurrencyRepository currencyRepository, Web3j web3j) {
        this.walletRepository = walletRepository;
        this.currencyRepository = currencyRepository;
        this.web3j = web3j;
    }

    @Override
    public WalletDto generate(String password) {
        try {
            String decodedPassword = decodeInfo(password);
            String seed = CoinWalletUtils.generateSeed(12, "english");
            Bip39Wallet wallet = WalletUtils.generateBip39WalletFromMnemonic(decodedPassword, seed, new File("src/main/resources/wallet"));
            Credentials credentials = WalletUtils.loadBip39Credentials(decodedPassword, seed);
            WalletEntity entity = WalletEntity.builder()
                    .publicKey(credentials.getAddress())
                    .seed(encodeInfo(seed))
                    .password(decodedPassword)
                    .walletFileName(wallet.getFilename())
                    .build();
            walletRepository.save(entity);
            this.credentials = credentials;
            WalletDto dto = WalletDto.builder()
                    .id(entity.getId())
                    .publicKey(entity.getPublicKey())
                    .build();
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deposit(Long amount, String currency) {
    }

    @Override
    public void withdraw(Long amount, String currency, String toAddress) {
    }

    @Override
    public Long balance(String currency) {
        if(Objects.isNull(credentials)) return null;
        try{
            var result = new EthGetBalance();
            result = this.web3j.ethGetBalance(credentials.getAddress(),
                            DefaultBlockParameter.valueOf("latest"))
                    .sendAsync()
                    .get();
            return result.getBalance().longValue();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * метод для восстановления информации о кошельке из пароля
     */
    @Override
    public Credentials restore(String password){
        if (credentials != null) return credentials;
        try{
            String decodedPassword = decodeInfo(password);
            var entity = walletRepository.getWalletEntityByPassword(decodedPassword);
            Credentials credentials = WalletUtils.loadCredentials(
                    decodedPassword, new File("src/main/resources/wallet/"+entity.getWalletFileName()));
            this.credentials = credentials;
            return credentials;
        } catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String generateSeed(Integer size, String language){
        try {
            String seed = CoinWalletUtils.generateSeed(size, language);
            return decodeInfo(seed);
        } catch (Exception e){
            e.printStackTrace();
        } return null;
    }

    private String decodeInfo(String info){
        byte[] decodedBytes= Base64.getDecoder().decode(info);
        String decodedInfo = new String(decodedBytes);
        return decodedInfo;
    }

    private String encodeInfo(String info){
        byte[] bytes = info.getBytes(StandardCharsets.UTF_8);
        String encodedInfo = Base64.getEncoder().encodeToString(bytes);
        return encodedInfo;
    }
}
