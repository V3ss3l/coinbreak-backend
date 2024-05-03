package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
public interface WalletRepository extends JpaRepository<WalletEntity, Long> {

    WalletEntity getWalletEntityByPublicKey(String publicKey);
    WalletEntity getWalletEntityByPassword(String password);
}
