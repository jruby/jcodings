package org.jcodings.specific;

import org.jcodings.Ptr;
import org.jcodings.transcode.EConv;
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
    public void testUTF8toUTF16() throws Exception{
        EConv econv = TranscoderDB.open("UTF-8".getBytes(), "UTF-16".getBytes(), 0);

        byte[] src = "foo".getBytes("UTF-8");
        byte[] dest = new byte["foo".getBytes("UTF-16").length];

        econv.convert(src, new Ptr(0), 3, dest, new Ptr(0), dest.length, 0);

        Assert.assertArrayEquals("foo".getBytes("UTF-16"), dest);
    }
}
