package de.tobias.simpsocserv.utils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class AESPair {

    public byte[] secretKey;
    public byte[] iv;

    public AESPair(byte[] pSecretKey, byte[] pIv) {
        this.secretKey = pSecretKey;
        this.iv = pIv;
    }

    public AESPair() throws Exception {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        this.secretKey = secretKey.getEncoded();
    }

    public byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), getIV());
        return cipher.doFinal(data);
    }

    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), getIV());
        return cipher.doFinal(data);
    }

    public SecretKeySpec getSecretKey() {
        return new SecretKeySpec(secretKey, "AES");
    }

    public IvParameterSpec getIV() {
        return new IvParameterSpec(iv);
    }
}
