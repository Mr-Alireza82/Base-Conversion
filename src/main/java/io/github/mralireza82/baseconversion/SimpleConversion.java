package io.github.mralireza82.baseconversion;

import java.math.BigInteger;
import java.util.Objects;

/**
 * The simple methods, exactly as you would do them on paper. Slower than {@link BaseConversion} but each
 * step is easy to follow; they exist for learning and as a second opinion in tests.
 *
 * <ul>
 *   <li>Digits to number: add up each digit times its weight (base to the power of its position).</li>
 *   <li>Number to digits: take away the biggest weight that fits, as often as it fits, then the next one.</li>
 *   <li>Any base to any base: go through the number (in practice, through decimal) in two steps.</li>
 * </ul>
 */
public final class SimpleConversion {
    private SimpleConversion() {
    }

    /** Sum of weights: "1A" in hexadecimal = 1 x 16^1 + 10 x 16^0 = 26. */
    public static BigInteger toNumber(String digits, Base base) {
        base.requireDigits(digits);
        BigInteger radix = BigInteger.valueOf(base.radix());
        BigInteger total = BigInteger.ZERO;
        int lastPosition = digits.length() - 1;
        for (int i = 0; i <= lastPosition; i++) {
            int position = lastPosition - i;
            BigInteger weight = radix.pow(position);
            total = total.add(weight.multiply(BigInteger.valueOf(base.valueOf(digits.charAt(i)))));
        }
        return total;
    }

    /** Subtracting weights: 26 in binary: 16 fits once, 8 once, 4 none, 2 once, 1 none, giving "11010". */
    public static String toDigits(BigInteger number, Base base) {
        BaseConversion.requireNotNegative(number);
        Objects.requireNonNull(base, "base");
        BigInteger radix = BigInteger.valueOf(base.radix());
        BigInteger weight = BigInteger.ONE;
        while (weight.multiply(radix).compareTo(number) <= 0) {
            weight = weight.multiply(radix);
        }
        StringBuilder digits = new StringBuilder();
        BigInteger rest = number;
        while (weight.signum() > 0) {
            int times = 0;
            while (rest.compareTo(weight) >= 0) {
                rest = rest.subtract(weight);
                times++;
            }
            digits.append(base.digitOf(times));
            weight = weight.divide(radix);
        }
        return digits.toString();
    }

    /** Two steps through the number: from-base to number, then number to the target base. */
    public static String convert(String digits, Base from, Base to) {
        return toDigits(toNumber(digits, from), to);
    }
}
