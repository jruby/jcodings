package org.jcodings.specific;

import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.junit.Test;

import static junit.framework.Assert.*;

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
    }

    String caseMap(String fromS, int flags) throws Exception {
        int CASE_MAPPING_ADDITIONAL_LENGTH = 20;
        byte[]from = fromS.getBytes("utf-8");
        IntHolder fromP = new IntHolder();
        fromP.value = 0;
        byte[]to = new byte[from.length + CASE_MAPPING_ADDITIONAL_LENGTH];
        IntHolder flagP = new IntHolder();
        flagP.value = flags;
        int len = enc.caseMap(flagP, from, fromP, from.length, to, 0, to.length);
        return new String(to, 0, len);
    }

    @Test
    public void testCaseMap() throws Exception {
        assertTrue(caseMap("äöü", Config.CASE_UPCASE).equals("ÄÖÜ"));
        assertTrue(caseMap("ÄÖÜ", Config.CASE_UPCASE).equals("ÄÖÜ"));
        assertTrue(caseMap("ÄÖÜ", Config.CASE_DOWNCASE).equals("äöü"));
        assertTrue(caseMap("äöü", Config.CASE_DOWNCASE).equals("äöü"));
        assertTrue(caseMap("aÄbÖcÜ", Config.CASE_DOWNCASE).equals("aäböcü"));
        assertTrue(caseMap("aäböcü", Config.CASE_UPCASE).equals("AÄBÖCÜ"));
        assertTrue(caseMap("aäböcü", Config.CASE_UPCASE | Config.CASE_ASCII_ONLY).equals("AäBöCü"));
        assertTrue(caseMap("AÄBÖCÜ", Config.CASE_DOWNCASE | Config.CASE_ASCII_ONLY).equals("aÄbÖcÜ"));
    }
}
