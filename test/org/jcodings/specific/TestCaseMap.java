package org.jcodings.specific;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.IntHolder;
import org.junit.Test;

public class TestCaseMap {
    String caseMap(Encoding enc, String fromS, int flags) throws Exception {
        int CASE_MAPPING_ADDITIONAL_LENGTH = 20;
        byte[]from = fromS.getBytes(enc.toString());
        IntHolder fromP = new IntHolder();
        fromP.value = 0;
        byte[]to = new byte[from.length + CASE_MAPPING_ADDITIONAL_LENGTH];
        IntHolder flagP = new IntHolder();
        flagP.value = flags;
        int len = enc.caseMap(flagP, from, fromP, from.length, to, 0, to.length);
        return new String(to, 0, len, enc.toString());
    }

    @Test
    public void testUnicodeCaseMap() throws Exception {
        Encoding enc = UTF8Encoding.INSTANCE;
        assertTrue(caseMap(enc, "äöü", Config.CASE_UPCASE).equals("ÄÖÜ"));
        assertTrue(caseMap(enc, "ÄÖÜ", Config.CASE_UPCASE).equals("ÄÖÜ"));
        assertTrue(caseMap(enc, "ÄÖÜ", Config.CASE_DOWNCASE).equals("äöü"));
        assertTrue(caseMap(enc, "äöü", Config.CASE_DOWNCASE).equals("äöü"));
        assertTrue(caseMap(enc, "aÄbÖcÜ", Config.CASE_DOWNCASE).equals("aäböcü"));
        assertTrue(caseMap(enc, "aäböcü", Config.CASE_UPCASE).equals("AÄBÖCÜ"));
        assertTrue(caseMap(enc, "aäböcü", Config.CASE_UPCASE | Config.CASE_ASCII_ONLY).equals("AäBöCü"));
        assertTrue(caseMap(enc, "AÄBÖCÜ", Config.CASE_DOWNCASE | Config.CASE_ASCII_ONLY).equals("aÄbÖcÜ"));
    }

    @Test
    public void testISOCaseMap() throws Exception {
        List<Encoding> list = Arrays.<Encoding>asList(ISO8859_1Encoding.INSTANCE, ISO8859_2Encoding.INSTANCE, ISO8859_3Encoding.INSTANCE);
        for (Encoding enc: list) {
            assertTrue(caseMap(enc, "ß", Config.CASE_UPCASE).equals("SS"));
            assertTrue(caseMap(enc, "ß", Config.CASE_DOWNCASE).equals("ß"));
        }
    }
}
