package org.jcodings.specific;

import static junit.framework.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.jcodings.Config;
import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.IntHolder;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.junit.Test;

public class TestCaseMap {
    String caseMap(Encoding enc, String transcode, String fromS, int flags) throws Exception {
        int CASE_MAPPING_ADDITIONAL_LENGTH = 20;
        byte[]from = fromS.getBytes(transcode);
        IntHolder fromP = new IntHolder();
        fromP.value = 0;
        byte[]to = new byte[from.length + CASE_MAPPING_ADDITIONAL_LENGTH];
        IntHolder flagP = new IntHolder();
        flagP.value = flags;
        int len = enc.caseMap(flagP, from, fromP, from.length, to, 0, to.length);
        return new String(to, 0, len, transcode);
    }

    String caseMap(Encoding enc, String fromS, int flags) throws Exception {
        return caseMap(enc, enc.toString(), fromS, flags);
    }

    @Test
    public void testASCIICaseMap() throws Exception {
        CaseInsensitiveBytesHash<EncodingDB.Entry> list = EncodingDB.getEncodings();
        String transcodeFrom = "iso-8859-1";
        for (EncodingDB.Entry entry: list) {
            Encoding enc = entry.getEncoding();
            if (enc.isAsciiCompatible()) {
                assertTrue(caseMap(enc, transcodeFrom, "abcdefghijklmnopqrstuvwxyz", Config.CASE_UPCASE).equals("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
                assertTrue(caseMap(enc, transcodeFrom, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", Config.CASE_UPCASE).equals("ABCDEFGHIJKLMNOPQRSTUVWXYZ"));
                assertTrue(caseMap(enc, transcodeFrom, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", Config.CASE_DOWNCASE).equals("abcdefghijklmnopqrstuvwxyz"));
                assertTrue(caseMap(enc, transcodeFrom, "abcdefghijklmnopqrstuvwxyz", Config.CASE_DOWNCASE).equals("abcdefghijklmnopqrstuvwxyz"));

                assertTrue(caseMap(enc, transcodeFrom, "abc", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("ABC"));
                assertTrue(caseMap(enc, transcodeFrom, "Abc", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("aBC"));
                assertTrue(caseMap(enc, transcodeFrom, "aBC", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("Abc"));

                assertTrue(caseMap(enc, transcodeFrom, "abc", Config.CASE_UPCASE | Config.CASE_TITLECASE).equals("Abc"));
            }
        }
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

        assertTrue(caseMap(enc, "äöü", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("ÄÖÜ"));
        assertTrue(caseMap(enc, "Äöü", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("äÖÜ"));
        assertTrue(caseMap(enc, "äÖÜ", Config.CASE_UPCASE | Config.CASE_DOWNCASE).equals("Äöü"));

        assertTrue(caseMap(enc, "äöü", Config.CASE_UPCASE | Config.CASE_TITLECASE).equals("Äöü"));

        assertTrue(caseMap(enc, "İ", Config.CASE_DOWNCASE).equals("i̇")); // i\u0307
        assertTrue(caseMap(enc, "İ", Config.CASE_DOWNCASE | Config.CASE_FOLD_TURKISH_AZERI).equals("i"));
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
