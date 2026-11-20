package com.psp.pos.shared.baseconversion;

import java.util.Objects;

/**
 * The four bases (radixes) used in computing. A base only decides how a number is <em>written</em>;
 * the number itself does not change. Digits above 9 are the letters {@code A}-{@code F}; input may be
 * upper or lower case, output is always upper case (the convention of payment specifications).
 */
public enum Base {
    /** Base 2: digits 0-1. One digit is exactly one bit. */
    BINARY(2, 1, "0b"),
    /** Base 8: digits 0-7. One digit is exactly three bits. */
    OCTAL(8, 3, "0o"),
    /** Base 10: digits 0-9. Not a power of two, so a digit is not a whole number of bits. */
    DECIMAL(10, 0, ""),
    /** Base 16: digits 0-9 and A-F. One digit is exactly four bits (a nibble). */
    HEXADECIMAL(16, 4, "0x");

    private static final String DIGITS = "0123456789ABCDEF";

    private final int radix;
    private final int bitsPerDigit;
    private final String prefix;

    Base(int radix, int bitsPerDigit, String prefix) {
        this.radix = radix;
        this.bitsPerDigit = bitsPerDigit;
        this.prefix = prefix;
    }

    /** How many different digits this base has: 2, 8, 10 or 16. */
    public int radix() {
        return radix;
    }

    /** True for 2, 8 and 16: each digit is a fixed group of bits, so conversion can skip arithmetic. */
    public boolean isPowerOfTwo() {
        return bitsPerDigit > 0;
    }

    /** Bits per digit: 1, 3 or 4; 0 for decimal (log2(10) is not a whole number). */
    public int bitsPerDigit() {
        return bitsPerDigit;
    }

    /** The common source-code prefix ({@code 0b}, {@code 0o}, {@code 0x}); empty for decimal. Never parsed. */
    public String prefix() {
        return prefix;
    }

    /** The value of one digit character, e.g. {@code 'F'} in hexadecimal is 15. */
    public int valueOf(char digit) {
        int value = DIGITS.indexOf(Character.toUpperCase(digit));
        if (value < 0 || value >= radix) {
            throw new IllegalArgumentException("'" + digit + "' is not a " + name().toLowerCase() + " digit");
        }
        return value;
    }

    /** The digit character for a value, e.g. 15 in hexadecimal is {@code 'F'}. */
    public char digitOf(int value) {
        if (value < 0 || value >= radix) {
            throw new IllegalArgumentException(value + " is not a single " + name().toLowerCase() + " digit");
        }
        return DIGITS.charAt(value);
    }

    /** Checks that {@code digits} is a non-empty run of this base's digits (no sign, prefix or spaces). */
    void requireDigits(String digits) {
        Objects.requireNonNull(digits, "digits");
        if (digits.isEmpty()) {
            throw new IllegalArgumentException("a number needs at least one digit");
        }
        for (int i = 0; i < digits.length(); i++) {
            valueOf(digits.charAt(i));
        }
    }
}
