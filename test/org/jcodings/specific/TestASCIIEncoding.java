/*
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies
 * of the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.jcodings.specific;

import org.jcodings.exception.EncodingException;
import org.jcodings.specific.ASCIIEncoding;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

public class TestASCIIEncoding {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();
    
    @Test
    public void testValidCodeToMbcLength() {
        assertEquals(1, ASCIIEncoding.INSTANCE.codeToMbcLength(0xff));
    }

    @Test
    public void testValidCodeToMbc() {
        byte[] buffer = new byte[1];
        assertEquals(1, ASCIIEncoding.INSTANCE.codeToMbc(0xff, buffer, 0));
        assertArrayEquals(new byte[]{ -1 }, buffer);
    }

    @Test
    public void testInvalidCodeToMbc() {
        expectedException.expect(EncodingException.class);
        expectedException.expectMessage("out of range char");
        
        byte[] buffer = new byte[1];
        assertEquals(1, ASCIIEncoding.INSTANCE.codeToMbc(0x100, buffer, 0));
    }
}
