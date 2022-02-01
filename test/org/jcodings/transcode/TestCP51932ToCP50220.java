package org.jcodings.transcode;

import org.jcodings.Ptr;
import org.junit.Test;

public class TestCP51932ToCP50220 {
    @Test
    public void test2() {
        byte[] src = {0, 127, -114, -95, -114, -2, -95, -95, -95, -2};
        byte[] dst = new byte[100];
        Ptr srcPtr = new Ptr(0);
        Ptr dstPtr = new Ptr(0);
        EConv econv = TranscoderDB.open("CP51932", "CP50220", 0);
        econv.convert(src, srcPtr, src.length, dst, dstPtr, dst.length, 0);
    }
}
