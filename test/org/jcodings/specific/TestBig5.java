package org.jcodings.specific;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;

import org.jcodings.specific.BIG5Encoding;
import org.junit.Test;

public class TestBig5 {
    @Test
    public void testGetCharset() {
        Charset Big5 = Charset.forName("Big5");
        assumeTrue(Big5 != null);
        assertEquals("Big5Encoding.charset should be 'Big5'",
                Big5,
                BIG5Encoding.INSTANCE.getCharset());
    }

    @Test
    public void testGetCharsetName() {
        assertEquals("Big5Encoding.charsetName should be 'Big5'",
                "Big5".toUpperCase(),
                BIG5Encoding.INSTANCE.getCharsetName().toUpperCase());
    }
}
