package com.example.coinbreakbackend.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigInteger;

@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
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
    private String to;
    private BigInteger value;
    @Column(nullable = false)
    private String data;
}
