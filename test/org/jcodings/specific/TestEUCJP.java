package org.jcodings.specific;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;

import org.jcodings.IntHolder;
import org.junit.Test;

public class TestEUCJP {
    @Test
    public void testGetCharset() {
        Charset EUCJP = Charset.forName("EUC-JP");
        assumeTrue(EUCJP != null);
        assertEquals("EUCJPEncoding.charset should be 'EUC-JP'",
                EUCJP,
                EUCJPEncoding.INSTANCE.getCharset());
    }

    @Test
    public void testGetCharsetName() {
        assertEquals("EUCJPEncoding.charsetName should be 'EUC-JP'",
                "EUC-JP",
                EUCJPEncoding.INSTANCE.getCharsetName());
    }

    @Test
    public void testCaseFold() {
        EUCJPEncoding enc = EUCJPEncoding.INSTANCE;
        byte [] lowerSrc = new byte[]{(byte)0xA3, (byte)0xE1};
        byte [] upperSrc = new byte[]{(byte)0xA3, (byte)0xC1};
        byte [] lower = new byte[2];
        IntHolder pp = new IntHolder();

        pp.value = 0;
        enc.mbcCaseFold(0, lowerSrc, pp, 2, lower);
        assertArrayEquals(lowerSrc, lower);

        pp.value = 0;
        enc.mbcCaseFold(0, upperSrc, pp, 2, lower);
        assertArrayEquals(lowerSrc, lower);
    }
}
