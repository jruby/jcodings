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

public class CR_Noncharacter_Code_Point {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        18,
        0xfdd0, 0xfdef,
        0xfffe, 0xffff,
        0x1fffe, 0x1ffff,
        0x2fffe, 0x2ffff,
        0x3fffe, 0x3ffff,
        0x4fffe, 0x4ffff,
        0x5fffe, 0x5ffff,
        0x6fffe, 0x6ffff,
        0x7fffe, 0x7ffff,
        0x8fffe, 0x8ffff,
        0x9fffe, 0x9ffff,
        0xafffe, 0xaffff,
        0xbfffe, 0xbffff,
        0xcfffe, 0xcffff,
        0xdfffe, 0xdffff,
        0xefffe, 0xeffff,
        0xffffe, 0xfffff,
        0x10fffe, 0x10ffff,
    } : null; 

}