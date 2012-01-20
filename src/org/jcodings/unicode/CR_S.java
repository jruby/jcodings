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

public class CR_S {
    static final int Table[] = Config.USE_UNICODE_PROPERTIES ? new int[] {
        208,
        0x0024, 0x0024,
        0x002b, 0x002b,
        0x003c, 0x003e,
        0x005e, 0x005e,
        0x0060, 0x0060,
        0x007c, 0x007c,
        0x007e, 0x007e,
        0x00a2, 0x00a9,
        0x00ac, 0x00ac,
        0x00ae, 0x00b1,
        0x00b4, 0x00b4,
        0x00b6, 0x00b6,
        0x00b8, 0x00b8,
        0x00d7, 0x00d7,
        0x00f7, 0x00f7,
        0x02c2, 0x02c5,
        0x02d2, 0x02df,
        0x02e5, 0x02eb,
        0x02ed, 0x02ed,
        0x02ef, 0x02ff,
        0x0375, 0x0375,
        0x0384, 0x0385,
        0x03f6, 0x03f6,
        0x0482, 0x0482,
        0x0606, 0x0608,
        0x060b, 0x060b,
        0x060e, 0x060f,
        0x06de, 0x06de,
        0x06e9, 0x06e9,
        0x06fd, 0x06fe,
        0x07f6, 0x07f6,
        0x09f2, 0x09f3,
        0x09fa, 0x09fb,
        0x0af1, 0x0af1,
        0x0b70, 0x0b70,
        0x0bf3, 0x0bfa,
        0x0c7f, 0x0c7f,
        0x0d79, 0x0d79,
        0x0e3f, 0x0e3f,
        0x0f01, 0x0f03,
        0x0f13, 0x0f17,
        0x0f1a, 0x0f1f,
        0x0f34, 0x0f34,
        0x0f36, 0x0f36,
        0x0f38, 0x0f38,
        0x0fbe, 0x0fc5,
        0x0fc7, 0x0fcc,
        0x0fce, 0x0fcf,
        0x0fd5, 0x0fd8,
        0x109e, 0x109f,
        0x1360, 0x1360,
        0x1390, 0x1399,
        0x17db, 0x17db,
        0x1940, 0x1940,
        0x19de, 0x19ff,
        0x1b61, 0x1b6a,
        0x1b74, 0x1b7c,
        0x1fbd, 0x1fbd,
        0x1fbf, 0x1fc1,
        0x1fcd, 0x1fcf,
        0x1fdd, 0x1fdf,
        0x1fed, 0x1fef,
        0x1ffd, 0x1ffe,
        0x2044, 0x2044,
        0x2052, 0x2052,
        0x207a, 0x207c,
        0x208a, 0x208c,
        0x20a0, 0x20b9,
        0x2100, 0x2101,
        0x2103, 0x2106,
        0x2108, 0x2109,
        0x2114, 0x2114,
        0x2116, 0x2118,
        0x211e, 0x2123,
        0x2125, 0x2125,
        0x2127, 0x2127,
        0x2129, 0x2129,
        0x212e, 0x212e,
        0x213a, 0x213b,
        0x2140, 0x2144,
        0x214a, 0x214d,
        0x214f, 0x214f,
        0x2190, 0x2328,
        0x232b, 0x23f3,
        0x2400, 0x2426,
        0x2440, 0x244a,
        0x249c, 0x24e9,
        0x2500, 0x26ff,
        0x2701, 0x2767,
        0x2794, 0x27c4,
        0x27c7, 0x27ca,
        0x27cc, 0x27cc,
        0x27ce, 0x27e5,
        0x27f0, 0x2982,
        0x2999, 0x29d7,
        0x29dc, 0x29fb,
        0x29fe, 0x2b4c,
        0x2b50, 0x2b59,
        0x2ce5, 0x2cea,
        0x2e80, 0x2e99,
        0x2e9b, 0x2ef3,
        0x2f00, 0x2fd5,
        0x2ff0, 0x2ffb,
        0x3004, 0x3004,
        0x3012, 0x3013,
        0x3020, 0x3020,
        0x3036, 0x3037,
        0x303e, 0x303f,
        0x309b, 0x309c,
        0x3190, 0x3191,
        0x3196, 0x319f,
        0x31c0, 0x31e3,
        0x3200, 0x321e,
        0x322a, 0x3250,
        0x3260, 0x327f,
        0x328a, 0x32b0,
        0x32c0, 0x32fe,
        0x3300, 0x33ff,
        0x4dc0, 0x4dff,
        0xa490, 0xa4c6,
        0xa700, 0xa716,
        0xa720, 0xa721,
        0xa789, 0xa78a,
        0xa828, 0xa82b,
        0xa836, 0xa839,
        0xaa77, 0xaa79,
        0xfb29, 0xfb29,
        0xfbb2, 0xfbc1,
        0xfdfc, 0xfdfd,
        0xfe62, 0xfe62,
        0xfe64, 0xfe66,
        0xfe69, 0xfe69,
        0xff04, 0xff04,
        0xff0b, 0xff0b,
        0xff1c, 0xff1e,
        0xff3e, 0xff3e,
        0xff40, 0xff40,
        0xff5c, 0xff5c,
        0xff5e, 0xff5e,
        0xffe0, 0xffe6,
        0xffe8, 0xffee,
        0xfffc, 0xfffd,
        0x10102, 0x10102,
        0x10137, 0x1013f,
        0x10179, 0x10189,
        0x10190, 0x1019b,
        0x101d0, 0x101fc,
        0x1d000, 0x1d0f5,
        0x1d100, 0x1d126,
        0x1d129, 0x1d164,
        0x1d16a, 0x1d16c,
        0x1d183, 0x1d184,
        0x1d18c, 0x1d1a9,
        0x1d1ae, 0x1d1dd,
        0x1d200, 0x1d241,
        0x1d245, 0x1d245,
        0x1d300, 0x1d356,
        0x1d6c1, 0x1d6c1,
        0x1d6db, 0x1d6db,
        0x1d6fb, 0x1d6fb,
        0x1d715, 0x1d715,
        0x1d735, 0x1d735,
        0x1d74f, 0x1d74f,
        0x1d76f, 0x1d76f,
        0x1d789, 0x1d789,
        0x1d7a9, 0x1d7a9,
        0x1d7c3, 0x1d7c3,
        0x1f000, 0x1f02b,
        0x1f030, 0x1f093,
        0x1f0a0, 0x1f0ae,
        0x1f0b1, 0x1f0be,
        0x1f0c1, 0x1f0cf,
        0x1f0d1, 0x1f0df,
        0x1f110, 0x1f12e,
        0x1f130, 0x1f169,
        0x1f170, 0x1f19a,
        0x1f1e6, 0x1f202,
        0x1f210, 0x1f23a,
        0x1f240, 0x1f248,
        0x1f250, 0x1f251,
        0x1f300, 0x1f320,
        0x1f330, 0x1f335,
        0x1f337, 0x1f37c,
        0x1f380, 0x1f393,
        0x1f3a0, 0x1f3c4,
        0x1f3c6, 0x1f3ca,
        0x1f3e0, 0x1f3f0,
        0x1f400, 0x1f43e,
        0x1f440, 0x1f440,
        0x1f442, 0x1f4f7,
        0x1f4f9, 0x1f4fc,
        0x1f500, 0x1f53d,
        0x1f550, 0x1f567,
        0x1f5fb, 0x1f5ff,
        0x1f601, 0x1f610,
        0x1f612, 0x1f614,
        0x1f616, 0x1f616,
        0x1f618, 0x1f618,
        0x1f61a, 0x1f61a,
        0x1f61c, 0x1f61e,
        0x1f620, 0x1f625,
        0x1f628, 0x1f62b,
        0x1f62d, 0x1f62d,
        0x1f630, 0x1f633,
        0x1f635, 0x1f640,
        0x1f645, 0x1f64f,
        0x1f680, 0x1f6c5,
        0x1f700, 0x1f773,
    } : null; 

}