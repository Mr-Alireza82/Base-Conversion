package io.github.mralireza82.baseconversion;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigInteger;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Every result is checked against the JDK's own {@link BigInteger} conversion, and simple against quick. */
class BaseConversionTest {

    @ParameterizedTest(name = "{0} in {1} is {2} in {3}")
    @CsvSource({
            // The examples used in the article.
            "1101,       BINARY,      13,     DECIMAL",
            "11010110,   BINARY,      326,    OCTAL",
            "11010110,   BINARY,      D6,     HEXADECIMAL",
            "326,        OCTAL,       214,    DECIMAL",
            "326,        OCTAL,       11010110, BINARY",
            "326,        OCTAL,       D6,     HEXADECIMAL",
            "214,        DECIMAL,     11010110, BINARY",
            "214,        DECIMAL,     326,    OCTAL",
            "214,        DECIMAL,     D6,     HEXADECIMAL",
            "D6,         HEXADECIMAL, 214,    DECIMAL",
            "d6,         HEXADECIMAL, 11010110, BINARY",
            "D6,         HEXADECIMAL, 326,    OCTAL",
            "0,          HEXADECIMAL, 0,      BINARY",
            "000FF,      HEXADECIMAL, 255,    DECIMAL",
            "0000,       BINARY,      0,      OCTAL",
    })
    void examples(String digits, Base from, String expected, Base to) {
        assertEquals(expected, BaseConversion.convert(digits, from, to));
        assertEquals(expected, SimpleConversion.convert(digits, from, to));
    }

    @Test
    void randomNumbersMatchTheJdkInEveryDirection() {
        Random random = new Random(42);
        for (int round = 0; round < 2_000; round++) {
            BigInteger number = new BigInteger(1 + random.nextInt(200), random);
            for (Base from : Base.values()) {
                String digits = number.toString(from.radix()).toUpperCase();
                assertEquals(number, BaseConversion.toNumber(digits, from));
                assertEquals(number, SimpleConversion.toNumber(digits, from));
                assertEquals(digits, BaseConversion.toDigits(number, from));
                assertEquals(digits, SimpleConversion.toDigits(number, from));
                for (Base to : Base.values()) {
                    String expected = number.toString(to.radix()).toUpperCase();
                    assertEquals(expected, BaseConversion.convert(digits, from, to), from + " -> " + to);
                }
            }
        }
    }

    @Test
    void wrongDigitsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.toNumber("102", Base.BINARY));
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.toNumber("8", Base.OCTAL));
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.toNumber("G", Base.HEXADECIMAL));
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.convert("0xFF", Base.HEXADECIMAL, Base.BINARY));
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.convert("", Base.DECIMAL, Base.BINARY));
        assertThrows(IllegalArgumentException.class, () -> BaseConversion.toDigits(BigInteger.valueOf(-1), Base.BINARY));
    }

    @Test
    void bytesKeepTheirLengthAndLeadingZeros() {
        byte[] data = {0x00, (byte) 0xAF, 0x05, (byte) 0xFF};

        assertEquals("00AF05FF", ByteEncoding.toHex(data));
        assertArrayEquals(data, ByteEncoding.fromHex("00af05ff"));
        assertEquals("00000000101011110000010111111111", ByteEncoding.toBits(data));
        assertArrayEquals(data, ByteEncoding.fromBits(ByteEncoding.toBits(data)));
        assertEquals(255, ByteEncoding.unsigned((byte) 0xFF));
        assertEquals(0, ByteEncoding.toHex(new byte[0]).length());
    }

    @Test
    void incompleteBytesAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> ByteEncoding.fromHex("ABC"));
        assertThrows(IllegalArgumentException.class, () -> ByteEncoding.fromBits("1010"));
        assertThrows(IllegalArgumentException.class, () -> ByteEncoding.fromHex("ZZ"));
    }
}
