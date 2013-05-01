package org.jcodings.specific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;

import org.jcodings.specific.EUCJPEncoding;
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
}
