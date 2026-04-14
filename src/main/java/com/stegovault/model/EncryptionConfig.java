package com.stegovault.model;

/**
 * Przetrzymuje informacje o parametrach konfiguracji enkrypcji AES-256-CBC i derivacji klucza PBKDF2.
 * @param password haslo przekazane przez uzytkownika.
 * @param salt 16-bajtowa sol dla klucza PBKDF2
 * @param iv 16-bajtowy wektor inicjalizacyjny dla AES-CBC
 * @param iterations liczba iteracji PBKDF2 (domyslnie 100000)
 */
public record EncryptionConfig(String password, byte[] salt, byte[] iv, int iterations) {}
