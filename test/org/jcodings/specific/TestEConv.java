package org.jcodings.specific;

import org.jcodings.Ptr;
import org.jcodings.transcode.EConv;
import org.jcodings.transcode.EConvFlags;
import org.jcodings.transcode.TranscoderDB;
import org.jcodings.transcode.Transcoding;
import org.junit.Assert;
import org.junit.Test;

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
}
