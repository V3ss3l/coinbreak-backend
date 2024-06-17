package com.example.coinbreakbackend.util;

import lombok.extern.slf4j.Slf4j;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Slf4j
public class CoinWalletUtils {

    private static SecureRandom random;

    static {
        try{
            random = SecureRandom.getInstance("SHA1PRNG");
        }catch (Exception ignored){}
    }

    public static String generateSeed(Integer sizeOfWords, String language) throws IOException {
        StringBuilder seed = new StringBuilder();
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

    public static void initToEncryptModeCipher(Cipher cipher, String password, byte[] salt) throws InvalidKeyException,
            NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        final byte[][] keyAndIV = generateKeyAndIV(32, 16, 1, salt,
                password.getBytes(StandardCharsets.UTF_8),
                MessageDigest.getInstance("MD5"));
        SecretKeySpec key = new SecretKeySpec(keyAndIV[0], "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(keyAndIV[1]);
        cipher.init(Cipher.ENCRYPT_MODE, key, ivParameterSpec);
    }
    public static void initToDecryptModeCipher(Cipher cipher, byte[] key, byte[] iv) throws InvalidKeyException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
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

    public static String decodeKey(String str) {
        byte[] decoded = Base64.getDecoder().decode(str.getBytes());
        return new String(decoded);
    }
    public static String encodeKey(String str) {
        byte[] encoded = Base64.getEncoder().encode(str.getBytes());
        return new String(encoded);
    }


    public static String encrypt(String stringToEncrypt, Cipher cipher, byte[] salt) {
        try {
            byte[] encryptedData = cipher.doFinal(stringToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] prefixAndSaltAndEncryptedData = new byte[16 + encryptedData.length];
            System.arraycopy("Salted__".getBytes(StandardCharsets.UTF_8),
                    0, prefixAndSaltAndEncryptedData,
                    0, 8);
            System.arraycopy(salt, 0,
                    prefixAndSaltAndEncryptedData, 8, 8);
            System.arraycopy(encryptedData, 0,
                    prefixAndSaltAndEncryptedData, 16, encryptedData.length);
            return Base64.getEncoder().encodeToString(prefixAndSaltAndEncryptedData);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String decrypt(String stringToDecrypt, Cipher cipher, String cipherPassword){
        try {
            int keySize = 8;
            int ivSize = 4;
            byte[] decodedText = Base64.getDecoder().decode(stringToDecrypt.getBytes(StandardCharsets.UTF_8));
            byte[] prefix = new byte[8];
            System.arraycopy(decodedText, 0, prefix, 0, 8);
            log.info("Is salted - " + new String(prefix).equals("Salted__"));
            byte[] salt = new byte[8];
            System.arraycopy(decodedText, 8, salt, 0, 8);
            byte[] trueCipherText = new byte[decodedText.length - 16];
            System.arraycopy(decodedText, 16, trueCipherText, 0, decodedText.length - 16);
            byte[] javaKey = new byte[keySize * 4];
            byte[] javaIv = new byte[ivSize * 4];
            evpKDF(cipherPassword.getBytes(StandardCharsets.UTF_8), keySize, ivSize, salt, javaKey, javaIv);
            initToDecryptModeCipher(cipher, javaKey, javaIv);
            byte[] byteMsg = cipher.doFinal(trueCipherText);
            return new String(byteMsg, StandardCharsets.UTF_8);

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
            while (generatedLength < keyLength + ivLength) {

                if (generatedLength > 0)
                    md.update(generatedData, generatedLength - digestLength, digestLength);
                md.update(password);
                if (salt != null)
                    md.update(salt, 0, 8);
                md.digest(generatedData, generatedLength, digestLength);

                for (int i = 1; i < iterations; i++) {
                    md.update(generatedData, generatedLength, digestLength);
                    md.digest(generatedData, generatedLength, digestLength);
                }

                generatedLength += digestLength;
            }
            byte[][] result = new byte[2][];
            result[0] = Arrays.copyOfRange(generatedData, 0, keyLength);
            if (ivLength > 0)
                result[1] = Arrays.copyOfRange(generatedData, keyLength, keyLength + ivLength);
            return result;
        } catch (DigestException e) {
            throw new RuntimeException(e);
        } finally {
            Arrays.fill(generatedData, (byte)0);
        }
    }

    private static byte[] evpKDF(byte[] password, int keySize, int ivSize, byte[] salt, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        return evpKDF(password, keySize, ivSize, salt, 1, "MD5", resultKey, resultIv);
    }

    private static byte[] evpKDF(byte[] password, int keySize, int ivSize, byte[] salt, int iterations, String hashAlgorithm, byte[] resultKey, byte[] resultIv) throws NoSuchAlgorithmException {
        int targetKeySize = keySize + ivSize;
        byte[] derivedBytes = new byte[targetKeySize * 4];
        int numberOfDerivedWords = 0;
        byte[] block = null;
        MessageDigest hasher = MessageDigest.getInstance(hashAlgorithm);
        while (numberOfDerivedWords < targetKeySize) {
            if (block != null) {
                hasher.update(block);
            }
            hasher.update(password);
            block = hasher.digest(salt);
            hasher.reset();

            for (int i = 1; i < iterations; i++) {
                block = hasher.digest(block);
                hasher.reset();
            }

            System.arraycopy(block, 0, derivedBytes, numberOfDerivedWords * 4,
                    Math.min(block.length, (targetKeySize - numberOfDerivedWords) * 4));

            numberOfDerivedWords += block.length/4;
        }

        System.arraycopy(derivedBytes, 0, resultKey, 0, keySize * 4);
        System.arraycopy(derivedBytes, keySize * 4, resultIv, 0, ivSize * 4);

        return derivedBytes;
    }
}
