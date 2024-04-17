package com.example.coinbreakbackend.model;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@ToString
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto implements Serializable {
    private String name;
    private String publicKey;
    private String privateKey;
    private Map<Currency, BigDecimal> balance;
}
