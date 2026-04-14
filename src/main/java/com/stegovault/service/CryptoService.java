package com.stegovault.service;
import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;

/**
 * Pozwala na operacje szyfrowania i odszyfrowania tekstu.
 */
public interface CryptoService {

    /**
     * Zaszyfrowuje dane podane przez bajty tekstu.
     *
     * @param data bajty tekstu do enkrypcji
     * @param cfg  Zawiera haslo, bajty soli, wektor inicjalizacyjny, oraz liczbe iteracji (derivacji klucza)
     * @return bajty zaszyfrowanego tekstu
     * @throws CryptoException jesli enkrypcja sie nie powiedzie
     */
    public byte[] encrypt(byte[] data, EncryptionConfig cfg) throws CryptoException;

    /**
     * Odszyfrowuje dane podane przez bajty do tekstu
     *
     * @param data bajty do dekrypcji
     * @param cfg  Zawiera haslo, bajty soli, wektor inicjalizacyjny, oraz liczbe iteracji (derivacji klucza)
     * @return bajty odszyfrowanego tesktu
     * @throws CryptoException jesli dekrypcja sie nie powiedzie
     */
    public byte[] decrypt(byte[] data, EncryptionConfig cfg) throws CryptoException;
}


