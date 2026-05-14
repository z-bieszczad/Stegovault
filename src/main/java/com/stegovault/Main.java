package com.stegovault;

import java.awt.image.BufferedImage;
import java.io.Console;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

import javax.imageio.ImageIO;

import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.HashServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;

public class Main {

    public static void main(String[] args) throws Exception {
        String inputImagePath  = "input.png";
        String inputTextPath   = "message.txt";
        String outputImagePath = "output.png";

        CryptoService     cryptoService = new CryptoServiceImpl();
        ValidationService validation    = new ValidationServiceImpl();
        HashService       hashService   = new HashServiceImpl();
        StegoService      stego         = new StegoServiceImpl(cryptoService, validation, hashService);

        Scanner scanner = new Scanner(System.in);

        System.out.println("╔══════════════════════════════╗");
        System.out.println("║        StegoVault v1.0       ║");
        System.out.println("╚══════════════════════════════╝");
        System.out.println("Wybierz tryb:");
        System.out.println("  1 — Kodowanie (embed)");
        System.out.println("  2 — Dekodowanie (extract)");
        System.out.print("Wybór: ");
        String choice = scanner.nextLine().trim();

        switch (choice) {
            case "1" -> encode(stego, hashService, validation, scanner,
                    inputImagePath, inputTextPath, outputImagePath);
            case "2" -> decode(stego, scanner, outputImagePath);
            default  -> System.err.println("Nieznany tryb: " + choice);
        }

        scanner.close();
    }

    private static void encode(
            StegoService stego,
            HashService hashService,
            ValidationService validation,
            Scanner scanner,
            String inputImagePath,
            String inputTextPath,
            String outputImagePath) throws Exception {

        System.out.println("\n=== KODOWANIE ===");

        // Wczytaj obraz wejściowy
        File imageFile = new File(inputImagePath);
        if (!imageFile.exists()) {
            System.err.println("Błąd: plik obrazu nie istnieje: " + inputImagePath);
            return;
        }
        BufferedImage image = ImageIO.read(imageFile);
        System.out.println("Wczytano obraz: " + inputImagePath
                + " (" + image.getWidth() + "x" + image.getHeight() + " px)");

        // Wczytaj wiadomość z pliku tekstowego
        File textFile = new File(inputTextPath);
        if (!textFile.exists()) {
            System.err.println("Błąd: plik tekstowy nie istnieje: " + inputTextPath);
            return;
        }
        String message = Files.readString(Path.of(inputTextPath));
        System.out.println("Wczytano wiadomość: " + inputTextPath
                + " (" + message.length() + " znaków)");

        // Pobierz i zwaliduj hasło
        String password = readPassword(scanner, "Podaj hasło do szyfrowania: ");
        if (!validation.validatePassword(password)) {
            System.err.println("Błąd: hasło nie spełnia wymagań.");
            System.err.println("Wymagania: min. 8 znaków, wielka litera, mała litera, cyfra.");
            return;
        }

        // Utwórz konfigurację szyfrowania
        byte[] iv   = new byte[16];
        byte[] salt = new byte[16];
        EncryptionConfig config = new EncryptionConfig(password, salt, iv, 65536);

        // Zakoduj i zapisz obraz
        stego.encode(message, config, image);
        ImageIO.write(image, "PNG", new File(outputImagePath));

        System.out.println("Wiadomość zakodowana i zapisana w: " + outputImagePath);
    }

    private static void decode(
            StegoService stego,
            Scanner scanner,
            String outputImagePath) throws Exception {

        System.out.println("\n=== DEKODOWANIE ===");

        // Wczytaj obraz z zakodowaną wiadomością
        File imageFile = new File(outputImagePath);
        if (!imageFile.exists()) {
            System.err.println("Błąd: plik obrazu nie istnieje: " + outputImagePath);
            return;
        }
        BufferedImage image = ImageIO.read(imageFile);
        System.out.println("Wczytano obraz: " + outputImagePath);

        // Pobierz hasło
        String password = readPassword(scanner, "Podaj hasło do odszyfrowania: ");

        // Utwórz konfigurację — IV i sól są odczytywane z payloadu, wartości tu nieistotne
        EncryptionConfig config = new EncryptionConfig(password, new byte[16], new byte[16], 65536);

        // Zdekoduj wiadomość
        try {
            String decoded = stego.decode(image, config);
            System.out.println("Zdekodowana wiadomość:\n");
            System.out.println(decoded);
        } catch (CryptoException e) {
            System.err.println("Błąd: nieprawidłowe hasło lub uszkodzony obraz.");
        }
    }

    /**
     * Odczytuje hasło z konsoli. Jeśli dostępna jest {@link Console} (terminal),
     * hasło jest maskowane (znaki nie są wyświetlane). W środowisku IDE/Maven
     * używany jest zwykły {@link Scanner}.
     */
    private static String readPassword(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] pwd = console.readPassword(prompt);
            return new String(pwd);
        } else {
            // Fallback dla IDE i mvn exec:java gdzie System.console() == null
            System.out.print(prompt);
            return scanner.nextLine().trim();
        }
    }
}