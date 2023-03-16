package org.jcodings.specific;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.jcodings.Encoding;
import org.jcodings.constants.CharacterType;
import java.nio.charset.StandardCharsets;
import org.junit.Test;

public class TestCESU8 {
    final Encoding enc = CESU8Encoding.INSTANCE;

    @Test
    public void testUnicodeLength6byteChar() throws Exception {
        byte[]bytes = "\u00ed\u00a0\u0080\u00ed\u00b0\u0080".getBytes("ISO-8859-1");
        assertEquals(1, enc.strLength(bytes, 0, bytes.length));
    }

    @Test
    public void testPrevCharHead6byteChar() throws Exception {
        byte[]bytes = "\u00ed\u00a0\u0080\u00ed\u00b0\u0080".getBytes("ISO-8859-1");
        assertEquals(0, enc.prevCharHead(bytes, 0, bytes.length, bytes.length));
    }

    @Test
    public void testUnicodeLength() throws Exception {
        byte[]bytes = "test\u00C5\u0099".getBytes();
        assertEquals(6, enc.strLength(bytes, 0, bytes.length));
    }

    @Test
    public void testUnicodeLengthLong() throws Exception {
        byte[]bytes = ("\u00C5\u0099\u00C5\u00A1\u00C4\u009B\u00C5\u0099\u00C5\u00A1\u00C4\u009B\u00C5\u0099\u00C3\u00A9\u00C4" +
        "\u009B\u00C3\u00BD\u00C5\u0099\u00C5\u00A1\u00C4\u009B\u00C3\u00A9\u00C4\u009B\u00C3\u00A9\u00C5\u00BE\u00C4\u009B\u00C5\u00A1" +
        "\u00C3\u00A9\u00C5\u00BE\u00C4\u009B\u00C5\u00BE\u00C3\u00A9\u00C4\u009B\u00C5\u00A1").getBytes("ISO-8859-1");
        assertEquals(26, enc.strLength(bytes, 0, bytes.length));
    }

    @Test
    public void testCodeToMbcLength() throws Exception {
        assertEquals(enc.codeToMbcLength(0x01), 1);
        assertEquals(enc.codeToMbcLength(0x1F608), 6);
    }

    @Test
    public void testMbcToCode() throws Exception {
        assertEquals('Ø', enc.mbcToCode("mØØse".getBytes("UTF-8"), 1, 3));
    }

    @Test
    public void testEncodingLoad() throws Exception {
        assertEquals(CESU8Encoding.INSTANCE, Encoding.load("CESU8"));
    }
}
