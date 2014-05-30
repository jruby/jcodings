package org.jcodings.specific;

import org.jcodings.Ptr;
import org.jcodings.transcode.EConv;
import org.jcodings.transcode.EConvFlags;
import org.jcodings.transcode.EConvResult;
import org.jcodings.transcode.TranscoderDB;
import org.jcodings.transcode.Transcoding;
import org.junit.Assert;
import org.junit.Test;
import sun.nio.cs.ext.ISO2022_JP;

import java.nio.charset.Charset;
import java.util.Arrays;

/**
 * Created by headius on 2/25/14.
 */
public class TestEConv {
    @Test
    public void testUTF8toUTF16() throws Exception {
        EConv econv = TranscoderDB.open("UTF-8".getBytes(), "UTF-16".getBytes(), 0);

        byte[] src = "foo".getBytes("UTF-8");
        byte[] dest = new byte["foo".getBytes("UTF-16").length];

        econv.convert(src, new Ptr(0), 3, dest, new Ptr(0), dest.length, 0);

        Assert.assertArrayEquals("foo".getBytes("UTF-16"), dest);
    }

    @Test
    public void testUniversalNewline() throws Exception {
        EConv econv = TranscoderDB.open("".getBytes(), "".getBytes(), EConvFlags.UNIVERSAL_NEWLINE_DECORATOR);

        byte[] src = "foo\r\nbar".getBytes();
        byte[] dest = new byte[7];

        econv.convert(src, new Ptr(0), 8, dest, new Ptr(0), dest.length, 0);

        Assert.assertArrayEquals("foo\nbar".getBytes(), dest);
    }

    @Test
    public void testCrlfNewline() throws Exception {
        EConv econv = TranscoderDB.open("".getBytes(), "".getBytes(), EConvFlags.CRLF_NEWLINE_DECORATOR);

        byte[] src = "foo\nbar".getBytes();
        byte[] dest = new byte[8];

        econv.convert(src, new Ptr(0), 7, dest, new Ptr(0), dest.length, 0);

        Assert.assertArrayEquals("foo\r\nbar".getBytes(), dest);
    }

    @Test
    public void testCrNewline() throws Exception {
        EConv econv = TranscoderDB.open("".getBytes(), "".getBytes(), EConvFlags.CR_NEWLINE_DECORATOR);

        byte[] src = "foo\nbar".getBytes();
        byte[] dest = new byte[7];

        econv.convert(src, new Ptr(0), 7, dest, new Ptr(0), dest.length, 0);

        Assert.assertArrayEquals("foo\rbar".getBytes(), dest);
    }

    @Test
    public void testXMLWithCharref() throws Exception {
        EConv econv = TranscoderDB.open("utf-8".getBytes(), "euc-jp".getBytes(), EConvFlags.XML_ATTR_CONTENT_DECORATOR | EConvFlags.XML_ATTR_QUOTE_DECORATOR | EConvFlags.UNDEF_HEX_CHARREF);

        byte[] src = "<\u2665>&\"\u2661\"".getBytes(UTF8);

        byte[] dest = new byte[50];
        Ptr destP = new Ptr(0);

        econv.convert(src, new Ptr(0), src.length, dest, destP, dest.length, 0);

        Assert.assertArrayEquals("\"&lt;&#x2665;&gt;&amp;&quot;&#x2661;&quot;\"".getBytes(), Arrays.copyOf(dest, destP.p));
    }

    @Test
    public void testXMLText() throws Exception {
//        EConv econv = TranscoderDB.open("utf-8".getBytes(), "iso-2022-jp".getBytes(), EConvFlags.XML_TEXT_DECORATOR);
//
//        byte[] expected = "&amp;う&#x2665;&amp;\"'".getBytes(ISO2022_JP);
//
//        byte[] src = "&\u3046\u2665&\"'".getBytes(UTF8);
//        byte[] dest = new byte[50];
//        Ptr destP = new Ptr(0);
//
//        econv.convert(src, new Ptr(0), src.length, dest, destP, dest.length, 0);
//
////        Assert.assertArrayEquals(expected, Arrays.copyOf(dest, destP.p));
//        Assert.assertEquals(new String(expected, ISO2022_JP), new String(Arrays.copyOf(dest, destP.p), ISO2022_JP));
    }

    @Test
    public void testEmptyStrings() throws Exception {
        EConv econv = TranscoderDB.open("UTF-8".getBytes(), "UTF-16".getBytes(), 0);

        byte[] notEmpty = new byte["foo".getBytes("UTF-16").length];

        EConvResult result = econv.convert(null, null, 0, notEmpty, new Ptr(0), notEmpty.length, 0);

        Assert.assertEquals(EConvResult.Finished, result);
    }

    private static final Charset UTF8;
    private static final Charset ISO2022_JP;

    static {
        Charset utf8 = null;
        Charset iso2022_jp = null;
        try {
            utf8 = Charset.forName("UTF-8");
            iso2022_jp = Charset.forName("ISO-2022-JP");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        UTF8 = utf8;
        ISO2022_JP = iso2022_jp;
    }
}
