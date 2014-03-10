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

import static org.jcodings.util.CaseInsensitiveBytesHash.caseInsensitiveEquals;

import org.jcodings.Encoding;
import org.jcodings.Ptr;
import org.jcodings.exception.InternalException;

public final class EConv implements EConvFlags {
    int flags;
    public byte[] source, destination; // source/destination encoding names

    boolean started = false;

    public byte[] replacementString;
    public int replacementLength;
    public byte[] replacementEncoding;

    Buffer inBuf = new Buffer();

    public EConvElement[] elements;
    public int numTranscoders;
    int numFinished;

    public Transcoding lastTranscoding;
    public final LastError lastError = new LastError();

    public Encoding sourceEncoding, destinationEncoding;

    @Override
    public String toString() {
        return new String(source) + " => " + new String(destination);
    }

    EConv(int nHint) {
        if (nHint < 0) nHint = 1;
        elements = new EConvElement[nHint];
        lastError.result = EConvResult.SourceBufferEmpty;
    }

    public static final class EConvElement extends Buffer {
        EConvElement(Transcoding transcoding) {
            this.transcoding = transcoding;
            lastResult = EConvResult.SourceBufferEmpty;
        }

        public final Transcoding transcoding;
        EConvResult lastResult;

        @Override
        public String toString() {
            String s = "EConv " + transcoding.toString() + "\n";
            s += "  last result: " + lastResult;
            return s;
        }
    }

    public static final class LastError {
        public EConvResult result;
        public Transcoding errorTranscoding;
        public byte[] source, destination;

        public byte[] errorBytes;
        public int errorBytesP, errorBytesEnd;
        public int readAgainLength;

        void reset() {
            result = null;
            errorTranscoding = null;
            source = destination = null;
            errorBytes = null;
            errorBytesP = errorBytesEnd = 0;
            readAgainLength = 0;
        }

        @Override
        public String toString() {
            String s = "Last Error " + (source == null ? "null" : new String(source)) + " => " + (destination == null ? "null" : new String(destination))
                    + "\n";
            s += "  result: " + result.toString() + "\n";
            s += "  error bytes: " + (errorBytes == null ? "null" : new String(errorBytes, errorBytesP, errorBytesP + errorBytesEnd)) + "\n";
            s += "  read again length: " + readAgainLength;
            return s;
        }
    }

    static final byte[] NULL_STRING = new byte[0];
    static final int[] NULL_POINTER = new int[0];

    static boolean decorator(byte[] source, byte[] destination) {
        return source.length == 0;
    }

    /* rb_econv_add_transcoder_at */
    void addTranscoderAt(Transcoder transcoder, int i) {
        if (numTranscoders == elements.length) {
            EConvElement[] tmp = new EConvElement[elements.length * 2];
            System.arraycopy(elements, 0, tmp, 0, i);
            System.arraycopy(elements, i, tmp, i + 1, elements.length - i);
            elements = tmp;
        } else {
            System.arraycopy(elements, i, elements, i + 1, elements.length - i - 1);
        }

        elements[i] = new EConvElement(transcoder.transcoding(0));
        elements[i].allocate(4096);
        numTranscoders++;

        if (!decorator(transcoder.source, transcoder.destination)) {
            for (int j = numTranscoders - 1; i <= j; j--) {
                Transcoding tc = elements[j].transcoding;
                Transcoder tr = tc.transcoder;
                if (!decorator(tr.source, tr.destination)) {
                    lastTranscoding = tc;
                    break;
                }
            }
        }
    }

    /* trans_sweep */
    private int transSweep(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags, int start) {
        boolean try_ = true;

        Ptr ipp = null;
        Ptr opp = null;

        while (try_) {
            try_ = false;
            for (int i = start; i < numTranscoders; i++) {
                EConvElement te = elements[i];

                final int is, os;
                final byte[] ibytes, obytes;
                EConvElement previousTE = null;
                boolean ippIsStart = false;
                boolean oppIsEnd = false;

                if (i == 0) {
                    ipp = inPtr;
                    is = inStop;
                    ibytes = in;
                } else {
                    previousTE = elements[i - 1];
                    ipp = new Ptr(previousTE.dataStart);
                    ippIsStart = true;
                    is = previousTE.dataEnd;
                    ibytes = previousTE.bytes;
                }

                if (i == numTranscoders - 1) {
                    opp = outPtr;
                    os = outStop;
                    obytes = out;
                } else {
                    if (te.bufStart != te.dataStart) {
                        int len = te.dataEnd - te.dataStart;
                        int off = te.dataStart - te.bufStart;
                        System.arraycopy(te.bytes, te.dataStart, te.bytes, te.bufStart, len);
                        te.dataStart = te.bufStart;
                        te.dataEnd -= off;
                    }
                    opp = new Ptr(te.dataEnd);
                    oppIsEnd = true;
                    os = te.bufEnd;
                    obytes = te.bytes;
                }

                int f = flags;
                if (numFinished != i) f |= PARTIAL_INPUT;

                if (i == 0 && (flags & AFTER_OUTPUT) != 0) {
                    start = 1;
                    flags &= ~AFTER_OUTPUT;
                }

                if (i != 0) f &= ~AFTER_OUTPUT;

                int iold = ipp.p;
                int oold = opp.p;
                EConvResult res;
                te.lastResult = res = te.transcoding.convert(ibytes, ipp, is, obytes, opp, os, f);

                if (ippIsStart) previousTE.dataStart = ipp.p;
                if (oppIsEnd) te.dataEnd = opp.p;

                if (iold != ipp.p || oold != opp.p) try_ = true;

                switch (res) {
                case InvalidByteSequence:
                case IncompleteInput:
                case UndefinedConversion:
                case AfterOutput:
                    return i;

                case DestinationBufferFull:
                case SourceBufferEmpty:
                    break;

                case Finished:
                    numFinished = i + 1;
                    break;
                }

            }
        }

        return -1;
    }

    /* rb_trans_conv */
    private EConvResult transConv(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags, Ptr resultPositionPtr) {
        // null check

        if (elements[0].lastResult == EConvResult.AfterOutput) elements[0].lastResult = EConvResult.SourceBufferEmpty;

        for (int i = numTranscoders - 1; 0 <= i; i--) {
            switch (elements[i].lastResult) {
            case InvalidByteSequence:
            case IncompleteInput:
            case UndefinedConversion:
            case AfterOutput:
            case Finished:
                return transConvNeedReport(in, inPtr, inStop, out, outPtr, outStop, flags, resultPositionPtr, i + 1, i);
            case DestinationBufferFull:
            case SourceBufferEmpty:
                break;
            default:
                throw new InternalException("unexpected transcode last result");
            }
        }

        /* /^[sd]+$/ is confirmed. but actually /^s*d*$/. */

        if (elements[numTranscoders - 1].lastResult == EConvResult.DestinationBufferFull && (flags & AFTER_OUTPUT) != 0) {
            EConvResult res = transConv(NULL_STRING, Ptr.NULL, 0, out, outPtr, outStop, (flags & ~AFTER_OUTPUT) | PARTIAL_INPUT, resultPositionPtr);
            return res.isSourceBufferEmpty() ? EConvResult.AfterOutput : res;
        }

        return transConvNeedReport(in, inPtr, inStop, out, outPtr, outStop, flags, resultPositionPtr, 0, -1);
    }

    private EConvResult transConvNeedReport(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags, Ptr resultPositionPtr,
            int sweepStart, int needReportIndex) {
        do {
            needReportIndex = transSweep(in, inPtr, inStop, out, outPtr, outStop, flags, sweepStart);
            sweepStart = needReportIndex + 1;
        } while (needReportIndex != -1 && needReportIndex != numTranscoders - 1);

        for (int i = numTranscoders - 1; i >= 0; i--) {
            if (elements[i].lastResult != EConvResult.SourceBufferEmpty) {
                EConvResult res = elements[i].lastResult;
                switch (res) {
                case InvalidByteSequence:
                case IncompleteInput:
                case UndefinedConversion:
                case AfterOutput:
                    elements[i].lastResult = EConvResult.SourceBufferEmpty;
                }

                if (resultPositionPtr != null) resultPositionPtr.p = i;
                return res;
            }
        }
        if (resultPositionPtr != null) resultPositionPtr.p = -1;
        return EConvResult.SourceBufferEmpty;
    }

    /* rb_econv_convert0 */
    private EConvResult convertInternal(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags) {
        lastError.reset();

        EConvResult res;
        int len;

        if (numTranscoders == 0) {
            if (inBuf.bytes != null && inBuf.dataStart != inBuf.dataEnd) {
                if (outStop - outPtr.p < inBuf.dataEnd - inBuf.dataStart) {
                    len = outStop - outPtr.p;
                    System.arraycopy(inBuf, inBuf.dataStart, out, outPtr.p, len);
                    outPtr.p = outStop;
                    inBuf.dataStart += len;
                    return convertInternalResult(EConvResult.DestinationBufferFull, null);
                }
                len = inBuf.dataEnd - inBuf.dataStart;
                System.arraycopy(inBuf, inBuf.dataStart, out, outPtr.p, len);
                outPtr.p += len;
                inBuf.dataStart = inBuf.dataEnd = inBuf.bufStart;
                if ((flags & AFTER_OUTPUT) != 0) return convertInternalResult(EConvResult.AfterOutput, null);
            }

            if (outStop - outPtr.p < inStop - inPtr.p) {
                len = outStop - outPtr.p;
            } else {
                len = inStop - inPtr.p;
            }

            if (len > 0 && (flags & AFTER_OUTPUT) != 0) {
                out[outPtr.p++] = in[inPtr.p++];
                return convertInternalResult(EConvResult.AfterOutput, null);
            }

            System.arraycopy(in, inPtr.p, out, outPtr.p, len);
            outPtr.p += len;
            inPtr.p += len;

            if (inPtr.p != inStop) {
                res = EConvResult.DestinationBufferFull;
            } else if ((flags & PARTIAL_INPUT) != 0) {
                res = EConvResult.SourceBufferEmpty;
            } else {
                res = EConvResult.Finished;
            }
            return convertInternalResult(res, null);
        }

        boolean hasOutput = false;
        EConvElement elem = elements[numTranscoders - 1];
        if (elem.bytes != null) {
            int dataStart = elem.dataStart;
            int dataEnd = elem.dataEnd;
            byte[] data = elem.bytes;
            if (dataStart != dataEnd) {
                if (outStop - outPtr.p < dataEnd - dataStart) {
                    len = outStop - outPtr.p;
                    System.arraycopy(data, dataStart, out, outPtr.p, len);
                    outPtr.p = outStop;
                    elem.dataStart += len;
                    return convertInternalResult(EConvResult.DestinationBufferFull, null);
                }
                len = dataEnd - dataStart;
                System.arraycopy(data, dataStart, out, outPtr.p, len);
                outPtr.p += len;
                elem.dataStart = elem.dataEnd = elem.bufStart;
                hasOutput = true;
            }
        }

        Ptr resultPosition = new Ptr(0);
        if (inBuf != null && inBuf.dataStart != inBuf.dataEnd) {
            Ptr inDataStartPtr = new Ptr(inBuf.dataStart);
            res = transConv(inBuf.bytes, inDataStartPtr, inBuf.dataEnd, out, outPtr, outStop, (flags & ~AFTER_OUTPUT) | PARTIAL_INPUT, resultPosition);
            inBuf.dataStart = inDataStartPtr.p;
            if (!res.isSourceBufferEmpty()) return convertInternalResult(EConvResult.SourceBufferEmpty, resultPosition);
        }

        if (hasOutput && (flags & AFTER_OUTPUT) != 0 && inPtr.p != inStop) {
            inStop = inPtr.p;
            res = transConv(in, inPtr, inStop, out, outPtr, outStop, flags, resultPosition);
            if (res.isSourceBufferEmpty()) res = EConvResult.AfterOutput;
        } else if ((flags & AFTER_OUTPUT) != 0 || numTranscoders == 1) {
            res = transConv(in, inPtr, inStop, out, outPtr, outStop, flags, resultPosition);
        } else {
            flags |= AFTER_OUTPUT;
            do {
                res = transConv(in, inPtr, inStop, out, outPtr, outStop, flags, resultPosition);
            } while (res.isAfterOutput());
        }

        return convertInternalResult(res, resultPosition);
    }

    private EConvResult convertInternalResult(EConvResult res, Ptr resultPosition) {
        lastError.result = res;
        switch (res) {
        case InvalidByteSequence:
        case IncompleteInput:
        case UndefinedConversion:
            Transcoding errorTranscoding = elements[resultPosition.p].transcoding;
            lastError.errorTranscoding = errorTranscoding;
            lastError.source = errorTranscoding.transcoder.source;
            lastError.destination = errorTranscoding.transcoder.destination;
            lastError.errorBytes = errorTranscoding.readBuf;
            lastError.errorBytesP = 0;
            lastError.errorBytesEnd = errorTranscoding.recognizedLength; // ???
            lastError.readAgainLength = errorTranscoding.readAgainLength;
        }
        return res;
    }

    /* rb_econv_convert */
    public EConvResult convert(byte[] in, Ptr inPtr, int inStop, byte[] out, Ptr outPtr, int outStop, int flags) {
        started = true;

        // null check

        resume: while (true) {
            EConvResult ret = convertInternal(in, inPtr, inStop, out, outPtr, outStop, flags);
            if (ret.isInvalidByteSequence() || ret.isIncompleteInput()) {
                switch (this.flags & INVALID_MASK) {
                case INVALID_REPLACE:
                    if (outputReplacementCharacter() == 0) continue resume;
                }
            }

            if (ret.isUndefinedConversion()) {
                switch (this.flags & UNDEF_MASK) {
                case UNDEF_REPLACE:
                    if (outputReplacementCharacter() == 0) continue resume;
                    break;
                case UNDEF_HEX_CHARREF:
                    if (outputHexCharref() == 0) continue resume;
                    break;
                }
            }
            return ret;
        }
    }

    /* output_hex_charref */
    private int outputHexCharref() {
        final byte[] utfBytes;
        final int utfP;
        int utfLen;

        if (caseInsensitiveEquals(lastError.source, "UTF-32BE".getBytes())) {
            utfBytes = lastError.errorBytes;
            utfP = lastError.errorBytesP;
            utfLen = lastError.errorBytesEnd - lastError.errorBytesP;
        } else {
            Ptr utfLenA = new Ptr();
            byte[] utfBuf = new byte[1024]; // ??
            utfBytes = allocateConvertedString(lastError.source, "UTF-32BE".getBytes(), lastError.errorBytes, lastError.errorBytesP, lastError.errorBytesEnd
                    - lastError.errorBytesP, utfBuf, utfLenA);

            if (utfBytes == null) return -1;
            utfP = 0;
            utfLen = utfLenA.p;
        }

        if (utfLen % 4 != 0) return -1;

        int p = utfP;
        while (4 <= utfLen) {
            int u = 0; // long ??
            u += (utfBytes[p] & 0xff) << 24;
            u += (utfBytes[p + 1] & 0xff) << 16;
            u += (utfBytes[p + 2] & 0xff) << 8;
            u += (utfBytes[p + 3]);
            byte[] charrefbuf = String.format("&#x%X;", u).getBytes(); // use faster sprintf ??

            if (insertOuput(charrefbuf, charrefbuf.length, "US-ASCII".getBytes()) == -1) return -1;

            p += 4;
            utfLen -= 4;
        }

        return 0;
    }

    /* rb_econv_encoding_to_insert_output */
    private byte[] encodingToInsertOutput() {
        Transcoding transcoding = lastTranscoding;
        if (transcoding == null) return NULL_STRING;
        Transcoder transcoder = transcoding.transcoder;
        return transcoder.compatibility.isEncoder() ? transcoder.source : transcoder.destination;
    }

    /* allocate_converted_string */
    private static byte[] allocateConvertedString(byte[] source, byte[] destination, byte[] str, int strP, int strLen, byte[] callerDstBuf, Ptr dstLenPtr) {
        int dstBufSize;

        if (callerDstBuf != null) {
            dstBufSize = callerDstBuf.length;
        } else if (strLen == 0) {
            dstBufSize = 1; // ??
        } else {
            dstBufSize = strLen;
        }

        EConv ec = TranscoderDB.open(source, destination, 0);
        if (ec == null) return null;

        byte[] dstStr;
        if (callerDstBuf != null) {
            dstStr = callerDstBuf;
        } else {
            dstStr = new byte[dstBufSize];
        }

        int dstLen = 0;
        Ptr sp = new Ptr(strP);
        Ptr dp = new Ptr(dstLen);
        EConvResult res = ec.convert(str, sp, strP + strLen, dstStr, dp, dstBufSize, 0);
        dstLen = dp.p;

        while (res.isDestinationBufferFull()) {
            dstBufSize *= 2;
            byte[] tmp = new byte[dstBufSize];
            System.arraycopy(dstStr, 0, tmp, 0, dstBufSize / 2);
            dstStr = tmp;

            dp.p = dstLen; // ??
            res = ec.convert(str, sp, strP + strLen, dstStr, dp, dstBufSize, 0);
            dstLen = dp.p;
        }

        if (!res.isFinished()) return null;

        ec.close();
        dstLenPtr.p = dstLen;

        return dstStr;
    }

    /* rb_econv_insert_output */
    int insertOuput(byte[] str, int strLen, byte[] strEncoding) {
        byte[] insertEncoding = encodingToInsertOutput();
        byte[] insertBuf = null;

        started = true;

        if (strLen == 0) return 0;

        final byte[] insertStr;
        final int insertLen;
        if (caseInsensitiveEquals(insertEncoding, strEncoding)) {
            insertStr = str;
            insertLen = strLen;
        } else {
            Ptr insertLenP = new Ptr();
            insertBuf = new byte[4096];
            insertStr = allocateConvertedString(strEncoding, insertEncoding, str, 0, strLen, insertBuf, insertLenP);
            insertLen = insertLenP.p;
            if (insertStr == null) return -1;
        }

        int need = insertLen;

        final int lastTranscodingIndex = numTranscoders - 1;
        final Transcoding transcoding;

        Buffer buf;

        if (numTranscoders == 0) {
            transcoding = null;
            buf = inBuf;
        } else if (elements[lastTranscodingIndex].transcoding.transcoder.compatibility.isEncoder()) {
            transcoding = elements[lastTranscodingIndex].transcoding;
            need += transcoding.readAgainLength;
            if (need < insertLen) return -1;

            if (lastTranscodingIndex == 0) {
                buf = inBuf;
            } else {
                buf = elements[lastTranscodingIndex - 1];
            }
        } else {
            transcoding = elements[lastTranscodingIndex].transcoding;
            buf = elements[lastTranscodingIndex];
        }

        if (buf == null) {
            buf = new Buffer();
            buf.allocate(need);
        } else if ((buf.bufEnd - buf.dataEnd) < need) {
            System.arraycopy(buf.bytes, buf.dataStart, buf.bytes, buf.bufStart, buf.dataEnd - buf.dataStart);
            buf.dataEnd = buf.dataStart + (buf.dataEnd - buf.dataStart);
            buf.dataStart = buf.bufStart;

            if ((buf.bufEnd - buf.dataEnd) < need) {
                int s = (buf.dataEnd - buf.bufStart) + need;
                if (need > s) return -1;
                byte[] tmp = new byte[s];
                System.arraycopy(buf.bytes, buf.bufStart, tmp, 0, s); // ??
                buf.bytes = tmp;
                buf.dataStart = 0;
                buf.dataEnd = buf.dataEnd - buf.bufStart;
                buf.bufStart = 0;
                buf.bufEnd = 0;
            }
        }

        System.arraycopy(insertStr, 0, buf.bytes, buf.dataEnd, insertLen);
        buf.dataEnd += insertLen;
        if (transcoding != null && transcoding.transcoder.compatibility.isEncoder()) {
            System.arraycopy(transcoding.readBuf, transcoding.recognizedLength, buf, buf.dataEnd, transcoding.readAgainLength);
            buf.dataEnd = transcoding.readAgainLength;
            transcoding.readAgainLength = 0;
        }

        return 0;
    }

    /* rb_econv_close */
    public void close() {
        for (int i = 0; i < numTranscoders; i++) {
            elements[i].transcoding.close();
        }
    }

    /* rb_econv_putbackable */
    int putbackable() {
        return numTranscoders == 0 ? 0 : elements[0].transcoding.readAgainLength;
    }

    /* rb_econv_putback */
    void putback(byte[] bytes, int p, int len) {
        if (numTranscoders == 0 || len == 0) return;
        Transcoding transcoding = elements[0].transcoding;
        System.arraycopy(transcoding.readBuf, transcoding.recognizedLength + transcoding.readAgainLength, bytes, p, len);
        transcoding.readAgainLength -= len;
    }

    /* rb_econv_add_converter */
    public boolean addConverter(byte[] source, byte[] destination, int n) {
        if (started) return false;
        TranscoderDB.Entry entry = TranscoderDB.getEntry(source, destination);
        if (entry == null) return false;
        Transcoder transcoder = entry.getTranscoder();
        if (transcoder == null) return false;
        addTranscoderAt(transcoder, n);
        return true;
    }

    /* rb_econv_decorate_at */
    boolean decorateAt(byte[] decorator, int n) {
        return addConverter(NULL_STRING, decorator, n);
    }

    /* rb_econv_decorate_at_last */
    boolean decorateAtFirst(byte[] decorator) {
        if (numTranscoders == 0) return decorateAt(decorator, 0);
        Transcoder transcoder = elements[0].transcoding.transcoder;

        if (!decorator(transcoder.source, transcoder.destination) && transcoder.compatibility.isDecoder()) {
            return decorateAt(decorator, 1);
        }

        return decorateAt(decorator, 0);
    }

    /* rb_econv_decorate_at_last */
    boolean decorateAtLast(byte[] decorator) {
        if (numTranscoders == 0) return decorateAt(decorator, 0);
        Transcoder transcoder = elements[numTranscoders - 1].transcoding.transcoder;

        if (!decorator(transcoder.source, transcoder.destination) && transcoder.compatibility.isDecoder()) {
            return decorateAt(decorator, numTranscoders - 1);
        }

        return decorateAt(decorator, numTranscoders);
    }

    /* rb_econv_binmode */
    private void binmode() {
        Transcoder[] transcoders = new Transcoder[3];
        int n = 0;
        if ((flags & UNIVERSAL_NEWLINE_DECORATOR) != 0) {
            TranscoderDB.Entry entry = TranscoderDB.getEntry(NULL_STRING, "universal_newline".getBytes());
            if (entry.getTranscoder() != null) transcoders[n++] = entry.getTranscoder();
        }

        if ((flags & CRLF_NEWLINE_DECORATOR) != 0) {
            TranscoderDB.Entry entry = TranscoderDB.getEntry(NULL_STRING, "crlf_newline".getBytes());
            if (entry.getTranscoder() != null) transcoders[n++] = entry.getTranscoder();
        }

        if ((flags & CR_NEWLINE_DECORATOR) != 0) {
            TranscoderDB.Entry entry = TranscoderDB.getEntry(NULL_STRING, "cr_newline".getBytes());
            if (entry.getTranscoder() != null) transcoders[n++] = entry.getTranscoder();
        }

        int nTrans = numTranscoders;
        int j = 0;
        for (int i = 0; i < nTrans; i++) {
            int k;
            for (k = 0; k < n; k++) {
                if (transcoders[k] == elements[i].transcoding.transcoder) break;
            }

            if (k == n) {
                elements[j] = elements[i];
                j++;
            } else {
                elements[i].transcoding.close();
                numTranscoders--;
            }

        }
        flags &= ~NEWLINE_DECORATOR_MASK;
    }

    /* econv_description, rb_econv_open_exc, make_econv_exception */
    /* more_output_buffer */

    /* make_replacement */
    public int makeReplacement() {
        if (replacementString != null) return 0;

        byte[] insEnc = encodingToInsertOutput();

        final byte[] replEnc;
        final int len;
        final byte[] replacement;

        if (insEnc.length != 0) {
            // Transcoding transcoding = lastTranscoding;
            // Transcoder transcoder = transcoding.transcoder;
            // Encoding enc = EncodingDB.getEncodings().get(transcoder.destination).getEncoding();

            // get_replacement_character
            if (caseInsensitiveEquals(insEnc, "UTF-8".getBytes())) {
                len = 3;
                replEnc = "UTF-8".getBytes();
                replacement = new byte[] { (byte) 0xEF, (byte) 0xBF, (byte) 0xBD };
            } else {
                len = 1;
                replEnc = "US-ASCII".getBytes();
                replacement = new byte[] { '?' };
            }
        } else {
            len = 1;
            replEnc = NULL_STRING;
            replacement = new byte[] { '?' };
        }

        replacementString = replacement;
        replacementLength = len;
        replacementEncoding = replEnc;
        return 0;
    }

    /* rb_econv_set_replacement */
    public int setReplacement(byte[] str, int p, int len, byte[] encname) {
        byte[] encname2 = encodingToInsertOutput();

        final byte[] str2;
        final int p2 = 0;
        final int len2;

        if (caseInsensitiveEquals(encname, encname2)) {
            str2 = new byte[len];
            System.arraycopy(str, p, str2, 0, len); // ??
            len2 = len;
            encname2 = encname;
        } else {
            Ptr len2p = new Ptr();
            str2 = allocateConvertedString(encname, encname2, str, p, len, null, len2p);
            if (str == null) return -1;
            len2 = len2p.p;
        }

        replacementString = str2;
        replacementLength = len2;
        replacementEncoding = encname2;
        return 0;
    }

    /* output_replacement_character */
    int outputReplacementCharacter() {
        if (makeReplacement() == -1) return -1;
        if (insertOuput(replacementString, replacementLength, replacementEncoding) == -1) return -1;
        return 0;
    }

    public String toStringFull() {
        String s = "EConv " + new String(source) + " => " + new String(destination) + "\n";
        s += "  started: " + started + "\n";
        s += "  replacement string: " + (replacementString == null ? "null" : new String(replacementString, 0, replacementLength)) + "\n";
        s += "  replacement encoding: " + (replacementEncoding == null ? "null" : new String(replacementEncoding)) + "\n";
        s += "\n";
        for (int i = 0; i < numTranscoders; i++) {
            s += "  element " + i + ": " + elements[i].toString() + "\n";
        }

        s += "\n";
        s += "  lastTranscoding: " + lastTranscoding + "\n";
        s += "  last error: " + (lastError == null ? "null" : lastError.toString());

        return s;
    }
}