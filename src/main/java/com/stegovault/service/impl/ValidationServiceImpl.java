package com.stegovault.service.impl;

import java.awt.image.BufferedImage;

import com.stegovault.service.ValidationService;

/**
 * Implementacja {@link ValidationService} odpowiedzialna za walidację
 * pojemności obrazu oraz poprawności hasła.
 */
public class ValidationServiceImpl implements ValidationService {

    /** Minimalna długość hasła. */
    private static final int MIN_PASSWORD_LENGTH = 8;

    /** Maksymalna długość hasła. */
    private static final int MAX_PASSWORD_LENGTH = 128;

    /**
     * Sprawdza czy obraz ma wystarczającą pojemność do przechowania payloadu.
     *
     * <p>Pojemność obrazu jest obliczana jako liczba pikseli pomnożona przez 3
     * (kanały RGB), ponieważ w każdym kanale przechowywany jest 1 bit (LSB).</p>
     *
     * @param image            obraz do sprawdzenia; nie może być {@code null}
     * @param payloadSizeBytes rozmiar payloadu w bajtach
     * @return {@code true} jeśli payload mieści się w obrazie, {@code false} w przeciwnym razie
     * @throws IllegalArgumentException jeśli {@code image} jest {@code null}
     */
    @Override
    public boolean validateCapacity(BufferedImage image, int payloadSizeBytes) {
        if (image == null) {
            throw new IllegalArgumentException("Obraz nie może być null");
        }
        // Każdy piksel przechowuje 1 bit na kanał RGB = 3 bity na piksel
        int availableBits = image.getWidth() * image.getHeight() * 3;
        int requiredBits  = payloadSizeBytes * 8;
        return requiredBits <= availableBits;
    }

    /**
     * Sprawdza czy hasło spełnia wymagania bezpieczeństwa.
     *
     * <p>Hasło jest uznawane za poprawne gdy:</p>
     * <ul>
     *   <li>nie jest {@code null} ani puste</li>
     *   <li>ma długość między {@value #MIN_PASSWORD_LENGTH}
     *       a {@value #MAX_PASSWORD_LENGTH} znaków</li>
     *   <li>zawiera co najmniej jedną wielką literę</li>
     *   <li>zawiera co najmniej jedną małą literę</li>
     *   <li>zawiera co najmniej jedną cyfrę</li>
     * </ul>
     *
     * @param password hasło do sprawdzenia
     * @return {@code true} jeśli hasło spełnia wymagania, {@code false} w przeciwnym razie
     */
    @Override
    public boolean validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            return false;
        }
        if (password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
            return false;
        }
        boolean hasUpper  = false;
        boolean hasLower  = false;
        boolean hasDigit  = false;

        for (char c : password.toCharArray()) {
            if (Character.isUpperCase(c)) hasUpper = true;
            if (Character.isLowerCase(c)) hasLower = true;
            if (Character.isDigit(c))     hasDigit = true;
        }

        return hasUpper && hasLower && hasDigit;
    }
}