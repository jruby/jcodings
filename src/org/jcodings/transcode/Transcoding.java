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
package org.jcodings.transcode;

import org.jcodings.Ptr;
import org.jcodings.exception.TranscoderException;

public class Transcoding implements TranscodingInstruction {
    public Transcoding(Transcoder transcoder) {
        this.transcoder = transcoder;
        this.readBuf = new byte[transcoder.maxInput];
        this.writeBuf = new byte[transcoder.maxOutput];
    }

    public final Transcoder transcoder;
    int flags;

    int resumePosition;
    int nextTable;
    int nextInfo;
    byte nextByte;
    int outputIndex;

    int recognizedLength, readAgainLength;

    final byte[] readBuf;

    int writeBuffOff, writeBuffLen;
    final byte[] writeBuf;
    byte[] state;

    void close() {
        transcoder.stateFinish();
    }

    private int charStart;
    private byte[] charStartBytes;

    @Override
    public String toString() {
        return "Transcoding for transcoder " + transcoder.toString();
    }

    /* transcode_char_start */
    int charStart() {
        if (recognizedLength > inCharStart - inPos.p) {
            System.arraycopy(inBytes, inCharStart, readBuf, recognizedLength, inP - inCharStart);
            charStart = 0;
            charStartBytes = readBuf;
        } else {
            charStart = inCharStart - recognizedLength;
            charStartBytes = inBytes;
        }

        return recognizedLength + (inP - inCharStart);
    }

    /* rb_transcoding_convert */
    EConvResult convert(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags) {
        return transcodeRestartable(in, inPtr, inStop, out, outPtr, outStop, flags);
    }

    private EConvResult transcodeRestartable(byte[] in, Ptr inStart, int inStop, byte[] out, Ptr outStart, int outStop, int opt) {
        if (readAgainLength != 0) {
            byte[] readAgainBuf = new byte[readAgainLength];
            Ptr readAgainPos = new Ptr(0);
            int readAgainStop = readAgainLength;
            System.arraycopy(readBuf, recognizedLength, readAgainBuf, readAgainPos.p, readAgainLength);
            readAgainLength = 0;

            EConvResult res = transcodeRestartable0(readAgainBuf, readAgainPos, readAgainStop, out, outStart, outStop, opt | EConv.PARTIAL_INPUT);
            if (!res.isSourceBufferEmpty()) {
                System.arraycopy(readAgainBuf, readAgainPos.p, readBuf, recognizedLength + readAgainLength, readAgainStop - readAgainPos.p);
                readAgainLength += readAgainStop - readAgainPos.p;
            }
        }
        return transcodeRestartable0(in, inStart, inStop, out, outStart, outStop, opt);
    }

    int inCharStart;
    byte[] inBytes, outBytes;
    int inP, outP;

    int outStop, inStop;

    Ptr inPos, outPos;
    int readAgainL;

    int opt;

    private EConvResult transcodeRestartable0(byte[] in, Ptr inStart, int inStop, byte[] out, Ptr outStart, int outStop, int opt) {
        inPos = inStart;
        inP = inCharStart = inStart.p;
        outPos = outStart;
        readAgainL = 0;
        inBytes = in;
        outBytes = out;

        return null;
    }

    private EConvResult transcodeSwitch() {
        int jump = resumePosition != 0 ? resumePosition : (nextInfo & 0xff);

        while (true) {

            switch (jump) {
            case NOMAP:
                int p = inCharStart;
                writeBuffOff = 0;
                while (p < inP) {
                    writeBuf[writeBuffOff++] = inBytes[p++];
                }

                writeBuffLen = writeBuffOff;
                writeBuffOff = 0;
                while (writeBuffOff < writeBuffLen) {
                    if (outStop - outP < 1) return suspend(NOMAP_RESUME_1);
                    outBytes[outP++] = writeBuf[writeBuffOff++];
                }
                continue;
            case NOMAP_RESUME_1:
                do {
                    if (outStop - outP < 1) return suspend(NOMAP_RESUME_1);
                    outBytes[outP++] = writeBuf[writeBuffOff++];
                } while (writeBuffOff < writeBuffLen);

            case 0x00:
            case 0x04:
            case 0x08:
            case 0x0C:
            case 0x10:
            case 0x14:
            case 0x18:
            case 0x1C:
                if (suspendAfterOutput()) return suspend(EConvResult.AfterOutput, ZeroXResume_1);
            case ZeroXResume_1:
                if (inP >= inStop) {
                    if ((opt & EConvFlags.PARTIAL_INPUT) == 0) return suspend(EConvResult.AfterOutput, ZeroXResume_2);
                    return suspend(EConvResult.SourceBufferEmpty, ZeroXResume_2);
                }

            case ZeroXResume_2:
                while (inP >= inStop) {
                    if ((opt & EConvFlags.PARTIAL_INPUT) == 0) return suspend(EConvResult.AfterOutput, ZeroXResume_2);
                    return suspend(EConvResult.SourceBufferEmpty, ZeroXResume_2);
                }
                nextByte = inBytes[inP++];
                nextTable = nextInfo;

            case ZERObt: /* drop input */
            case ONEbt:
            case TWObt:
            case THREEbt:
            case FOURbt:
            case GB4bt:
            case STR1:
            case FUNii:
            case FUNsi:
            case FUNio:
            case FUNso:
            case FUNsio:
            case INVALID:
            case UNDEF:

            default:
                throw new TranscoderException("unknown transcoding instruction: " + jump);
            }

        }
        // return null;
    }

    private EConvResult transcodeInstruction_NoMap() {

        return transcodeInstruction_NoMap_resume_3();
    }

    private EConvResult transcodeInstruction_NoMap_resume_3() {
        while (writeBuffOff < writeBuffLen) {
            outBytes[outP++] = writeBuf[writeBuffOff++];

            return suspend(3);
        }
        return null;
    }

    // SUSPEND
    private EConvResult suspend(EConvResult ret, int num) {
        resumePosition = num;
        if (inP - inCharStart > 0) System.arraycopy(inBytes, inCharStart, readBuf, recognizedLength, inP - inCharStart);
        inPos.p = inP;
        outPos.p = outP;
        recognizedLength += inP - inCharStart;
        if (readAgainL != 0) {
            recognizedLength -= readAgainL;
            readAgainLength = readAgainL;
        }
        return ret;
    }

    // SUSPEND_OBUF
    private EConvResult suspend(int num) {
        while (outStop - outP < 1) {
            return suspend(EConvResult.DestinationBufferFull, num);
        }
        return null;

    }

    private boolean suspendAfterOutput() {
        return (opt & EConvFlags.AFTER_OUTPUT) != 0 && outPos.p != outP;
    }

    public static int getGB4bt0(int a) {
        return a >>> 8;
    }

    public static int getGB4bt1(int a) {
        return ((a >>> 24) & 0xf) | 0x30;
    }

    public static int getGB4bt2(int a) {
        return a >>> 16;
    }

    public static int getGB4bt3(int a) {
        return ((a >>> 28) & 0x0f) | 0x30;
    }

}