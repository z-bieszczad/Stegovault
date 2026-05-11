package com.stegovault.service.impl;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.stegovault.exception.CryptoException;
import com.stegovault.service.HashService;

/**
 * Implementacja {@link HashService} oparta na algorytmie SHA-256.
 *
 * <p>Klasa dostarcza metody do generowania skrótów kryptograficznych (hashów)
 * oraz ich weryfikacji. Używa standardowego algorytmu {@code SHA-256}
 * dostępnego w {@link java.security.MessageDigest}.</p>
 *
 * <p>Przykład użycia:</p>
 * <pre>{@code
 * HashService hashService = new HashServiceImpl();
 * byte[] data = "tajne dane".getBytes(StandardCharsets.UTF_8);
 *
 * byte[] hash = hashService.generateHash(data);
 * byte[] verified = hashService.verifyHash(data, hash); // zwraca hash jeśli zgodny
 * }</pre>
 *
 * @see HashService
 * @see CryptoException
 */
public class HashServiceImpl implements HashService {
 
    /** Nazwa algorytmu skrótu kryptograficznego używanego przez implementację. */
    private static final String ALGORITHM = "SHA-256";
 
    /**
     * Generuje skrót kryptograficzny SHA-256 z podanych danych.
     *
     * <p>Metoda oblicza hash dla przekazanej tablicy bajtów przy użyciu
     * algorytmu SHA-256. Wynikowy skrót ma stałą długość 32 bajtów (256 bitów).</p>
     *
     * @param data dane wejściowe do hashowania; nie mogą być {@code null}
     * @return tablica 32 bajtów zawierająca wygenerowany skrót SHA-256
     * @throws IllegalArgumentException jeśli {@code data} jest {@code null}
     * @throws CryptoException          jeśli algorytm SHA-256 jest niedostępny
     *                                  w środowisku wykonawczym (nie powinno wystąpić
     *                                  na standardowym JDK)
     */
    @Override
    public byte[] generateHash(byte[] data) throws CryptoException {
        if (data == null) {
            throw new IllegalArgumentException("Dane wejściowe nie mogą być null");
        }
        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            return digest.digest(data);
        } catch (NoSuchAlgorithmException e) {
            throw new CryptoException(
                    "Algorytm " + ALGORITHM + " jest niedostępny w tym środowisku", e);
        }
    }
 
    /**
     * Weryfikuje integralność danych przez porównanie ich skrótu z oczekiwanym hashem.
     *
     * <p>Metoda oblicza skrót SHA-256 z podanych danych, a następnie porównuje go
     * z dostarczoną wartością referencyjną. Porównanie jest wykonywane w czasie stałym
     * ({@link MessageDigest#isEqual}), co chroni przed atakami czasowymi
     * (ang. <em>timing attacks</em>).</p>
     *
     * <p>Jeśli hasze są zgodne, metoda zwraca obliczony hash (potwierdzenie integralności).
     * W przypadku niezgodności rzuca wyjątek {@link CryptoException}.</p>
     *
     * @param data dane wejściowe do weryfikacji; nie mogą być {@code null}
     * @param hash oczekiwany skrót referencyjny do porównania; nie może być {@code null}
     * @return obliczony skrót danych, jeśli jest zgodny z {@code hash}
     * @throws IllegalArgumentException jeśli {@code data} lub {@code hash} są {@code null}
     * @throws CryptoException          jeśli obliczony skrót danych nie zgadza się
     *                                  z podanym {@code hash} — oznacza naruszenie
     *                                  integralności danych, lub jeśli algorytm
     *                                  SHA-256 jest niedostępny
     */
    @Override
    public byte[] verifyHash(byte[] data, byte[] hash) throws CryptoException {
        if (data == null) {
            throw new IllegalArgumentException("Dane wejściowe nie mogą być null");
        }
        if (hash == null) {
            throw new IllegalArgumentException("Hash referencyjny nie może być null");
        }
 
        byte[] computed = generateHash(data);
 
        if (!MessageDigest.isEqual(computed, hash)) {
            throw new CryptoException(
                    "Weryfikacja integralności nie powiodła się: " +
                    "obliczony hash nie zgadza się z oczekiwanym");
        }
 
        return computed;
    }
}
 
