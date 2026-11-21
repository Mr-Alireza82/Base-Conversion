package io.github.mralireza82.baseconversion;

import java.math.BigInteger;
import java.util.Objects;

/**
 * Converts <strong>numbers</strong> between bases 2, 8, 10 and 16 with the quick methods. Use this one.
 *
 * <ul>
 *   <li>Digits to number: Horner's method (multiply by the base, add the next digit).</li>
 *   <li>Number to digits: repeated division (the remainders are the digits, last first).</li>
 *   <li>Between 2, 8 and 16: regrouping bits, no arithmetic at all.</li>
 * </ul>
 *
 * A number has no leading zeros: {@code "00FF"} and {@code "FF"} are the same number, 255. When the
 * length matters (keys, blocks, messages) the value is data, not a number: use {@link ByteEncoding}.
 * Numbers have any size and are never negative. {@link SimpleConversion} holds the slow, easy-to-follow
 * methods for learning; both give identical results.
 */
public final class BaseConversion {
    private static final BigInteger[] SMALL = new BigInteger[16];

    static {
        for (int i = 0; i < SMALL.length; i++) {
            SMALL[i] = BigInteger.valueOf(i);
        }
    }

    private BaseConversion() {
    }

    /** Reads digits written in {@code base} as a number. {@code toNumber("FF", HEXADECIMAL)} is 255. */
    public static BigInteger toNumber(String digits, Base base) {
        base.requireDigits(digits);
        BigInteger radix = BigInteger.valueOf(base.radix());
        BigInteger number = BigInteger.ZERO;
        for (int i = 0; i < digits.length(); i++) {
            number = number.multiply(radix).add(SMALL[base.valueOf(digits.charAt(i))]);
        }
        return number;
    }

    /** Writes a number in {@code base}. {@code toDigits(255, BINARY)} is {@code "11111111"}. */
    public static String toDigits(BigInteger number, Base base) {
        requireNotNegative(number);
        Objects.requireNonNull(base, "base");
        if (number.signum() == 0) {
            return "0";
        }
        BigInteger radix = BigInteger.valueOf(base.radix());
        StringBuilder digits = new StringBuilder();
        BigInteger rest = number;
        while (rest.signum() > 0) {
            BigInteger[] quotientAndRemainder = rest.divideAndRemainder(radix);
            digits.append(base.digitOf(quotientAndRemainder[1].intValue()));
            rest = quotientAndRemainder[0];
        }
        return digits.reverse().toString();
    }

    /** Rewrites a number from one base to another, without leading zeros. */
    public static String convert(String digits, Base from, Base to) {
        Objects.requireNonNull(to, "to");
        if (from.isPowerOfTwo() && to.isPowerOfTwo()) {
            from.requireDigits(digits);
            return regroupBits(digits, from, to);
        }
        return toDigits(toNumber(digits, from), to);
    }

    /** Expands each digit to its bits, then cuts the bits into groups of the target size from the right. */
    private static String regroupBits(String digits, Base from, Base to) {
        StringBuilder bits = new StringBuilder(digits.length() * from.bitsPerDigit());
        for (int i = 0; i < digits.length(); i++) {
            appendBits(bits, from.valueOf(digits.charAt(i)), from.bitsPerDigit());
        }
        int groupSize = to.bitsPerDigit();
        int padding = (groupSize - bits.length() % groupSize) % groupSize;
        for (int i = 0; i < padding; i++) {
            bits.insert(0, '0');
        }
        StringBuilder result = new StringBuilder(bits.length() / groupSize);
        for (int start = 0; start < bits.length(); start += groupSize) {
            int value = 0;
            for (int i = start; i < start + groupSize; i++) {
                value = value * 2 + (bits.charAt(i) - '0');
            }
            result.append(to.digitOf(value));
        }
        return withoutLeadingZeros(result);
    }

    static void appendBits(StringBuilder target, int value, int width) {
        for (int bit = width - 1; bit >= 0; bit--) {
            target.append((value >> bit & 1) == 1 ? '1' : '0');
        }
    }

    static String withoutLeadingZeros(CharSequence digits) {
        int first = 0;
        while (first < digits.length() - 1 && digits.charAt(first) == '0') {
            first++;
        }
        return digits.subSequence(first, digits.length()).toString();
    }

    static void requireNotNegative(BigInteger number) {
        Objects.requireNonNull(number, "number");
        if (number.signum() < 0) {
            throw new IllegalArgumentException("negative numbers are not supported: " + number);
        }
    }
}
