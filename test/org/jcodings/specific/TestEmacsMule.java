package org.jcodings.specific;

import static org.junit.Assert.assertEquals;

import org.jcodings.Encoding;
import org.jcodings.exception.ErrorCodes;
import org.junit.Test;

public class TestEmacsMule {
    @Test
    public void testRightAdjustCharHeadAscii() {
        byte[] str = new byte[]{(byte)'a', (byte)'b', (byte)'c', (byte)',', (byte)'d', (byte)'e', (byte)'f'};

        int t = EmacsMuleEncoding.INSTANCE.rightAdjustCharHead(str, 0, 3, 7);
        assertEquals("rightAdjustCharHead did not adjust properly", 3, t);
    }

    @Test
    public void testCodeToMbcLength() {
        Encoding enc = EmacsMuleEncoding.INSTANCE;
        // an Emacs-Mule code is the character's bytes read as a big-endian integer
        assertEquals(1, enc.codeToMbcLength(0x61));       // "a"
        assertEquals(2, enc.codeToMbcLength(0x86cd));     // Greek "Ν": 0x86 = ISO-8859-7, then 0xCD
        assertEquals(3, enc.codeToMbcLength(0x92a4a2));   // Japanese "あ": 0x92 = JIS X 0208, then 0x24 0x22 | 0x80
        assertEquals(4, enc.codeToMbcLength(0x9cf0a1a1)); // 0x9C = private 2-dimension charset prefix, 0xF0 = charset id
        assertEquals(ErrorCodes.ERR_INVALID_CODE_POINT_VALUE, enc.codeToMbcLength(0x80));
    }

    @Test
    public void testCodeToMbcLengthMatchesCharLength() {
        Encoding enc = EmacsMuleEncoding.INSTANCE;
        byte[][] chars = {
            {(byte)0x86, (byte)0xcd},                         // Greek "Ν"
            {(byte)0x92, (byte)0xa4, (byte)0xa2},             // Japanese "あ"
            {(byte)0x9c, (byte)0xf0, (byte)0xa1, (byte)0xa1}, // private 2-dimension charset
        };
        for (byte[] c : chars) {
            int code = enc.mbcToCode(c, 0, c.length);
            assertEquals(c.length, enc.length(c, 0, c.length));
            assertEquals(c.length, enc.codeToMbcLength(code));
            byte[] out = new byte[4];
            assertEquals(c.length, enc.codeToMbc(code, out, 0));
        }
    }
}
