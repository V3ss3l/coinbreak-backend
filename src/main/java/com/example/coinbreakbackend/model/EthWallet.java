package com.example.coinbreakbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EthWallet implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "public_key", unique = true)
    private String publicKey;
    @Column(name = "wallet_file_name", unique = true)
    private String walletFileName;
    @Column(unique = true)
    private String seed;
    @Column(unique = true)
    private String password;
}
