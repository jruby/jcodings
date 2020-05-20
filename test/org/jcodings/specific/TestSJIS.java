package org.jcodings.specific;

import static org.junit.Assert.assertArrayEquals;

import org.jcodings.IntHolder;
import org.junit.Test;

public class TestSJIS {

    @Test
    public void testCaseFold() {
        SJISEncoding enc = SJISEncoding.INSTANCE;
        byte [] lowerSrc = new byte[]{(byte)0x82, (byte)0x81};
        byte [] upperSrc = new byte[]{(byte)0x82, (byte)0x60};
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