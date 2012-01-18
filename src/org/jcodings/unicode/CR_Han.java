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

public class CR_Han {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        16,
        0x2e80, 0x2e99,
        0x2e9b, 0x2ef3,
        0x2f00, 0x2fd5,
        0x3005, 0x3005,
        0x3007, 0x3007,
        0x3021, 0x3029,
        0x3038, 0x303b,
        0x3400, 0x4db5,
        0x4e00, 0x9fcb,
        0xf900, 0xfa2d,
        0xfa30, 0xfa6d,
        0xfa70, 0xfad9,
        0x20000, 0x2a6d6,
        0x2a700, 0x2b734,
        0x2b740, 0x2b81d,
        0x2f800, 0x2fa1d,
    } : null; 

}