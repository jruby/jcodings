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
package org.jcodings.transcode.specific;

import org.jcodings.transcode.AsciiCompatibility;
import org.jcodings.transcode.Transcoder;

public class To_UTF_16_Transcoder extends Transcoder {
    protected To_UTF_16_Transcoder () {
        super("UTF-8", "UTF-16", 416, "Utf1632", 1, 4, 4, AsciiCompatibility.ENCODER, 1);
    }

    public static final Transcoder INSTANCE = new To_UTF_16_Transcoder();

    @Override
    public int startToOutput(byte[] statep, byte[] sBytes, int sStart, int l, byte[] o, int oStart, int oSize) {
        int sp = 0;
        if (statep[sp] == 0) {
            o[oStart++] = (byte)0xFE;
            o[oStart++] = (byte)0xFF;
            statep[sp] = (byte)1;
            return 2 + funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, oSize);
        }
        return funSoToUTF16BE(statep, sBytes, sStart, l, o, oStart, oSize);
    }

    private int funSoToUTF16BE(byte[] statep, byte[] sBytes, int sStart, int l, byte[] o, int oStart, int oSize) {
        if ((sBytes[sStart] & 0x80) == 0) {
            o[oStart] = (byte)0x00;
            o[oStart+1] = sBytes[sStart];
            return 2;
        } else if ((sBytes[sStart] & 0xE0) == 0xC0) {
            o[oStart] = (byte)(((sBytes[sStart] & 0xFF) >> 2) & 0x07);
            o[oStart+1] = (byte)(((sBytes[sStart] & 0x03) << 6) | (sBytes[sStart+1] & 0x3F));
            return 2;
        } else if ((sBytes[sStart] & 0xF0) == 0xE0) {
            o[oStart] = (byte)(((sBytes[sStart] & 0xFF) << 4) | (((sBytes[sStart+1] & 0xFF) >> 2) ^ 0x20));
            o[oStart+1] = (byte)(((sBytes[sStart+1] & 0xFF) << 6) | ((sBytes[sStart+2] & 0xFF) ^ 0x80));
            return 2;
        } else {
            int w = (((sBytes[sStart] & 0x07) << 2) | (((sBytes[sStart+1] & 0xFF) >> 4) & 0x03)) - 1;
            o[0] = (byte)(0xD8 | (w>>2));
            o[1] = (byte)((w<<6) | ((sBytes[sStart+1] & 0x0F) <<2 ) | (((sBytes[sStart+2] & 0xFF) >> 4) - 8));
            o[2] = (byte)(0xDC | (((sBytes[sStart+2] & 0xFF) >> 2) & 0x03));
            o[3] = (byte)(((sBytes[sStart+2] & 0xFF) << 6) | ((sBytes[sStart+3] & 0xFF) & ~0x80));
            return 4;
        }
    }
}
