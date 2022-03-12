package de.tobias.simpsocserv.utils;

import javax.crypto.Cipher;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAPair {

    public byte[] publicKey;
    public byte[] privateKey;

    public RSAPair(byte[] pPublic, byte[] pPrivate) {
        this.publicKey = pPublic;
        this.privateKey = pPrivate;
    }

    public RSAPair() throws Exception {
        KeyPairGenerator pairGen = KeyPairGenerator.getInstance("RSA");
        pairGen.initialize(2048);
        KeyPair pair = pairGen.generateKeyPair();
        this.publicKey = pair.getPublic().getEncoded();
        this.privateKey = pair.getPrivate().getEncoded();
    }

    public byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, getPrivateKey());
        return cipher.doFinal(data);
    }

    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, getPublicKey());
        return cipher.doFinal(data);
    }

    public PublicKey getPublicKey() throws Exception {
        if(publicKey == null) return null;
        return KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
    }

    public PrivateKey getPrivateKey() throws Exception {
        if(privateKey == null) return null;
        return KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
    }
}
