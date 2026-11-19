package org.jcodings.specific;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.jcodings.IntHolder;
import org.junit.Test;

public class TestGB18030 {
    @Test
    public void testCaseFoldWhenLogicalEndSplitsCharacter() {
        byte[][] input = {
            {(byte)0x81, (byte)0x40},
            {(byte)0x81, (byte)0x30, (byte)0x81, (byte)0x30}
        };
        int[] lengths = {2, 4};

        for (int i = 0; i < input.length; i++) {
            byte[] folded = new byte[4];
            IntHolder p = new IntHolder();
            assertEquals(lengths[i], GB18030Encoding.INSTANCE.mbcCaseFold(
                    0, input[i], p, 1, folded));
            assertEquals(lengths[i], p.value);
            assertArrayEquals(input[i], java.util.Arrays.copyOf(folded, lengths[i]));
        }
    }
}
