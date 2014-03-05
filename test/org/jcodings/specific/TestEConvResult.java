package org.jcodings.specific;

import junit.framework.Assert;
import org.jcodings.transcode.EConvResult;
import org.junit.Test;

public class TestEConvResult {
    @Test
    public void testSymbolicName() {
        Assert.assertEquals("finished", EConvResult.Finished.symbolicName());
        Assert.assertEquals("after_output", EConvResult.AfterOutput.symbolicName());
        Assert.assertEquals("destination_buffer_full", EConvResult.DestinationBufferFull.symbolicName());
    }
}
