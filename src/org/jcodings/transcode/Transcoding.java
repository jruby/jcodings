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

import static org.jcodings.transcode.Transcoding.Body.*;

public class Transcoding implements TranscodingInstruction {
    public Transcoding(Transcoder transcoder) {
        this.transcoder = transcoder;
        this.readBuf = new byte[transcoder.maxInput];
        this.writeBuf = new byte[transcoder.maxOutput];
    }

    public final Transcoder transcoder;
    int flags;

    Body resumePosition;
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

            EConvResult res = transcodeRestartable0(new Pointer<BytePointer>(new BytePointer[]{new BytePointer(readAgainBuf, readAgainPos.p)}, 0), , readAgainStop, outStop, this, opt | EConv.PARTIAL_INPUT);
            if (!res.isSourceBufferEmpty()) {
                System.arraycopy(readAgainBuf, readAgainPos.p, readBuf, recognizedLength + readAgainLength, readAgainStop - readAgainPos.p);
                readAgainLength += readAgainStop - readAgainPos.p;
            }
        }
        return transcodeRestartable0(in, inStart, inStop, out, outStart, outStop, opt);
    }

    int inCharStart;
    byte[] inBytes;
    int inP;

    Ptr inPos;

    private static int STR1_LENGTH(int byteaddr) {
        return byteaddr + 4;
    }

    private static int STR1_BYTEINDEX(int byteaddr) {
        return byteaddr >> 6;
    }

    private EConvResult transcodeRestartable0(Pointer<BytePointer> in_pos, Pointer<BytePointer> out_pos, BytePointer in_stop, BytePointer out_stop, Transcoding tc, int opt) {
        Transcoder tr = tc.transcoder;
        int unitlen = tr.inputUnitLength;
        int readagain_len = 0;

        BytePointer inchar_start;
        BytePointer in_p = inchar_start = in_pos.deref();

        BytePointer out_p = out_pos.deref();

        Body IP = tc.resumePosition;

        try {
            while (true) {
                switch (IP) {
                    case START:
                        inchar_start = in_p;
                        tc.recognizedLength = 0;
                        tc.nextTable = tr.treeStart;

                        IP = SUSPEND_AFTER_OUTPUT(tc, opt, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B01);
                        continue;
                    case B01:
                        if (in_stop.le(in_p)) {
                            if ((opt & EConvFlags.PARTIAL_INPUT) == 0) {
                                IP = CLEANUP;
                                continue;
                            }
                            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, START);
                            IP = START;
                            continue;
                        }
                    case B04:
                        tc.nextByte = in_p.deref_inc();
                    case B05: // follow_byte:
                        if (tc.nextByte < BL_MIN_BYTE(tc) || BL_MAX_BYTE(tc) < tc.nextByte) {
                            tc.nextInfo = INVALID;
                        } else {
                            tc.nextInfo = BL_ACTION(tc, tc.nextByte);
                        }
                    case B06: // follow_info:
                        switch (tc.nextInfo & 0x1F) {
                            case NOMAP:
                                BytePointer p = inchar_start;
                                tc.writeBuffOff = 0;
                                while (p.offset < in_p.offset) {
                                    TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff] = p.deref_inc();
                                }
                                tc.writeBuffLen = tc.writeBuffOff;
                                tc.writeBuffOff = 0;
                                while (tc.writeBuffOff < tc.writeBuffLen) {
                                    SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B07);
                                    out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                                }
                                IP = START;
                                continue;
                            case 0x00:
                            case 0x04:
                            case 0x08:
                            case 0x0C:
                            case 0x10:
                            case 0x14:
                            case 0x18:
                            case 0x1C:
                                SUSPEND_AFTER_OUTPUT(tc, opt, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B09);
                                IP = B09;
                                continue;
                            case ZERObt: // drop input
                                IP = START;
                                continue;
                            case ONEbt:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B11);
                                IP = B11;
                                continue;
                            case TWObt:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B12);
                                IP = B12;
                                continue;
                            case THREEbt:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B14);
                                IP = B14;
                            case FOURbt:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B17);
                                IP = B17;
                                continue;
                            case GB4bt:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B18);
                                IP = B18;
                                continue;
                            case STR1:
                                tc.outputIndex = 0;
                                IP = B22;
                                continue;
                            case FUNii:
                                tc.nextInfo = tr.func_ii.call(tc, tc.nextInfo);
                                IP = B06;
                                continue;
                            case FUNsi:
                            {
                                int char_start;
                                int[] char_len = {0};
                                char_start = tc.transcode_char_start(in_pos, inchar_start, in_p, char_len);
                                tc.nextInfo = tr.func_si.call(tc, char_start, char_len[0]);
                                IP = B06;
                                continue;
                            }
                            case FUNio:
                                IP = B24;
                                continue;
                            case FUNso:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B27);
                                IP = B27;
                                continue;
                            case FUNsio:
                                SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B28);
                                IP = B28;
                                continue;
                            case INVALID:
                                if (tc.recognizedLength + (in_p.offset - inchar_start.offset) <= unitlen) {
                                    if (tc.recognizedLength + (in_p.offset - inchar_start.offset) < unitlen) {
                                        SUSPEND_AFTER_OUTPUT(tc, opt, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B29);
                                    }
                                    IP = B29;
                                    continue;
                                } else {
                                    int invalid_len;
                                    int discard_len;
                                    invalid_len = tc.recognizedLength + (in_p.offset - inchar_start.offset);
                                    discard_len = ((invalid_len - 1) / unitlen) * unitlen;
                                    readagain_len = invalid_len - discard_len;
                                    IP = B31;
                                    continue;
                                }
                                IP = B31;
                                continue;
                            case UNDEF:
                                IP = B32;
                                continue;
                            default:
                                throw new RuntimeException("unknown transcoding instruction");
                        }
                        IP = START;
                        continue;
                    case B29:
                        while ((opt & EConvFlags.PARTIAL_INPUT) != 0 && tc.recognizedLength + (in_stop.offset - inchar_start.offset) < unitlen) {
                            in_p.offset = in_stop.offset;
                            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, B29);
                        }
                        IP = B30;
                        continue;
                    case B30:
                        if (tc.recognizedLength + (in_stop.offset - inchar_start.offset) <= unitlen) {
                            in_p.offset = in_stop.offset;
                        } else {
                            in_p.offset = inchar_start.offset + (unitlen - tc.recognizedLength);
                        }
                        IP = B31;
                        continue;
                    case B28:
                    {
                        int char_start;
                        int[] char_len = {0};
                        if (tr.maxOutput <= out_stop.offset - out_p.offset) {
                            char_start = transcode_char_start(tc, in_pos.deref(), inchar_start, in_p, char_len);
                            out_p.offset += tr.func_sio(TRANSCODING_STATE(tc), char_start, char_len[0], out_p, out_stop.offset - out_p.offset);
                            IP = START;
                            continue;
                        } else {
                            char_start = transcode_char_start(tc, in_pos.deref(), inchar_start, in_p, char_len);
                            tc.writeBuffLen = tr.func_sio(TRANSCODING_STATE(tc), char_start, char_len[0], TRANSCODING_WRITEBUF(tc), TRANSCODING_WRITEBUF_SIZE(tc));
                            tc.writeBuffOff = 0;
                            IP = B25;
                            continue;
                        }
                    }
                    case B27:
                    {
                        int char_start;
                        int[] char_len = {0};
                        if (tr.maxOutput <= out_stop.offset - out_p.offset) {
                            char_start = transcode_char_start(tc, in_pos.deref(), inchar_start, in_p, char_len);
                            out_p.offset += tr.func_so(TRANSCODING_STATE(tc), char_start, char_len[0], out_p, out_stop.offset - out_p.offset);
                            IP = START;
                            continue;
                        } else {
                            char_start = transcode_char_start(tc, in_pos.deref(), inchar_start, in_p, char_len);
                            tc.writeBuffLen = tr.func_so(TRANSCODING_STATE(tc), char_start, char_len[0], TRANSCODING_WRITEBUF(tc), TRANSCODING_WRITEBUF_SIZE(tc));
                            tc.writeBuffOff = 0;
                            IP = B25;
                            continue;
                        }
                    }
                    case B24:
                        if (tr.maxOutput <= out_stop.offset - out_p.offset) {
                            out_p.offset += tr.func_io.call(tc, tc.nextInfo, out_p, out_stop.offset - out_p.offset);
                            IP = START;
                            continue;
                        } else {
                            tc.writeBuffLen = tr.func_io.call(tc, tc.nextInfo, TRANSCODING_WRITEBUF(tc), TRANSCODING_WRITEBUF_SIZE(tc));
                            tc.writeBuffOff = 0;
                            IP = B25;
                            continue;
                        }
                    case B25:
                        while (tc.writeBuffOff < tc.writeBuffLen) {
                            SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B26);
                            out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                        }
                        IP = START;
                        continue;
                    case B26:
                        out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                        IP = B25;
                        continue;
                    case B11: // byte 1
                        out_p.set_inc(getBT1(tc.nextInfo));
                        IP = START;
                        continue;
                    case B12: // bytes 1, 2
                        out_p.set_inc(getBT1(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B13);
                    case B13: // byte 2
                        out_p.set_inc(getBT2(tc.nextInfo));
                        IP = START; // continue
                        continue;
                    case B17: // bytes 0, 1, 2, 3
                        out_p.set_inc(getBT0(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B14);
                    case B14: // bytes 1, 2, 3
                        out_p.set_inc(getBT1(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B15);
                    case B15: // bytes 2, 3
                        out_p.set_inc(getBT2(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B16);
                    case B16: // byte 3
                        out_p.set_inc(getBT3(tc.nextInfo));
                        IP = START;
                        continue;
                    case B18: // GB4 bytes 0, 1, 2, 3
                        out_p.set_inc(getGB4bt0(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B19);
                    case B19: // GB4 bytes 1, 2, 3
                        out_p.set_inc(getGB4bt1(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B20);
                    case B20: // GB4 bytes 2, 3
                        out_p.set_inc(getGB4bt2(tc.nextInfo));
                        SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B21);
                    case B21: // GB4 bytes 3
                        out_p.set_inc(getGB4bt3(tc.nextInfo));
                        IP = START;
                        continue;
                    case B22:
                        while (tc.outputIndex < STR1_LENGTH(BYTE_ADDR(tc, STR1_BYTEINDEX(tc.nextInfo)).deref())) {
                            SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B23);
                            out_p.set_inc(BYTE_ADDR(tc, STR1_BYTEINDEX(tc.nextInfo)).deref());
                            tc.outputIndex++;
                        }
                        IP = START;
                        continue;
                    case B23:
                        out_p.set_inc(BYTE_ADDR(tc, STR1_BYTEINDEX(tc.nextInfo)).deref());
                        tc.outputIndex++;
                        IP = B22;
                        continue;
                    case B07:
                        out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                        while (tc.writeBuffOff < tc.writeBuffLen) {
                            SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, B07);
                            out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                        }
                        IP = START;
                        continue;
                    case B09:
                        while (in_p.offset >= in_stop.offset) {
                            if ((opt & EConvFlags.PARTIAL_INPUT) != 0) {
                                IP = B10; // incomplete
                                continue;
                            }
                            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, B09);
                        }
                        tc.nextByte = in_p.deref_inc();
                        tc.nextTable = tc.nextInfo;
                        IP = B05;
                        continue;
                    case B31: // invalid:
                        SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.InvalidByteSequence, START);
                        IP = START;
                        continue;
                    case B10: // incomplete:
                        SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.IncompleteInput, START);
                        IP = START;
                        continue;
                    case B32: // undef:
                        SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.UndefinedConversion, START);
                        IP = START;
                        continue;
                    case CLEANUP:
                        if (tr.finish_func != null) {
                            IP = SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, Body.FINISH_FUNC);
                            continue;
                        }
                        IP = FINISHED;
                        continue;
                    case FINISH_FUNC:
                        if (tr.maxOutput <= out_stop.offset - out_p.offset) {
                            out_p.offset += tr.finish_func(TRANSCODING_STATE(tc), out_p, out_stop.offset - out_p.offset);
                        } else {
                            tc.writeBuffLen = tr.finish_func(TRANSCODING_STATE(tc), TRANSCODING_WRITEBUF(tc), TRANSCODING_WRITEBUF_SIZE(tc));
                            tc.writeBuffOff = 0;
                            while (tc.writeBuffOff <= tc.writeBuffLen) {
                                IP = SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, CLEANUP_RESUME);
                                out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                            }
                        }
                        IP = FINISHED;
                        continue;
                    case CLEANUP_RESUME:
                        do {
                            out_p.set_inc(TRANSCODING_WRITEBUF(tc)[tc.writeBuffOff++]);
                            IP = SUSPEND_OBUF(tc, out_stop, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, CLEANUP_RESUME);
                        } while (tc.writeBuffOff <= tc.writeBuffLen);
                    case FINISHED:
                        while (true) {
                            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.Finished, CLEANUP_RESUME);
                        }
                }
            }
        } catch (TranscodingSuspend ts) {
            return ts.result;
        }
    }

    private static void SUSPEND(Transcoding tc, BytePointer in_p, BytePointer inchar_start, Pointer<BytePointer> in_pos, Pointer<BytePointer> out_pos, BytePointer out_p, int readagain_len, EConvResult ret, Body num) {
        tc.resumePosition = num;
        if (in_p.offset - inchar_start.offset > 0) System.arraycopy(inchar_start.bytes, inchar_start.offset, tc.readBuf, tc.recognizedLength, in_p.offset - inchar_start.offset);
        in_pos.set(in_p);
        out_pos.set(out_p);
        tc.recognizedLength += in_p.offset - inchar_start.offset;
        if (readagain_len != 0) {
            tc.recognizedLength -= readagain_len;
            tc.readAgainLength = readagain_len;
        }
        throw new TranscodingSuspend(ret);
    }

    private static Body SUSPEND_OBUF(Transcoding tc, BytePointer out_stop, BytePointer in_p, BytePointer inchar_start, Pointer<BytePointer> in_pos, Pointer<BytePointer> out_pos, BytePointer out_p, int readagain_len, Body num) {
        while (out_stop.offset - out_p.offset < 1) {
            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.DestinationBufferFull, num);
        }
        return num;
    }

    private static Body SUSPEND_AFTER_OUTPUT(Transcoding tc, int opt, BytePointer in_p, BytePointer inchar_start, Pointer<BytePointer> in_pos, Pointer<BytePointer> out_pos, BytePointer out_p, int readagain_len, Body num) {
        if ((opt & EConvFlags.AFTER_OUTPUT) != 0 && out_pos.deref() != out_p) {
            SUSPEND(tc, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.AfterOutput, num);
        }
        return num;
    }

    enum Body {
        START,
        B01,
        B04,
        B05,
        B06,
        B29,
        B30,
        B28,
        B27,
        B24,
        B25,
        B26,
        B11,
        B12,
        B13,
        B14,
        B15,
        B16,
        B17,
        B18,
        B19,
        B20,
        B21,
        B22,
        B23,
        B07,
        B09,
        B31,
        B10,
        B32,
        FINISH_FUNC,
        CLEANUP_RESUME,
        FINISHED,
        CLEANUP;
    }

    private static byte[] TRANSCODING_WRITEBUF(Transcoding tc) {
        return tc.writeBuf;
    }

    private static int TRANSCODING_WRITEBUF_SIZE(Transcoding tc) {
        return tc.writeBuffLen;
    }

    private static byte[] TRANSCODING_STATE(Transcoding tc) {
        return tc.state;
    }

    private static class BytePointer {
        BytePointer(byte[] bytes, int offset) {
            this.bytes = bytes;
            this.offset = offset;
        }

        byte deref() {
            return deref(0);
        }

        byte deref_inc() {
            byte result = deref(0);
            offset++;
            return result;
        }

        byte deref(int index) {
            return this.bytes[offset + index];
        }

        boolean lt(BytePointer other) {
            return this.offset < other.offset;
        }

        boolean le(BytePointer other) {
            return this.offset <= other.offset;
        }

        void set_inc(byte value) {
            this.bytes[this.offset++] = value;
        }

        final byte[] bytes;
        int offset;
    }

    private static class IntPointer {
        IntPointer(int[] ints, int offset) {
            this.ints = ints;
            this.offset = offset;
        }

        int deref(int index) {
            return this.ints[offset + index];
        }

        final int[] ints;
        int offset;
    }

    private static class Pointer<T> {
        Pointer(T[] objects, int offset) {
            this.objects = objects;
            this.offset = offset;
        }

        T deref() {
            return deref(0);
        }

        T deref(int index) {
            return this.objects[offset + index];
        }

        void set(T t) {
            objects[offset] = t;
        }

        final T[] objects;
        int offset;
    }

    private static final int WORDINDEX_SHIFT_BITS = 2;

    private static int WORDINDEX2INFO(int widx) {
        return widx << WORDINDEX_SHIFT_BITS;
    }

    private static int INFO2WORDINDEX(int info) {
        return info >>> WORDINDEX_SHIFT_BITS;
    }

    private static BytePointer BYTE_ADDR(Transcoding tc, int index) {
        return new BytePointer(tc.transcoder.byteArray, index);
    }

    private static IntPointer WORD_ADDR(Transcoding tc, int index) {
        return new IntPointer(tc.transcoder.intArray, INFO2WORDINDEX(index));
    }

    private static BytePointer BL_BASE(Transcoding tc) {
        return Transcoding.BYTE_ADDR(tc, BYTE_LOOKUP_BASE(Transcoding.WORD_ADDR(tc, tc.nextTable)));
    }

    private static IntPointer BL_INFO(Transcoding tc) {
        return Transcoding.WORD_ADDR(tc, BYTE_LOOKUP_INFO(Transcoding.WORD_ADDR(tc, tc.nextTable)));
    }

    private static int BYTE_LOOKUP_BASE(IntPointer bl) {
        return bl.deref(0);
    }

    private static int BYTE_LOOKUP_INFO(IntPointer bl) {
        return bl.deref(1);
    }

    private static int BL_MIN_BYTE(Transcoding tc) {
        return Transcoding.BL_BASE(tc).deref(0);
    }

    private static int BL_MAX_BYTE(Transcoding tc) {
        return Transcoding.BL_BASE(tc).deref(1);
    }

    private static int BL_OFFSET(Transcoding tc, byte b) {
        return Transcoding.BL_BASE(tc).deref(2 + b - Transcoding.BL_MIN_BYTE(tc));
    }

    private static int BL_ACTION(Transcoding tc, byte b) {
        return Transcoding.BL_INFO(tc).deref(Transcoding.BL_OFFSET(tc, b));
    }

    private static class TranscodingSuspend extends RuntimeException {
        EConvResult result;

        TranscodingSuspend(EConvResult result) {
            this.result = result;
        }

        @Override
        public Throwable fillInStackTrace() {
            return this;
        }
    }

    public static byte getGB4bt0(int a) {
        return (byte)(a >>> 8);
    }

    public static byte getGB4bt1(int a) {
        return (byte)(((a >>> 24) & 0xf) | 0x30);
    }

    public static byte getGB4bt2(int a) {
        return (byte)(a >>> 160);
    }

    public static byte getGB4bt3(int a) {
        return (byte)(((a >>> 28) & 0x0f) | 0x30);
    }

    public static byte getBT1(int a) {
        return (byte)(a >>> 8);
    }

    public static byte getBT2(int a) {
        return (byte)(a >>> 16);
    }

    public static byte getBT3(int a) {
        return (byte)(a >>> 24);
    }

    public static byte getBT0(int a) {
        return (byte)(((a >>> 5) & 0x0F) | 0x30);
    }

}