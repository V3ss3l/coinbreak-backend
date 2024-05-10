package com.example.coinbreakbackend.util;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
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

    public static void initToEncryptModeCipher(Cipher cipher, String password) throws InvalidKeyException, NoSuchAlgorithmException {
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(bytes, "AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, random);
    }

    public static void initToEncryptModeCipher(Cipher cipher, String password, byte[] salt) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final byte[][] keyAndIV = generateKeyAndIV(32, 16, 1, salt,
                password.getBytes(StandardCharsets.UTF_8),
                MessageDigest.getInstance("MD5"));
        SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(keyAndIV[1]);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
    }

    public static void initToDecryptModeCipher(Cipher cipher, String password) throws InvalidKeyException {
        byte[] bytes = password.getBytes(StandardCharsets.UTF_8);
        SecretKey secretKey = new SecretKeySpec(bytes, "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, random);
    }

    public static String decodeInfo(String info){
        byte[] decodedBytes= Base64.getDecoder().decode(info);
        return new String(decodedBytes);
    }

    public static String encodeInfo(String info){
        byte[] bytes = info.getBytes(StandardCharsets.UTF_8);
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static String encodeInfo(byte[] bytes){
        return Base64.getEncoder().encodeToString(bytes);
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

    public static String encrypt(String stringToEncrypt, Cipher cipher, byte[] salt) {
        try {
            byte[] encryptedData = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] prefixAndSaltAndEncryptedData = new byte[16 + encryptedData.length];
            // Copy prefix (0-th to 7-th bytes)
            System.arraycopy("Salted__".getBytes(StandardCharsets.UTF_8), 0, prefixAndSaltAndEncryptedData, 0, 8);
            // Copy salt (8-th to 15-th bytes)
            System.arraycopy(salt, 0, prefixAndSaltAndEncryptedData, 8, 8);
            // Copy encrypted data (16-th byte and onwards)
            System.arraycopy(encryptedData, 0, prefixAndSaltAndEncryptedData, 16, encryptedData.length);
            return Base64.getEncoder().encodeToString(prefixAndSaltAndEncryptedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] generateSalt(){
        byte[] salt = new byte[8];
        random.nextBytes(salt);
        return salt;
    }

    private static byte[][] generateKeyAndIV(int keyLength, int ivLength, int iterations, byte[] salt, byte[] password, MessageDigest md) {

        int digestLength = md.getDigestLength();
        int requiredLength = (keyLength + ivLength + digestLength - 1) / digestLength * digestLength;
        byte[] generatedData = new byte[requiredLength];
        int generatedLength = 0;

        try {
            md.reset();

            // Repeat process until sufficient data has been generated
            while (generatedLength < keyLength + ivLength) {

                // Digest data (last digest if available, password data, salt if available)
                if (generatedLength > 0)
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                md.update(password);
                if (salt != null)
                    md.update(salt, 0, 8);
                md.digest(generatedData, generatedLength, digestLength);

                // additional rounds
                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }

                generatedLength += digestLength;
            }

            // Copy key and IV into separate byte arrays
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0)
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);

            return result;

        } catch (DigestException e) {
            throw new RuntimeException(e);

        } finally {
            // Clean out temporary data
            Arrays.fill(generatedData, (byte)0);
        }
    }
}
