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
import org.jcodings.transcode.TranscodeFunctions;
import org.jcodings.transcode.Transcoder;

public class Iso2022jp_kddi_decoder_Transcoder extends Transcoder {
    protected Iso2022jp_kddi_decoder_Transcoder () {
        super("ISO-2022-JP-KDDI", "stateless-ISO-2022-JP-KDDI", 56, "EmojiIso2022Kddi", 1, 3, 3, AsciiCompatibility.DECODER, 1);
    }

    public static final Transcoder INSTANCE = new Iso2022jp_kddi_decoder_Transcoder();

    @Override
    public int stateInit(byte[] statep) {
        return TranscodeFunctions.iso2022jpKddiInit(statep);
    }

    @Override
    public int stateFinish(byte[] state) {
        return TranscodeFunctions.iso2022jpKddiInit(state);
    }

    @Override
    public int startToInfo(byte[] statep, byte[] s, int sStart, int l) {
        return TranscodeFunctions.funSiIso2022jpKddiDecoder(statep, s, sStart, l);
    }

    @Override
    public int startToOutput(byte[] statep, byte[] s, int sStart, int l, byte[] o, int oStart, int oSize) {
        return TranscodeFunctions.funSoIso2022jpKddiDecoder(statep, s, sStart, l, o, oStart, oSize);
    }
}
