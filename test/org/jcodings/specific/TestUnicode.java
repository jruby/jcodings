package org.jcodings.specific;

import org.junit.Test;

import static junit.framework.Assert.*;

public class TestUnicode {
    @Test
    public void testUnicodeLength() throws Exception {
        byte[] utf8Bytes = "mØØse".getBytes("UTF-8");

        assertEquals(7, utf8Bytes.length);
        assertEquals(5, UTF8Encoding.INSTANCE.strLength(utf8Bytes, 0, 7));
        assertEquals(2, UTF8Encoding.INSTANCE.length(utf8Bytes[1]));
        assertEquals('Ø', UTF8Encoding.INSTANCE.mbcToCode(utf8Bytes, 1, 3));
    }
}
