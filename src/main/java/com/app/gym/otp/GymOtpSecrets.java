package com.app.gym.otp;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

public final class GymOtpSecrets {

    private static final SecureRandom RANDOM = new SecureRandom();

    private GymOtpSecrets() {
    }

    /*
     * Generates a 256-bit random token.
     * Hex encoding produces a 64-character string.
     */
    public static String token() {

        byte[] bytes = new byte[32];

        RANDOM.nextBytes(bytes);

        return hex(bytes);
    }

    /*
     * Creates a SHA-256 hash for high-entropy tokens
     * and browser-session binding values.
     */
    public static String hash(String value) {

        try {

            MessageDigest digest =
                    MessageDigest.getInstance("SHA-256");

            byte[] hashedBytes = digest.digest(
                    value.getBytes(StandardCharsets.UTF_8)
            );

            return hex(hashedBytes);

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 is not available",
                    exception
            );
        }
    }

    /*
     * Compares a submitted token against its stored hash.
     */
    public static boolean matches(
            String value,
            String expectedHash) {

        if (value == null || expectedHash == null) {
            return false;
        }

        byte[] actualHashBytes = hash(value)
                .getBytes(StandardCharsets.US_ASCII);

        byte[] expectedHashBytes = expectedHash
                .getBytes(StandardCharsets.US_ASCII);

        return MessageDigest.isEqual(
                actualHashBytes,
                expectedHashBytes
        );
    }

    private static String hex(byte[] bytes) {

        StringBuilder result =
                new StringBuilder(bytes.length * 2);

        for (byte value : bytes) {

            result.append(
                    String.format("%02x", value & 0xff)
            );
        }

        return result.toString();
    }
}