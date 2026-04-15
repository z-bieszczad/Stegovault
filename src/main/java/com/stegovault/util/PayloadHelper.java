package com.stegovault.util;
import java.nio.ByteBuffer;

/**
 * Klasa pomocnicza odpowiedzialna za tworzenie i obsługę struktury payloadu
 * wykorzystywanej w steganografii.
 *
 * <p>Struktura payloadu:
 * <pre>
 * [4B: długość zaszyfrowanych danych ]
 * [16B: sól ]
 * [16B: wektor inicjalizacyjny IV]
 * [32B: hash oryginalnych danych]
 * [NB: dane zaszyfrowane AES]
 * </pre>
 *
 */

public class PayloadHelper {
    /**
     * Metoda statyczna uduje binarny payload zawierający zaszyfrowane dane oraz metadane
     * potrzebne do ich późniejszego odszyfrowania.
     *
     * @param encryptedData zaszyfrowane dane
     * @param salt sól (16 bajtów)
     * @param iv wektor inicjalizacyjny AES (16 bajtów)
     * @param hash hash oryginalnego tekstu (32 bajty)
     * @return tablica bajtów reprezentująca kompletny payload gotowy do ukrycia w obrazie
     */
    public static byte[] buildPayload(byte[] encryptedData, byte[] salt, byte[] iv, byte[] hash){
        ByteBuffer buffer=ByteBuffer.allocate(4+ 16 +16+ 32+ encryptedData.length);

        buffer.putInt(encryptedData.length);
        buffer.put(salt);
        buffer.put(iv);
        buffer.put(hash);
        buffer.put(encryptedData);

        return buffer.array();
    }
}
