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

            System.arraycopy(readAgainBuf, 0, TRANSCODING_READBUF(this), recognizedLength, readAgainLength);
            readAgainLength = 0;
            EConvResult res = transcodeRestartable0(readAgainBuf, readAgainPos, out, outStart, readAgainStop, outStop, opt | EConv.PARTIAL_INPUT);
            if (!res.isSourceBufferEmpty()) {
                System.arraycopy(readAgainBuf, readAgainPos.p, readBuf, recognizedLength + readAgainLength, readAgainStop - readAgainPos.p);
                readAgainLength += readAgainStop - readAgainPos.p;
            }
        }
        return transcodeRestartable0(in, inStart, out, outStart, inStop, outStop, opt);
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

    private EConvResult transcodeRestartable0(final byte[] in_bytes, Ptr in_pos, final byte[] out_bytes, Ptr out_pos, int in_stop, int out_stop, int opt) {
        Transcoder tr = transcoder;
        int unitlen = tr.inputUnitLength;
        int readagain_len = 0;

        int inchar_start = in_pos.p;
        int in_p = inchar_start;

        int out_p = out_pos.p;

        Body IP = resumePosition;

        try {
            while (true) {
                switch (IP) {
                    case START:
                        inchar_start = in_p;
                        recognizedLength = 0;
                        nextTable = tr.treeStart;

                        SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_AFTER_OUTPUT);
                    case RESUME_AFTER_OUTPUT:
                        if (in_stop <= in_p) {
                            if ((opt & EConvFlags.PARTIAL_INPUT) == 0) {
                                IP = CLEANUP;
                                continue;
                            }
                            SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, START);
                            IP = START;
                            continue;
                        }
                    case NEXTBYTE:
                        nextByte = in_bytes[in_p++];
                    case FOLLOW_BYTE: // follow_byte:
                        if (nextByte < BL_MIN_BYTE(this) || BL_MAX_BYTE(this) < nextByte) {
                            nextInfo = INVALID;
                        } else {
                            nextInfo = BL_ACTION(this, nextByte);
                        }
                    case FOLLOW_INFO: // follow_info:
                        switch (nextInfo & 0x1F) {
                            case NOMAP:
                                int p = inchar_start;
                                writeBuffOff = 0;
                                while (p < in_p) {
                                    TRANSCODING_WRITEBUF(this)[writeBuffOff] = in_bytes[p++];
                                }
                                writeBuffLen = writeBuffOff;
                                writeBuffOff = 0;
                                while (writeBuffOff < writeBuffLen) {
                                    SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_NOMAP);
                                    out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
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
                                SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, SELECT_TABLE);
                                IP = SELECT_TABLE;
                                continue;
                            case ZERObt: // drop input
                                IP = START;
                                continue;
                            case ONEbt:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, ONE_BYTE_1);
                                IP = ONE_BYTE_1;
                                continue;
                            case TWObt:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, TWO_BYTE_1);
                                IP = TWO_BYTE_1;
                                continue;
                            case THREEbt:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_1);
                                IP = FOUR_BYTE_1;
                            case FOURbt:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_0);
                                IP = FOUR_BYTE_0;
                                continue;
                            case GB4bt:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_0);
                                IP = GB_FOUR_BYTE_0;
                                continue;
                            case STR1:
                                outputIndex = 0;
                                IP = STRING;
                                continue;
                            case FUNii:
                                nextInfo = tr.func_ii.call(this, nextInfo);
                                IP = FOLLOW_INFO;
                                continue;
                            case FUNsi:
                            {
                                int char_start;
                                int[] char_len = {0};
                                char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len);
                                nextInfo = tr.func_si.call(this, char_start, char_len[0]);
                                IP = FOLLOW_INFO;
                                continue;
                            }
                            case FUNio:
                                IP = CALL_FUN_IO;
                                continue;
                            case FUNso:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, CALL_FUN_SO);
                                IP = CALL_FUN_SO;
                                continue;
                            case FUNsio:
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, CALL_FUN_SIO);
                                IP = CALL_FUN_SIO;
                                continue;
                            case INVALID:
                                if (recognizedLength + (in_p - inchar_start) <= unitlen) {
                                    if (recognizedLength + (in_p - inchar_start) < unitlen) {
                                        SUSPEND_AFTER_OUTPUT(this, opt, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, READ_MORE);
                                    }
                                    IP = READ_MORE;
                                    continue;
                                } else {
                                    int invalid_len;
                                    int discard_len;
                                    invalid_len = recognizedLength + (in_p - inchar_start);
                                    discard_len = ((invalid_len - 1) / unitlen) * unitlen;
                                    readagain_len = invalid_len - discard_len;
                                    IP = REPORT_INVALID;
                                    continue;
                                }
                            case UNDEF:
                                IP = REPORT_UNDEF;
                                continue;
                            default:
                                throw new RuntimeException("unknown transcoding instruction");
                        }
                    case READ_MORE:
                        while ((opt & EConvFlags.PARTIAL_INPUT) != 0 && recognizedLength + (in_stop - inchar_start) < unitlen) {
                            in_p = in_stop;
                            SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, READ_MORE);
                        }
                        IP = RESUME_READ_MORE;
                        continue;
                    case RESUME_READ_MORE:
                        if (recognizedLength + (in_stop - inchar_start) <= unitlen) {
                            in_p = in_stop;
                        } else {
                            in_p = inchar_start + (unitlen - recognizedLength);
                        }
                        IP = REPORT_INVALID;
                        continue;
                    case CALL_FUN_SIO:
                    {
                        int char_start;
                        int[] char_len = {0};
                        if (tr.maxOutput <= out_stop - out_p) {
                            char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len);
                            out_p += tr.func_sio.call(TRANSCODING_STATE(this), char_start, char_len[0], out_bytes, out_p, out_stop - out_p);
                            IP = START;
                            continue;
                        } else {
                            char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len);
                            writeBuffLen = tr.func_sio.call(TRANSCODING_STATE(this), char_start, char_len[0], TRANSCODING_WRITEBUF(this), 0, TRANSCODING_WRITEBUF_SIZE(this));
                            writeBuffOff = 0;
                            IP = TRANSFER_WRITEBUF;
                            continue;
                        }
                    }
                    case CALL_FUN_SO:
                    {
                        int char_start;
                        int[] char_len = {0};
                        if (tr.maxOutput <= out_stop - out_p) {
                            char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len);
                            out_p += tr.func_so.call(TRANSCODING_STATE(this), char_start, char_len[0], out_bytes, out_p, out_stop - out_p);
                            IP = START;
                            continue;
                        } else {
                            char_start = transcode_char_start(in_bytes, in_pos.p, inchar_start, in_p, char_len);
                            writeBuffLen = tr.func_so.call(TRANSCODING_STATE(this), char_start, char_len[0], TRANSCODING_WRITEBUF(this), 0, TRANSCODING_WRITEBUF_SIZE(this));
                            writeBuffOff = 0;
                            IP = TRANSFER_WRITEBUF;
                            continue;
                        }
                    }
                    case CALL_FUN_IO:
                        if (tr.maxOutput <= out_stop - out_p) {
                            out_p += tr.func_io.call(this, nextInfo, out_bytes, out_p, out_stop - out_p);
                            IP = START;
                            continue;
                        } else {
                            writeBuffLen = tr.func_io.call(this, nextInfo, TRANSCODING_WRITEBUF(this), 0, TRANSCODING_WRITEBUF_SIZE(this));
                            writeBuffOff = 0;
                            IP = TRANSFER_WRITEBUF;
                            continue;
                        }
                    case TRANSFER_WRITEBUF:
                        while (writeBuffOff < writeBuffLen) {
                            SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_TRANSFER_WRITEBUF);
                            out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                        }
                        IP = START;
                        continue;
                    case RESUME_TRANSFER_WRITEBUF:
                        out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                        IP = TRANSFER_WRITEBUF;
                        continue;
                    case ONE_BYTE_1: // byte 1
                        out_bytes[out_p++] = getBT1(nextInfo);
                        IP = START;
                        continue;
                    case TWO_BYTE_1: // bytes 1, 2
                        out_bytes[out_p++] = getBT1(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, TWO_BYTE_2);
                    case TWO_BYTE_2: // byte 2
                        out_bytes[out_p++] = getBT2(nextInfo);
                        IP = START; // continue
                        continue;
                    case FOUR_BYTE_0: // bytes 0, 1, 2, 3
                        out_bytes[out_p++] = getBT0(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_1);
                    case FOUR_BYTE_1: // bytes 1, 2, 3
                        out_bytes[out_p++] = getBT1(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_2);
                    case FOUR_BYTE_2: // bytes 2, 3
                        out_bytes[out_p++] = getBT2(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, FOUR_BYTE_3);
                    case FOUR_BYTE_3: // byte 3
                        out_bytes[out_p++] = getBT3(nextInfo);
                        IP = START;
                        continue;
                    case GB_FOUR_BYTE_0: // GB4 bytes 0, 1, 2, 3
                        out_bytes[out_p++] = getGB4bt0(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_1);
                    case GB_FOUR_BYTE_1: // GB4 bytes 1, 2, 3
                        out_bytes[out_p++] = getGB4bt1(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_2);
                    case GB_FOUR_BYTE_2: // GB4 bytes 2, 3
                        out_bytes[out_p++] = getGB4bt2(nextInfo);
                        SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, GB_FOUR_BYTE_3);
                    case GB_FOUR_BYTE_3: // GB4 bytes 3
                        out_bytes[out_p++] = getGB4bt3(nextInfo);
                        IP = START;
                        continue;
                    case STRING:
                        while (outputIndex < STR1_LENGTH(BYTE_ADDR(STR1_BYTEINDEX(nextInfo)))) {
                            SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_STRING);
                            out_bytes[out_p++] = transcoder.byteArray[BYTE_ADDR(STR1_BYTEINDEX(nextInfo))];
                            outputIndex++;
                        }
                        IP = START;
                        continue;
                    case RESUME_STRING:
                        out_bytes[out_p++] = transcoder.byteArray[BYTE_ADDR(STR1_BYTEINDEX(nextInfo))];
                        outputIndex++;
                        IP = STRING;
                        continue;
                    case RESUME_NOMAP:
                        out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                        while (writeBuffOff < writeBuffLen) {
                            SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_NOMAP);
                            out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                        }
                        IP = START;
                        continue;
                    case SELECT_TABLE:
                        while (in_p >= in_stop) {
                            if ((opt & EConvFlags.PARTIAL_INPUT) != 0) {
                                IP = REPORT_INCOMPLETE; // incomplete
                                continue;
                            }
                            SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.SourceBufferEmpty, SELECT_TABLE);
                        }
                        nextByte = in_bytes[in_p++];
                        nextTable = nextInfo;
                        IP = FOLLOW_BYTE;
                        continue;
                    case REPORT_INVALID: // invalid:
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.InvalidByteSequence, START);
                        IP = START;
                        continue;
                    case REPORT_INCOMPLETE: // incomplete:
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.IncompleteInput, START);
                        IP = START;
                        continue;
                    case REPORT_UNDEF: // undef:
                        SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.UndefinedConversion, START);
                        IP = START;
                        continue;
                    case CLEANUP:
                        if (tr.finish_func != null) {
                            IP = SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, Body.FINISH_FUNC);
                            continue;
                        }
                        IP = FINISHED;
                        continue;
                    case FINISH_FUNC:
                        if (tr.maxOutput <= out_stop - out_p) {
                            out_p += tr.finish_func.call(TRANSCODING_STATE(this), out_bytes, out_p, out_stop - out_p);
                        } else {
                            writeBuffLen = tr.finish_func.call(TRANSCODING_STATE(this), TRANSCODING_WRITEBUF(this), 0, TRANSCODING_WRITEBUF_SIZE(this));
                            writeBuffOff = 0;
                            while (writeBuffOff <= writeBuffLen) {
                                SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_CLEANUP);
                                out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                            }
                        }
                        IP = FINISHED;
                        continue;
                    case RESUME_CLEANUP:
                        do {
                            out_bytes[out_p++] = TRANSCODING_WRITEBUF(this)[writeBuffOff++];
                            SUSPEND_OBUF(this, out_stop, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, RESUME_CLEANUP);
                        } while (writeBuffOff <= writeBuffLen);
                    case FINISHED:
                        while (true) {
                            SUSPEND(this, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.Finished, RESUME_CLEANUP);
                        }
                }
            }
        } catch (TranscodingSuspend ts) {
            return ts.result;
        }
    }

    private int transcode_char_start(byte[] in_bytes, int in_start, int inchar_start, int in_p, int[] char_len_ptr) {
        int ptr;
        if (inchar_start - in_start < recognizedLength) {
            System.arraycopy(TRANSCODING_READBUF(this), recognizedLength, in_bytes, inchar_start, in_p - inchar_start);
            ptr = 0;
        }
        else {
            ptr = inchar_start - recognizedLength;
        }
        char_len_ptr[0] = recognizedLength + (in_p - inchar_start);
        return ptr;
    }


    private static void SUSPEND(Transcoding tc, byte[] in_bytes, int in_p, int inchar_start, Ptr in_pos, Ptr out_pos, int out_p, int readagain_len, EConvResult ret, Body num) {
        tc.resumePosition = num;
        if (in_p - inchar_start > 0) System.arraycopy(in_bytes, inchar_start, tc.readBuf, tc.recognizedLength, in_p - inchar_start);
        in_pos.p = in_p;
        out_pos.p = out_p;
        tc.recognizedLength += in_p - inchar_start;
        if (readagain_len != 0) {
            tc.recognizedLength -= readagain_len;
            tc.readAgainLength = readagain_len;
        }
        throw new TranscodingSuspend(ret);
    }

    private static Body SUSPEND_OBUF(Transcoding tc, int out_stop, byte[] in_bytes, int in_p, int inchar_start, Ptr in_pos, Ptr out_pos, int out_p, int readagain_len, Body num) {
        while (out_stop - out_p < 1) {
            SUSPEND(tc, in_bytes, in_p, inchar_start, in_pos, out_pos, out_p, readagain_len, EConvResult.DestinationBufferFull, num);
        }
        return num;
    }

    private static Body SUSPEND_AFTER_OUTPUT(Transcoding tc, int opt, byte[] in_bytes, int in_p_offset, int inchar_start_offset, Ptr in_pos, Ptr out_pos, int out_p_offset, int readagain_len, Body num) {
        if ((opt & EConvFlags.AFTER_OUTPUT) != 0 && out_pos.p != out_p_offset) {
            SUSPEND(tc, in_bytes, in_p_offset, inchar_start_offset, in_pos, out_pos, out_p_offset, readagain_len, EConvResult.AfterOutput, num);
        }
        return num;
    }

    enum Body {
        START,
        RESUME_AFTER_OUTPUT,
        NEXTBYTE,
        FOLLOW_BYTE,
        FOLLOW_INFO,
        READ_MORE,
        RESUME_READ_MORE,
        CALL_FUN_SIO,
        CALL_FUN_SO,
        CALL_FUN_IO,
        TRANSFER_WRITEBUF,
        RESUME_TRANSFER_WRITEBUF,
        ONE_BYTE_1,
        TWO_BYTE_1,
        TWO_BYTE_2,
        FOUR_BYTE_1,
        FOUR_BYTE_2,
        FOUR_BYTE_3,
        FOUR_BYTE_0,
        GB_FOUR_BYTE_0,
        GB_FOUR_BYTE_1,
        GB_FOUR_BYTE_2,
        GB_FOUR_BYTE_3,
        STRING,
        RESUME_STRING,
        RESUME_NOMAP,
        SELECT_TABLE,
        REPORT_INVALID,
        REPORT_INCOMPLETE,
        REPORT_UNDEF,
        FINISH_FUNC,
        RESUME_CLEANUP,
        FINISHED,
        CLEANUP;
    }

    private static byte[] TRANSCODING_WRITEBUF(Transcoding tc) {
        return tc.writeBuf;
    }

    private static int TRANSCODING_WRITEBUF_SIZE(Transcoding tc) {
        return tc.writeBuffLen;
    }

    private static byte[] TRANSCODING_READBUF(Transcoding tc) {
        return tc.readBuf;
    }

    private static byte[] TRANSCODING_STATE(Transcoding tc) {
        return tc.state;
    }

    private static final int WORDINDEX_SHIFT_BITS = 2;

    private static int WORDINDEX2INFO(int widx) {
        return widx << WORDINDEX_SHIFT_BITS;
    }

    private static int INFO2WORDINDEX(int info) {
        return info >>> WORDINDEX_SHIFT_BITS;
    }

    private static int BYTE_ADDR(int index) {
        return index;
    }

    private static int WORD_ADDR(int index) {
        return INFO2WORDINDEX(index);
    }

    private static int BL_BASE(Transcoding tc) {
        return Transcoding.BYTE_ADDR(BYTE_LOOKUP_BASE(tc, WORD_ADDR(tc.nextTable)));
    }

    private static int BL_INFO(Transcoding tc) {
        return Transcoding.WORD_ADDR(BYTE_LOOKUP_INFO(tc, WORD_ADDR(tc.nextTable)));
    }

    private static int BYTE_LOOKUP_BASE(Transcoding tc, int bl) {
        return tc.transcoder.intArray[bl];
    }

    private static int BYTE_LOOKUP_INFO(Transcoding tc, int bl) {
        return tc.transcoder.intArray[bl + 1];
    }

    private static byte BL_MIN_BYTE(Transcoding tc) {
        return tc.transcoder.byteArray[BL_BASE(tc)];
    }

    private static byte BL_MAX_BYTE(Transcoding tc) {
        return tc.transcoder.byteArray[BL_BASE(tc) + 1];
    }

    private static int BL_OFFSET(Transcoding tc, byte b) {
        return tc.transcoder.byteArray[BL_BASE(tc) + 2 + b - BL_MIN_BYTE(tc)];
    }

    private static int BL_ACTION(Transcoding tc, byte b) {
        return tc.transcoder.intArray[BL_INFO(tc) + BL_OFFSET(tc, b)];
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