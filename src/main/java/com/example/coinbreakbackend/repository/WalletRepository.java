package com.example.coinbreakbackend.repository;

import com.example.coinbreakbackend.model.EthWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WalletRepository extends JpaRepository<EthWallet, Long> {

    EthWallet getWalletEntityByPublicKey(String publicKey);
    EthWallet getWalletEntityByPassword(String password);
}
