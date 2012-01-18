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

public class CR_Hangul {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        14,
        0x1100, 0x11ff,
        0x302e, 0x302f,
        0x3131, 0x318e,
        0x3200, 0x321e,
        0x3260, 0x327e,
        0xa960, 0xa97c,
        0xac00, 0xd7a3,
        0xd7b0, 0xd7c6,
        0xd7cb, 0xd7fb,
        0xffa0, 0xffbe,
        0xffc2, 0xffc7,
        0xffca, 0xffcf,
        0xffd2, 0xffd7,
        0xffda, 0xffdc,
    } : null; 

}