package com.lin.crypto.core;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES-256-GCM 加解密核心。
 * 输出格式：base64(IV[12] || ciphertext || GCM_tag[16])
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int IV_LENGTH = 12;
    private static final int AES_KEY_SIZE = 256;

    /** 生成随机 AES-256 密钥 */
    public static SecretKey generateKey() {
        try {
            KeyGenerator generator = KeyGenerator.getInstance("AES");
            generator.init(AES_KEY_SIZE, SecureRandom.getInstanceStrong());
            return generator.generateKey();
        } catch (Exception e) {
            throw new RuntimeException("生成加密密钥失败", e);
        }
    }

    /** AES-256-GCM 加密 */
    public static String encrypt(byte[] plaintext, SecretKey key) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            SecureRandom.getInstanceStrong().nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            byte[] ciphertext = cipher.doFinal(plaintext);

            byte[] result = new byte[IV_LENGTH + ciphertext.length];
            System.arraycopy(iv, 0, result, 0, IV_LENGTH);
            System.arraycopy(ciphertext, 0, result, IV_LENGTH, ciphertext.length);

            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new RuntimeException("加密失败", e);
        }
    }

    /** AES-256-GCM 解密 */
    public static byte[] decrypt(String base64Data, SecretKey key) {
        try {
            byte[] data = Base64.getDecoder().decode(base64Data);

            byte[] iv = new byte[IV_LENGTH];
            byte[] ciphertext = new byte[data.length - IV_LENGTH];
            System.arraycopy(data, 0, iv, 0, IV_LENGTH);
            System.arraycopy(data, IV_LENGTH, ciphertext, 0, ciphertext.length);

            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new GCMParameterSpec(GCM_TAG_LENGTH, iv));
            return cipher.doFinal(ciphertext);
        } catch (Exception e) {
            throw new RuntimeException("解密失败", e);
        }
    }
}
