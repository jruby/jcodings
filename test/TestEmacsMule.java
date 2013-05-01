import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import java.nio.charset.Charset;

import org.jcodings.specific.BIG5Encoding;
import org.jcodings.specific.EmacsMuleEncoding;
import org.junit.Test;

public class TestEmacsMule {
    @Test
    public void testRightAdjustCharHeadAscii() {
        byte[] str = new byte[]{(byte)'a', (byte)'b', (byte)'c', (byte)',', (byte)'d', (byte)'e', (byte)'f'};
        
        int t = EmacsMuleEncoding.INSTANCE.rightAdjustCharHead(str, 0, 3, 7);
        assertEquals("rightAdjustCharHead did not adjust properly", 3, t);
    }
}
