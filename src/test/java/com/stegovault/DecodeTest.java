package com.stegovault;

import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import com.stegovault.exception.CryptoException;
import com.stegovault.model.EncryptionConfig;
import com.stegovault.model.ParsedPayload;
import com.stegovault.service.CryptoService;
import com.stegovault.service.HashService;
import com.stegovault.service.StegoService;
import com.stegovault.service.ValidationService;
import com.stegovault.service.impl.CryptoServiceImpl;
import com.stegovault.service.impl.HashServiceImpl;
import com.stegovault.service.impl.StegoServiceImpl;
import com.stegovault.service.impl.ValidationServiceImpl;
import com.stegovault.util.LSBHelper;
import com.stegovault.util.PayloadHelper;

class DecodeTest {

    // =========================================================================
    // Wspólna konfiguracja dla wszystkich testów
    // =========================================================================

    private StegoService stego;
    private EncryptionConfig config;
    private BufferedImage img;
    private HashService hashService;
    private CryptoService cryptoService;
    private ValidationService validation;

    @BeforeEach
    void setUp() {
        cryptoService = new CryptoServiceImpl();
        validation    = new ValidationServiceImpl();
        hashService   = new HashServiceImpl();
        stego         = new StegoServiceImpl(cryptoService, validation, hashService);

        // Domyślna konfiguracja szyfrowania z zerowymi IV i solą
        config = new EncryptionConfig("passw", new byte[16], new byte[16], 65536);
        // Obraz 100x100 pikseli używany w większości testów
        img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
    }

    // =========================================================================
    // Testy integracyjne: pełny cykl embed → extract
    // =========================================================================

    @Nested
    class StegoIntegrationTests {

        @Test
        // Weryfikacja że zdekodowana wiadomość jest identyczna z oryginalną
        void decode_shouldReturnOriginalMessage_afterEncode() throws Exception {
            String message = "Ala ma kota";
            stego.encode(message, config, img);
            assertEquals(message, stego.decode(img, config));
        }

        @Test
        // Weryfikacja obsługi pustego ciągu znaków jako wiadomości
        void decode_shouldHandleEmptyString() throws Exception {
            stego.encode("", config, img);
            assertEquals("", stego.decode(img, config));
        }

        @Test
        // Weryfikacja poprawnego kodowania znaków specjalnych i polskich liter
        void decode_shouldHandleSpecialCharacters() throws Exception {
            String message = "Zażółć gęślą jaźń!@#$%";
            stego.encode(message, config, img);
            assertEquals(message, stego.decode(img, config));
        }

        @Test
        // Weryfikacja że zdekodowane dane są bitowo identyczne z oryginałem (SHA-256)
        void fullCycle_shouldPreserveDataIntegrity_verifiedBySHA256() throws Exception {
            String original = "Pełny tekst pliku TXT do weryfikacji integralności danych.";
            byte[] originalBytes = original.getBytes(StandardCharsets.UTF_8);
            byte[] hashBefore = hashService.generateHash(originalBytes);

            stego.encode(original, config, img);
            String decoded = stego.decode(img, config);

            byte[] hashAfter = hashService.generateHash(decoded.getBytes(StandardCharsets.UTF_8));
            assertArrayEquals(hashBefore, hashAfter, "Hash SHA-256 przed i po musi być identyczny");
            assertEquals(original, decoded);
        }

        @Test
        // Weryfikacja że kodowanie faktycznie modyfikuje piksele obrazu
        void encode_shouldModifyAtLeastOnePixel() throws Exception {
            // Biały obraz (wszystkie kanały 0xFF) gwarantuje zmianę LSB przy zapisie bitu 0
            BufferedImage whiteImg = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < whiteImg.getHeight(); y++)
                for (int x = 0; x < whiteImg.getWidth(); x++)
                    whiteImg.setRGB(x, y, 0xFFFFFF);

            int[] before = whiteImg.getRGB(0, 0, whiteImg.getWidth(), whiteImg.getHeight(),
                    null, 0, whiteImg.getWidth());
            stego.encode("Ala ma kota", config, whiteImg);
            int[] after = whiteImg.getRGB(0, 0, whiteImg.getWidth(), whiteImg.getHeight(),
                    null, 0, whiteImg.getWidth());

            assertFalse(Arrays.equals(before, after));
        }

        @Test
        // Weryfikacja że można zakodować wiadomość o maksymalnym rozmiarze (pojemność LSB - overhead)
        void encode_shouldSucceed_whenMessageSizeIsAtMaxCapacity() throws Exception {
            // 100x100x3 = 30000 bitów LSB → 3750 bajtów surowej pojemności
            // overhead PayloadHelper: 4 (długość) + 16 (sól) + 16 (IV) + 32 (hash) = 68 bajtów
            // AES-CBC padding: dodatkowe 16 bajtów marginesu
            int maxMessageBytes = (img.getWidth() * img.getHeight() * 3) / 8 - 68 - 16;
            String maxMessage = "A".repeat(maxMessageBytes);
            stego.encode(maxMessage, config, img);
            assertEquals(maxMessage, stego.decode(img, config));
        }

        @Test
        // Weryfikacja że obraz steganograficzny ma PSNR > 50 dB względem oryginału
        void encode_shouldProduceImageWithPSNRAbove50dB() throws Exception {
            // Przygotowanie obrazu z niezerowymi pikselami
            BufferedImage original = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            BufferedImage encoded  = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
            for (int y = 0; y < 100; y++)
                for (int x = 0; x < 100; x++) {
                    int pixel = (x * 2 + y * 3) & 0xFFFFFF;
                    original.setRGB(x, y, pixel);
                    encoded.setRGB(x, y, pixel);
                }

            stego.encode("test message", config, encoded);

            // Obliczenie MSE i PSNR dla wszystkich kanałów RGB
            double mse = 0;
            for (int y = 0; y < 100; y++) {
                for (int x = 0; x < 100; x++) {
                    int p1 = original.getRGB(x, y);
                    int p2 = encoded.getRGB(x, y);
                    for (int shift : new int[]{16, 8, 0}) {
                        int c1 = (p1 >> shift) & 0xFF;
                        int c2 = (p2 >> shift) & 0xFF;
                        mse += (c1 - c2) * (c1 - c2);
                    }
                }
            }
            mse /= (100.0 * 100 * 3);
            double psnr = mse == 0 ? Double.MAX_VALUE : 10 * Math.log10((255.0 * 255.0) / mse);

            assertTrue(psnr > 50, "PSNR powinien być > 50 dB, był: " + psnr);
        }

        @Test
        // Weryfikacja poprawnego działania na obrazie PNG minimalnej wymaganej rozdzielczości 800x600
        void encode_shouldWork_onMinimumRequiredResolution() throws Exception {
            BufferedImage image800x600 = new BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB);
            String message = "Test na obrazie 800x600";
            stego.encode(message, config, image800x600);
            assertEquals(message, stego.decode(image800x600, config));
        }

        @Test
        // Weryfikacja że osadzanie pliku 50 KB w obrazie FullHD trwa poniżej 3 sekund
        void encode_shouldComplete_within3Seconds_forFullHDImageAnd50KBPayload() throws Exception {
            BufferedImage fullHD = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
            // 50 KB = 51200 bajtów
            String payload = "A".repeat(51200);

            long start   = System.currentTimeMillis();
            stego.encode(payload, config, fullHD);
            long elapsed = System.currentTimeMillis() - start;

            assertTrue(elapsed < 3000,
                    "Osadzanie powinno trwać < 3000 ms, trwało: " + elapsed + " ms");
        }

        @ParameterizedTest(name = "obraz {0}x{1} pikseli")
        @MethodSource("com.stegovault.DecodeTest#imageSizes")
        // Testy regresji: pełny cykl dla obrazów różnych rozmiarów (PNG/BMP)
        void roundTrip_shouldWork_forDifferentImageSizes(int width, int height) throws Exception {
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            stego.encode("test message", config, image);
            assertEquals("test message", stego.decode(image, config));
        }
    }

    // Dostawca rozmiarów obrazów dla testów regresji
    static Stream<Arguments> imageSizes() {
        return Stream.of(
            Arguments.of(100, 100),
            Arguments.of(200, 150),
            Arguments.of(500, 500),
            Arguments.of(1920, 1080)
        );
    }

    // =========================================================================
    // Testy brzegowe: nieprawidłowe dane wejściowe
    // =========================================================================

    @Nested
    class EdgeCaseTests {

        @Test
        // Weryfikacja wyjątku gdy payload jest większy niż pojemność obrazu
        void encode_shouldThrow_whenMessageTooLargeForImage() {
            // 100x100x3 / 8 = 3750 bajtów pojemności, 4000 znaków przekracza limit
            String longMessage = "A".repeat(4000);
            assertThrows(IllegalArgumentException.class,
                    () -> stego.encode(longMessage, config, img));
        }

        @Test
        // Weryfikacja wyjątku przy próbie dekodowania z błędnym hasłem
        void decode_shouldFail_whenWrongPassword() throws Exception {
            stego.encode("secret", config, img);
            EncryptionConfig wrongConfig = new EncryptionConfig("wrong", new byte[16], new byte[16], 65536);
            assertThrows(Exception.class, () -> stego.decode(img, wrongConfig));
        }

        @Test
        // Weryfikacja integralności SHA-256: modyfikacja obrazu po zakodowaniu powinna rzucić wyjątek
        void decode_shouldFail_whenImageTamperedAfterEncode() throws Exception {
            stego.encode("secret", config, img);
            // Celowa modyfikacja piksela symulująca naruszenie integralności
            img.setRGB(0, 0, img.getRGB(0, 0) ^ 0xFF);
            assertThrows(Exception.class, () -> stego.decode(img, config));
        }
    }

    // =========================================================================
    // Testy jednostkowe: HashService
    // =========================================================================

    @Nested
    class HashServiceTests {

        @Test
        // Ten sam input powinien zawsze dawać identyczny hash (determinizm SHA-256)
        void generateHash_shouldReturnConsistentHash() throws Exception {
            byte[] data = "test".getBytes();
            assertArrayEquals(hashService.generateHash(data), hashService.generateHash(data));
        }

        @Test
        // Różne dane wejściowe muszą dawać różne hasze
        void generateHash_shouldReturnDifferentHashForDifferentData() throws Exception {
            assertFalse(Arrays.equals(
                    hashService.generateHash("aaa".getBytes()),
                    hashService.generateHash("bbb".getBytes())
            ));
        }

        @Test
        // Weryfikacja powinna rzucić CryptoException gdy dane nie zgadzają się z hashem
        void verifyHash_shouldThrow_whenDataTampered() throws Exception {
            byte[] data = "original".getBytes();
            byte[] hash = hashService.generateHash(data);
            assertThrows(CryptoException.class,
                    () -> hashService.verifyHash("tampered".getBytes(), hash));
        }

        @Test
        // Weryfikacja powinna przejść gdy dane zgadzają się z hashem
        void verifyHash_shouldNotThrow_whenDataMatchesHash() throws Exception {
            byte[] data = "original".getBytes();
            byte[] hash = hashService.generateHash(data);
            assertArrayEquals(hash, hashService.verifyHash(data, hash));
        }

        @Test
        // Null jako dane wejściowe powinien rzucić IllegalArgumentException
        void generateHash_shouldThrow_whenDataIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> hashService.generateHash(null));
        }

        @Test
        // Null jako hash referencyjny powinien rzucić IllegalArgumentException
        void verifyHash_shouldThrow_whenHashIsNull() throws Exception {
            assertThrows(IllegalArgumentException.class,
                    () -> hashService.verifyHash("data".getBytes(), null));
        }
    }

    // =========================================================================
    // Testy jednostkowe: CryptoService (round-trip szyfrowania)
    // =========================================================================

    @Nested
    class CryptoServiceTests {

        @Test
        // Odszyfrowany tekst musi być identyczny z oryginalnym plaintext
        void decrypt_shouldReturnOriginalPlaintext_afterEncrypt() throws Exception {
            byte[] plaintext = "secret message".getBytes();
            byte[] encrypted = cryptoService.encrypt(plaintext, config);
            assertArrayEquals(plaintext, cryptoService.decrypt(encrypted, config));
        }

        @Test
        // Ten sam plaintext z różnym IV powinien dawać różny ciphertext (tryb CBC)
        void encrypt_shouldProduceDifferentCiphertext_withDifferentIV() throws Exception {
            byte[] plaintext = "same input".getBytes();
            EncryptionConfig config1 = new EncryptionConfig("pass", new byte[16], new byte[16], 65536);
            EncryptionConfig config2 = new EncryptionConfig("pass", new byte[16],
                    new byte[]{1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 65536);
            assertFalse(Arrays.equals(
                    cryptoService.encrypt(plaintext, config1),
                    cryptoService.encrypt(plaintext, config2)
            ));
        }

        @Test
        // Odszyfrowanie z błędnym kluczem powinno rzucić CryptoException
        void decrypt_shouldThrow_whenWrongKey() throws Exception {
            byte[] encrypted = cryptoService.encrypt("data".getBytes(), config);
            EncryptionConfig wrongConfig = new EncryptionConfig("wrongpass", new byte[16], new byte[16], 65536);
            assertThrows(CryptoException.class,
                    () -> cryptoService.decrypt(encrypted, wrongConfig));
        }

        @Test
        // Szyfrowanie nie powinno zwracać oryginalnego plaintext (dane muszą być zaszyfrowane)
        void encrypt_shouldNotReturnPlaintext() throws Exception {
            byte[] plaintext = "secret message".getBytes();
            byte[] encrypted = cryptoService.encrypt(plaintext, config);
            assertFalse(Arrays.equals(plaintext, encrypted));
        }
    }

    // =========================================================================
    // Testy jednostkowe: LSBHelper (kodowanie/dekodowanie bitów)
    // =========================================================================

    @Nested
    class LSBHelperTests {

        @Test
        // Zakodowane bity powinny być identycznie odczytane z obrazu
        void encodeThenDecode_shouldReturnOriginalBits() {
            BufferedImage testImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            int[] bits = {1, 0, 1, 1, 0, 0, 1, 0};
            LSBHelper.encodeBitsToImage(testImg, bits);
            int[] decoded = LSBHelper.decodeBitsFromImage(testImg, bits.length);
            assertArrayEquals(bits, decoded);
        }

        @Test
        // Kodowanie LSB powinno modyfikować wyłącznie najmniej znaczący bit kanału
        void encodeBits_shouldOnlyModifyLSB() {
            BufferedImage testImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            // Biały piksel: kanał czerwony = 0xFF (11111111), LSB = 1
            for (int y = 0; y < 10; y++)
                for (int x = 0; x < 10; x++)
                    testImg.setRGB(x, y, 0xFFFFFF);

            // Zakoduj bit 0 → LSB czerwonego kanału zmieni się z 1 na 0: 0xFF → 0xFE
            LSBHelper.encodeBitsToImage(testImg, new int[]{0, 0, 0});

            int pixel = testImg.getRGB(0, 0);
            int red   = (pixel >> 16) & 0xFF;
            assertEquals(0xFE, red, "LSB kanału czerwonego powinien być 0 po zakodowaniu bitu 0");
        }

        @Test
        // Kodowanie bitów nie powinno wychodzić poza granice obrazu
        void encodeBits_shouldNotThrow_whenBitsExactlyFitImage() {
            // 2x2 obraz = 4 piksele * 3 kanały = 12 bitów pojemności
            BufferedImage smallImg = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
            int[] bits = new int[12];
            Arrays.fill(bits, 1);
            assertDoesNotThrow(() -> LSBHelper.encodeBitsToImage(smallImg, bits));
            assertArrayEquals(bits, LSBHelper.decodeBitsFromImage(smallImg, 12));
        }

        @Test
        // Zakodowanie zer na czarnym obrazie nie powinno zmieniać pikseli
        void encodeBits_shouldNotModifyPixels_whenBitsMatchExistingLSB() {
            // Czarny obraz: wszystkie kanały 0x00, LSB = 0
            BufferedImage blackImg = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            int[] before = blackImg.getRGB(0, 0, 10, 10, null, 0, 10);
            // Zakoduj same zera — LSB już są 0, piksele nie powinny się zmienić
            LSBHelper.encodeBitsToImage(blackImg, new int[]{0, 0, 0, 0, 0, 0});
            int[] after = blackImg.getRGB(0, 0, 10, 10, null, 0, 10);
            assertArrayEquals(before, after);
        }
    }

    // =========================================================================
    // Testy jednostkowe: ValidationService (pojemność, hasło)
    // =========================================================================

    @Nested
    class ValidationServiceTests {

        // --- validateCapacity ---

        @Test
        // Payload mieszczący się w obrazie powinien zwrócić true
        void validateCapacity_shouldReturnTrue_whenPayloadFitsInImage() {
            assertTrue(validation.validateCapacity(img, 100));
        }

        @Test
        // Payload większy niż pojemność obrazu powinien zwrócić false
        void validateCapacity_shouldReturnFalse_whenPayloadTooLarge() {
            assertFalse(validation.validateCapacity(img, 4000));
        }

        @Test
        // Przypadek graniczny: payload dokładnie równy pojemności i jeden bajt powyżej
        void validateCapacity_shouldReturnTrue_whenPayloadExactlyFitsImage() {
            // 100x100x3 = 30000 bitów = 3750 bajtów
            assertTrue(validation.validateCapacity(img, 3750));
            assertFalse(validation.validateCapacity(img, 3751));
        }

        @Test
        // Zerowy payload zawsze mieści się w obrazie
        void validateCapacity_shouldReturnTrue_whenPayloadIsZero() {
            assertTrue(validation.validateCapacity(img, 0));
        }

        @Test
        // Null jako obraz powinien rzucić IllegalArgumentException
        void validateCapacity_shouldThrow_whenImageIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> validation.validateCapacity(null, 100));
        }

        // --- validatePassword ---

        @Test
        // Poprawne hasło spełniające wszystkie wymagania powinno zwrócić true
        void validatePassword_shouldReturnTrue_forValidPassword() {
            assertTrue(validation.validatePassword("Haslo123"));
        }

        @Test
        // Null powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenNull() {
            assertFalse(validation.validatePassword(null));
        }

        @Test
        // Puste hasło powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenEmpty() {
            assertFalse(validation.validatePassword(""));
        }

        @Test
        // Hasło krótsze niż 8 znaków powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenTooShort() {
            assertFalse(validation.validatePassword("Ab1"));
        }

        @Test
        // Brak wielkiej litery powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenNoUpperCase() {
            assertFalse(validation.validatePassword("haslo123"));
        }

        @Test
        // Brak małej litery powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenNoLowerCase() {
            assertFalse(validation.validatePassword("HASLO123"));
        }

        @Test
        // Brak cyfry powinno zwrócić false
        void validatePassword_shouldReturnFalse_whenNoDigit() {
            assertFalse(validation.validatePassword("HasloAbc"));
        }
    }

    // =========================================================================
    // Testy jednostkowe: PayloadHelper (budowanie i parsowanie payloadu)
    // =========================================================================

    @Nested
    class PayloadHelperTests {

        @Test
        // Zbudowany payload powinien mieć poprawny rozmiar
        void buildPayload_shouldReturnCorrectSize() {
            byte[] encrypted = new byte[100];
            byte[] salt      = new byte[16];
            byte[] iv        = new byte[16];
            byte[] hash      = new byte[32];

            byte[] payload = PayloadHelper.buildPayload(encrypted, salt, iv, hash);
            // 4 (długość) + 16 (sól) + 16 (IV) + 32 (hash) + 100 (dane) = 168
            assertEquals(168, payload.length);
        }

        @Test
        // Sparsowany payload powinien zawierać oryginalne komponenty
        void parsePayload_shouldReturnOriginalComponents() {
            byte[] encrypted = {1, 2, 3, 4};
            byte[] salt      = new byte[16]; salt[0] = 7;
            byte[] iv        = new byte[16]; iv[0]   = 9;
            byte[] hash      = new byte[32]; hash[0] = 5;

            byte[] payload      = PayloadHelper.buildPayload(encrypted, salt, iv, hash);
            ParsedPayload parsed = PayloadHelper.parsePayload(payload);

            assertArrayEquals(encrypted, parsed.encryptedData());
            assertArrayEquals(salt,      parsed.salt());
            assertArrayEquals(iv,        parsed.iv());
            assertArrayEquals(hash,      parsed.hash());
        }

        @Test
        // Parse powinien rzucić wyjątek dla payload za krótkiego
        void parsePayload_shouldThrow_whenPayloadTooShort() {
            byte[] tooShort = new byte[10];
            assertThrows(IllegalArgumentException.class,
                    () -> PayloadHelper.parsePayload(tooShort));
        }

        @Test
        // Parse powinien rzucić wyjątek dla null
        void parsePayload_shouldThrow_whenPayloadIsNull() {
            assertThrows(IllegalArgumentException.class,
                    () -> PayloadHelper.parsePayload(null));
        }

        @Test
        // buildPayload → parsePayload round-trip powinien być symetryczny
        void buildAndParse_shouldBeSymmetric() {
            byte[] encrypted = "tajne dane".getBytes();
            byte[] salt      = new byte[16]; salt[3]  = 42;
            byte[] iv        = new byte[16]; iv[5]    = 99;
            byte[] hash      = new byte[32]; hash[10] = 13;

            ParsedPayload parsed = PayloadHelper.parsePayload(
                    PayloadHelper.buildPayload(encrypted, salt, iv, hash));

            assertArrayEquals(encrypted, parsed.encryptedData());
            assertArrayEquals(salt,      parsed.salt());
            assertArrayEquals(iv,        parsed.iv());
            assertArrayEquals(hash,      parsed.hash());
        }
    }
}