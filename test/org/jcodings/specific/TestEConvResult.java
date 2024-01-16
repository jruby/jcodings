package org.jcodings.specific;

import static org.junit.Assert.assertEquals;

import org.jcodings.transcode.EConvResult;
import org.junit.Test;

public class TestEConvResult {
    @Test
    public void testSymbolicName() {
        assertEquals("finished", EConvResult.Finished.symbolicName());
        assertEquals("after_output", EConvResult.AfterOutput.symbolicName());
        assertEquals("destination_buffer_full", EConvResult.DestinationBufferFull.symbolicName());
    }
}
