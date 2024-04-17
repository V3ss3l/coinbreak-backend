package com.example.coinbreakbackend.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Random;

public class CoinWalletUtils {

    private static SecureRandom random = new SecureRandom();

    public static String generateSeed() throws IOException {
        StringBuffer seed = new StringBuffer();
        FileReader input = new FileReader("src/main/resources/bip39-words.txt");
        BufferedReader bufRead = new BufferedReader(input);
        var stream = random.ints(12, 0, 2048).distinct().boxed().toList();
        var list = bufRead.lines().toList();
        for(int i = 0; i < stream.size(); i++){
            var word = list.get(stream.get(i));
            seed.append(i == stream.size() - 1 ? word : word+" ");
        }
        return seed.toString();
    }
}
