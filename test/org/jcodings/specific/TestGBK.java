package org.jcodings.specific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;

import org.jcodings.Encoding;
import org.jcodings.EncodingDB;
import org.jcodings.EncodingDB.Entry;
import org.jcodings.specific.GBKEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;
import org.junit.Before;
import org.junit.Test;

public class TestGBK {

    private CaseInsensitiveBytesHash<Entry> encodings;

    @Before
    public void setUp() throws Exception {
        encodings = EncodingDB.getEncodings();
    }

    @Test
    public void testGBK() {
        String charset_name = Charset.defaultCharset().displayName();

        assumeTrue(charset_name.equals("GBK"));

        Encoding from_jcodings = encodings.get(charset_name.getBytes()).getEncoding();
        assertEquals("the encoding got from jcodings should also be GBK, same as input",
                charset_name,
                from_jcodings.toString());
    }

    @Test
    public void testGBKEncoding() {
        assertEquals("GBKEncoding.INSTANCE should be of type GBKEncoding",
                GBKEncoding.class.getCanonicalName(),
                GBKEncoding.INSTANCE.getClass().getCanonicalName());
    }

    @Test
    public void testGetCharset() {
        Charset GBK = Charset.forName("GBK");
        assumeTrue(GBK != null);
        assertEquals("GBKEncoding.charset should be 'GBK'",
                GBK,
                GBKEncoding.INSTANCE.getCharset());
    }

    @Test
    public void testGetCharsetName() {
        assertEquals("GBKEncoding.charsetName should be 'GBK'",
                "GBK",
                GBKEncoding.INSTANCE.getCharsetName());
    }
}
