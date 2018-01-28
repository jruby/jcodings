package org.jcodings.specific;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

import org.jcodings.Encoding;
import org.jcodings.constants.CharacterType;
import org.junit.Test;

public class TestUnicode {
    final Encoding enc = UTF8Encoding.INSTANCE;

    @Test
    public void testUnicodeLength() throws Exception {
        byte[] utf8Bytes = "mØØse".getBytes("UTF-8");

        assertEquals(7, utf8Bytes.length);
        assertEquals(5, enc.strLength(utf8Bytes, 0, 7));
        assertEquals(2, enc.length(utf8Bytes[1]));
        assertEquals('Ø', enc.mbcToCode(utf8Bytes, 1, 3));
    }

    @Test
    public void testUnicodeProperties() throws Exception {
        Encoding enc = UTF16BEEncoding.INSTANCE;
        byte[]str = "\000B\000\000".getBytes("iso-8859-1");
        int code = enc.mbcToCode(str, 0, str.length);
        byte[]prop = "\000u\000p\000p\000e\000r".getBytes("iso-8859-1");
        int ctype = enc.propertyNameToCType(prop, 0, prop.length);
        assertTrue(enc.isCodeCType(code, ctype));

        Encoding utf8 = UTF8Encoding.INSTANCE;
        byte[]ascii = "ascii".getBytes();
        int a_ctype = utf8.propertyNameToCType(ascii, 0, ascii.length);
        assertEquals(a_ctype, CharacterType.ASCII);
    }

    @Test
    public void testCodeToMbcLength() throws Exception {
        assertEquals(enc.codeToMbcLength(0x01), 1);
        assertEquals(enc.codeToMbcLength(0x7f), 1);
        assertEquals(enc.codeToMbcLength(0x101), 2);
        assertEquals(enc.codeToMbcLength(0x1020), 3);
        assertEquals(enc.codeToMbcLength(0x1F608), 4);
        assertEquals(enc.codeToMbcLength(0xfffffffe), 1); // USE_INVALID_CODE_SCHEME
        assertEquals(enc.codeToMbcLength(0xffffffff), 1); // USE_INVALID_CODE_SCHEME
    }
}
