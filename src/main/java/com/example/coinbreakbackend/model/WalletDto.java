package com.example.coinbreakbackend.model;

import lombok.*;

import java.io.Serializable;

@Builder
@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class WalletDto implements Serializable {
    private Long id;
    private String publicKey;
    private String seed;

    public static String toString(WalletDto dto){
        StringBuilder string = new StringBuilder();
        string.append("Wallet:" + "{\n" +
                "      publicKey: " + dto.publicKey + "\n" +
        "}");
        return string.toString();
    }
}
