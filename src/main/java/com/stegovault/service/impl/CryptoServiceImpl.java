package com.stegovault.service.impl;
import java.security.spec.KeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;
import com.stegovault.service.CryptoService;

/**
 * Implementacja {@link CryptoService} z uzyciem SHA-256
 */
public class CryptoServiceImpl implements CryptoService{

    private static final String KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String KEY_SPEC = "AES";
    private static final int KEY_LENGTH = 256;

    /**
     * derywuje 256-bitowy klucz AES przy uzyciu PBKDF2WithHmacSHA256.
     * @param cfg konfiguracja enkrypcji
     * @return derived {@link SecretKey}
     * @throws CryptoException gdy derywacja klucza sie nie uda
     */
    private SecretKey deriveKey(EncryptionConfig cfg) throws CryptoException
    {
        try {
            KeySpec spec = new PBEKeySpec(
                cfg.password().toCharArray(),
                cfg.salt(),
                cfg.iterations(),
                KEY_LENGTH
            );
            SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
            byte[] keyBytes = factory.generateSecret(spec).getEncoded();
            return new SecretKeySpec(keyBytes, KEY_SPEC);
        } catch (Exception e) {
            throw new CryptoException("Derywacja klucza sie nie powiodla", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] encrypt(byte[] data, EncryptionConfig cfg) throws CryptoException
    {
        try 
        {
            SecretKey key = deriveKey(cfg);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(cfg.iv()));
            return cipher.doFinal(data);   
        } 
        catch (Exception e) 
        {
            throw new CryptoException("Enkrypcja sie nie powiodla", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] decrypt(byte[] data, EncryptionConfig cfg) throws CryptoException {
        try {
            SecretKey key = deriveKey(cfg);
            Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(cfg.iv()));
            return cipher.doFinal(data);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new CryptoException("Dekrypcja sie nie udala -- zle haslo lub dane", e);
        } catch (Exception e) {
            throw new CryptoException("Dekrypcja sie nie powiodla", e);
        }
    }
    
}
