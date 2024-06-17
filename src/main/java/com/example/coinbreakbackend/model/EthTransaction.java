package com.example.coinbreakbackend.model;

import jakarta.persistence.*;
import lombok.*;

import java.io.Serializable;
import java.math.BigInteger;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
public class EthTransaction implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sender;
    @ManyToOne
    @JoinColumn(name = "type_id")
    private TransactionType transactionType;
    private BigInteger nonce;
    @Column(name = "gas_price")
    private BigInteger gasPrice;
    @Column(name = "gas_limit")
    private BigInteger gasLimit;
    private String addressTo;
    private BigInteger value;
    @Column(nullable = false)
    private String data;
    @Column(name = "transaction_hash")
    private String transactionHash;
}
