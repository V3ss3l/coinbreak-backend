package com.example.coinbreakbackend.model;

import lombok.*;

import java.math.BigInteger;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class BalanceDto {
    private String wallet;
    private BigInteger amount;
    private Currency currency;
}
