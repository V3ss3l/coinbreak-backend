package com.example.coinbreakbackend.util;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.SecureRandom;
import java.util.Base64;
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

    public static void initToEncryptModeCipher(Cipher cipher, String password) throws InvalidKeyException {
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(bytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
    }

    public static void initToDecryptModeCipher(Cipher cipher, String password) throws InvalidKeyException {
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(bytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
    }

    public static String decodeInfo(String info){
        byte[] decodedBytes= Base64.getDecoder().decode(info);
        String decodedInfo = new String(decodedBytes);
        return decodedInfo;
    }

    public static String encodeInfo(String info){
        byte[] bytes = info.getBytes(StandardCharsets.UTF_8);
        String encodedInfo = Base64.getEncoder().encodeToString(bytes);
        return encodedInfo;
    }

    public static byte[] serialize(Object obj) {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();

        try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
            out.writeObject(obj);
            out.flush();
            return bos.toByteArray();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static Object deserialize(byte[] bytes) {
        ByteArrayInputStream bis = new ByteArrayInputStream(bytes);

        try (ObjectInput in = new ObjectInputStream(bis)) {
            return in.readObject();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public static String getStackTrace(Throwable throwable){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        throwable.printStackTrace(pw);
        return sw.getBuffer().toString();
    }
}
