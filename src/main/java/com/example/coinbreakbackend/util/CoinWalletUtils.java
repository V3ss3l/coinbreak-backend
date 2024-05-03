package com.example.coinbreakbackend.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.List;
import java.util.Random;

public class CoinWalletUtils {

    private static SecureRandom random = new SecureRandom();

    public static String generateSeed(Integer sizeOfWords, String language) throws IOException {
        StringBuffer seed = new StringBuffer();
        FileReader input = new FileReader(String.format("src/main/resources/words/bip39-%s.txt", language));
        BufferedReader bufRead = new BufferedReader(input);
        List<Integer> intStream = random.ints(sizeOfWords, 0, 2048)
                .distinct()
                .boxed()
                .toList();
        List<String> list = bufRead.lines().toList();
        for(int i = 0; i < intStream.size(); i++){
            var word = list.get(intStream.get(i));
            seed.append(i == intStream.size() - 1 ? word : word + " ");
        }
        return seed.toString();
    }
}
