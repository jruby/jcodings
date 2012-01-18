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
package org.jcodings.unicode;

import org.jcodings.Config;

public class CR_Latin {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        30,
        0x0041, 0x005a,
        0x0061, 0x007a,
        0x00aa, 0x00aa,
        0x00ba, 0x00ba,
        0x00c0, 0x00d6,
        0x00d8, 0x00f6,
        0x00f8, 0x02b8,
        0x02e0, 0x02e4,
        0x1d00, 0x1d25,
        0x1d2c, 0x1d5c,
        0x1d62, 0x1d65,
        0x1d6b, 0x1d77,
        0x1d79, 0x1dbe,
        0x1e00, 0x1eff,
        0x2071, 0x2071,
        0x207f, 0x207f,
        0x2090, 0x209c,
        0x212a, 0x212b,
        0x2132, 0x2132,
        0x214e, 0x214e,
        0x2160, 0x2188,
        0x2c60, 0x2c7f,
        0xa722, 0xa787,
        0xa78b, 0xa78e,
        0xa790, 0xa791,
        0xa7a0, 0xa7a9,
        0xa7fa, 0xa7ff,
        0xfb00, 0xfb06,
        0xff21, 0xff3a,
        0xff41, 0xff5a,
    } : null; 

}