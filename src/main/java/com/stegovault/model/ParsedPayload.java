package com.stegovault.model;


/**
 * Represents a parsed payload extracted from the input data.
 * Contains information ready for further processing.
 */
public record ParsedPayload(
        byte[] salt,
        byte[] iv,
        byte[] hash,
        byte[] encryptedData
) {
}
