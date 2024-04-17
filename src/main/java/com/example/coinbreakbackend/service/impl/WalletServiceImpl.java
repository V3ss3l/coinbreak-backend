package com.example.coinbreakbackend.service.impl;

import com.example.coinbreakbackend.model.WalletDto;
import com.example.coinbreakbackend.service.WalletService;
import com.example.coinbreakbackend.util.CoinWalletUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.web3j.crypto.*;
import org.web3j.utils.Strings;

import java.io.File;
import java.math.BigInteger;
import java.nio.charset.CharsetDecoder;
import java.util.Base64;

@Service
public class WalletServiceImpl implements WalletService {

    private ObjectMapper objectMapper;

    public WalletServiceImpl() {
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public WalletDto generate(String password) {
        try {
            String decodedPassword = decodeInfo(password);
            String seed = CoinWalletUtils.generateSeed();
            Bip39Wallet wallet = WalletUtils.generateBip39WalletFromMnemonic(decodedPassword, seed, new File("src/main/resources/wallet"));

            Credentials credentials1 = WalletUtils.loadBip39Credentials(decodedPassword, seed);
            ECKeyPair keyPair = credentials1.getEcKeyPair();
            String privateKeyHex = keyPair.getPrivateKey().toString(16);
            String publicKeyHex = keyPair.getPublicKey().toString(16);

            WalletDto dto = WalletDto.builder()
                    .privateKey(String.format("0x%s",privateKeyHex))
                    .publicKey(String.format("0x%s", publicKeyHex))
                    .build();
            return dto;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void deposit() {

    }

    @Override
    public void withdraw() {

    }

    @Override
    public void balance() {

    }

    /**
     * метод для восстановления информации о кошельке из мнемоника и пароля
     */
    @Override
    public Credentials restore(String mnemonic, String password){
        String decodedMnemonic = decodeInfo(mnemonic);
        String decodedPassword = decodeInfo(password);
        return WalletUtils.loadBip39Credentials(decodedPassword, decodedMnemonic);
    }

    private String decodeInfo(String info){
        byte[] decodedBytes= Base64.getDecoder().decode(info);
        String decodedInfo = new String(decodedBytes);
        return decodedInfo;
    }
}
