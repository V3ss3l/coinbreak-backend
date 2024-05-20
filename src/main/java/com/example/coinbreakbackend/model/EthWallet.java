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
    private String publicKey;
    private String walletFileName;
    @Column(unique = true)
    private String seed;
    @Column(unique = true)
    private String password;
}
