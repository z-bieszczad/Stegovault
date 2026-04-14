package com.stegovault.service;
/**
 * Pozwala na hashowanie tekstu oraz weryfikację haszy
 */
public interface HashService {
    /**
     * Generuje hash z danych
     * @param data dane do hashowania
     * @return bajty hashu
     */
    public byte[] generateHash(byte[] data);
    /**
     * Generuje hash na podstawie danych oraz porownuje to z podanym hashem
     * @param data dane do hashowania
     * @param hash hash sprawdzajacy dane
     * @return
     */
    public byte[] verifyHash(byte[] data, byte[] hash);
}
