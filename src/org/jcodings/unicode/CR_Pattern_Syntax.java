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

public class CR_Pattern_Syntax {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        28,
        0x0021, 0x002f,
        0x003a, 0x0040,
        0x005b, 0x005e,
        0x0060, 0x0060,
        0x007b, 0x007e,
        0x00a1, 0x00a7,
        0x00a9, 0x00a9,
        0x00ab, 0x00ac,
        0x00ae, 0x00ae,
        0x00b0, 0x00b1,
        0x00b6, 0x00b6,
        0x00bb, 0x00bb,
        0x00bf, 0x00bf,
        0x00d7, 0x00d7,
        0x00f7, 0x00f7,
        0x2010, 0x2027,
        0x2030, 0x203e,
        0x2041, 0x2053,
        0x2055, 0x205e,
        0x2190, 0x245f,
        0x2500, 0x2775,
        0x2794, 0x2bff,
        0x2e00, 0x2e7f,
        0x3001, 0x3003,
        0x3008, 0x3020,
        0x3030, 0x3030,
        0xfd3e, 0xfd3f,
        0xfe45, 0xfe46,
    } : null; 

}