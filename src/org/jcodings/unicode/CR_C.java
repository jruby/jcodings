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

public class CR_C {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        20,
        0x0000, 0x001f,
        0x007f, 0x009f,
        0x00ad, 0x00ad,
        0x0600, 0x0603,
        0x06dd, 0x06dd,
        0x070f, 0x070f,
        0x17b4, 0x17b5,
        0x200b, 0x200f,
        0x202a, 0x202e,
        0x2060, 0x2064,
        0x206a, 0x206f,
        0xd800, 0xf8ff,
        0xfeff, 0xfeff,
        0xfff9, 0xfffb,
        0x110bd, 0x110bd,
        0x1d173, 0x1d17a,
        0xe0001, 0xe0001,
        0xe0020, 0xe007f,
        0xf0000, 0xffffd,
        0x100000, 0x10ffff,
    } : null; 

}