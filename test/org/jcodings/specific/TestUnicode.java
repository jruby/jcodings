package org.jcodings.specific;

import org.jcodings.Encoding;
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

    @Test
    public void testUnicodeProperties() throws Exception {
        Encoding enc = UTF16BEEncoding.INSTANCE;
        byte[]str = "\000B\000\000".getBytes("iso-8859-1");
        int code = enc.mbcToCode(str, 0, str.length);
        byte[]prop = "\000u\000p\000p\000e\000r".getBytes("iso-8859-1");
        int ctype = enc.propertyNameToCType(prop, 0, prop.length);
        assertTrue(enc.isCodeCType(code, ctype));
    }
}
