package org.jcodings.transcode;

import org.jcodings.Ptr;
import org.junit.Test;
import org.junit.Assert;
import java.util.Arrays;

public class TestCP51932ToCP50220 {
    @Test
    public void testCP51932ToCP50220() throws Exception {
        byte[] src = "\u0000\u007F\u008E\u00A1\u008E\u00FE\u00A1\u00A1\u00A1\u00FE".getBytes("iso-8859-1");
        byte[] dst = new byte[100];
        Ptr srcPtr = new Ptr(0);
        Ptr dstPtr = new Ptr(0);
        EConv econv = TranscoderDB.open("CP51932", "CP50220", 0);
        econv.convert(src, srcPtr, src.length, dst, dstPtr, dst.length, 0);

        byte[] str = Arrays.copyOf(dst, dstPtr.p);

        byte[] expected = "\u0000\u007F\u001B\u0024\u0042\u0021\u0023\u0050\u0000\u0021\u0021\u0021\u007E\u001B\u0028\u0042".getBytes("iso-8859-1");
        byte[] actual = Arrays.copyOf(dst, dstPtr.p);
        Assert.assertEquals(new String(expected), new String(actual));
    }
}
