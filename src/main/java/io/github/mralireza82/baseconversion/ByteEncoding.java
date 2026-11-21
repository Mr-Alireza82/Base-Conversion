package io.github.mralireza82.baseconversion;

import java.util.Objects;

/**
 * Writes <strong>data</strong> (bytes) as hexadecimal or binary text and reads it back. Unlike numbers,
 * every byte counts, leading zeros included: {@code "00FF"} is two bytes, {@code "FF"} is one.
 * One byte is always exactly 2 hex digits or 8 binary digits. Java's {@code byte} is signed
 * (-128..127); the bit pattern is what matters, so {@code (byte) 0xFF} is written {@code "FF"}, never -1.
 */
public final class ByteEncoding {
    private ByteEncoding() {
    }

    /** {@code {0x00, 0xAF}} becomes {@code "00AF"}. */
    public static String toHex(byte[] data) {
        Objects.requireNonNull(data, "data");
        StringBuilder text = new StringBuilder(data.length * 2);
        for (byte b : data) {
            int value = unsigned(b);
            text.append(Base.HEXADECIMAL.digitOf(value >> 4)).append(Base.HEXADECIMAL.digitOf(value & 0x0F));
        }
        return text.toString();
    }

    /** {@code "00af"} becomes {@code {0x00, 0xAF}}. Needs an even number of hex digits. */
    public static byte[] fromHex(String hex) {
        Objects.requireNonNull(hex, "hex");
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("hex data needs 2 digits per byte, got " + hex.length() + " digits");
        }
        byte[] data = new byte[hex.length() / 2];
        for (int i = 0; i < data.length; i++) {
            int high = Base.HEXADECIMAL.valueOf(hex.charAt(2 * i));
            int low = Base.HEXADECIMAL.valueOf(hex.charAt(2 * i + 1));
            data[i] = (byte) (high << 4 | low);
        }
        return data;
    }

    /** {@code {0x05}} becomes {@code "00000101"}: eight bits per byte, most significant bit first. */
    public static String toBits(byte[] data) {
        Objects.requireNonNull(data, "data");
        StringBuilder text = new StringBuilder(data.length * 8);
        for (byte b : data) {
            BaseConversion.appendBits(text, unsigned(b), 8);
        }
        return text.toString();
    }

    /** {@code "00000101"} becomes {@code {0x05}}. Needs a multiple of 8 binary digits. */
    public static byte[] fromBits(String bits) {
        Objects.requireNonNull(bits, "bits");
        if (bits.length() % 8 != 0) {
            throw new IllegalArgumentException("binary data needs 8 digits per byte, got " + bits.length() + " digits");
        }
        byte[] data = new byte[bits.length() / 8];
        for (int i = 0; i < bits.length(); i++) {
            int bit = Base.BINARY.valueOf(bits.charAt(i));
            data[i / 8] = (byte) (data[i / 8] << 1 | bit);
        }
        return data;
    }

    /** The byte's bits read as 0..255 instead of Java's signed -128..127. */
    public static int unsigned(byte b) {
        return b & 0xFF;
    }
}
