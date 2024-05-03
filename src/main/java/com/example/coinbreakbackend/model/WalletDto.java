package com.example.coinbreakbackend.model;

import lombok.*;

import java.io.File;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Map;

@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto{
    private Long id;
    private String publicKey;
    private Map<Currency, BigDecimal> balance;

    public static String toString(WalletDto dto){
        StringBuilder string = new StringBuilder();
        string.append("Wallet:" + "{\n" +
                "      publicKey: " + dto.publicKey + "\n" +
        "}");
        return string.toString();
    }
}
