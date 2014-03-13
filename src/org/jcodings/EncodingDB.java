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
package org.jcodings;

import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.specific.ASCIIEncoding;
import org.jcodings.util.CaseInsensitiveBytesHash;

public class EncodingDB {
    public static final class Entry {
        private static int count;

        private final Entry base;
        private Encoding encoding;
        private final String encodingClass;
        private final int index;
        private final boolean isDummy;
        private final byte[]name;

        private Entry (byte[]name, String encodingClass, Entry base, boolean isDummy) {
            this.name = name;
            this.encodingClass = encodingClass;
            this.base = base;
            this.isDummy = isDummy;
            index = count++;
        }

        // declare
        Entry(String encodingClass) {
            this(null, encodingClass, null, false);
        }

        // replicate
        Entry(byte[]name, Entry base) {
            this(name, base.encodingClass, base, false);
        }

        // dummy
        Entry(byte[]name) {
            this(name, ascii.encodingClass, ascii, true);
        }

        @Override
        public int hashCode() {
            return encodingClass.hashCode();
        }

        public Entry getBase() {
            return base;
        }

        public Encoding getEncoding() {
            if (encoding == null) {
                if (name == null) {
                    encoding = Encoding.load(encodingClass);
                } else {
                    if (isDummy) {
                        encoding = ASCIIEncoding.DUMMY.replicate(name);
                    } else {
                        encoding = Encoding.load(encodingClass).replicate(name);
                    }
                }
            }
            return encoding;
        }

        public String getEncodingClass() {
            return encodingClass;
        }

        public int getIndex() {
            return index;
        }

        public boolean isDummy() {
            return isDummy;
        }
    }

    private static String[] builtin = {
        "ASCII-8BIT",   "ASCII",
        "Big5",         "BIG5",
        "Big5-HKSCS",   "Big5HKSCS",
        "Big5-UAO",     "Big5UAO",
        "CP949",        "CP949",
        "Emacs-Mule",   "EmacsMule",
        "EUC-JP",       "EUCJP",
        "EUC-KR",       "EUCKR",
        "EUC-TW",       "EUCTW",
        "GB18030",      "GB18030",
        "GBK",          "GBK",
        "ISO-8859-1",   "ISO8859_1",
        "ISO-8859-2",   "ISO8859_2",
        "ISO-8859-3",   "ISO8859_3",
        "ISO-8859-4",   "ISO8859_4",
        "ISO-8859-5",   "ISO8859_5",
        "ISO-8859-6",   "ISO8859_6",
        "ISO-8859-7",   "ISO8859_7",
        "ISO-8859-8",   "ISO8859_8",
        "ISO-8859-9",   "ISO8859_9",
        "ISO-8859-10",  "ISO8859_10",
        "ISO-8859-11",  "ISO8859_11",
        // "ISO-8859-12",  "ISO8859_12",
        "ISO-8859-13",  "ISO8859_13",
        "ISO-8859-14",  "ISO8859_14",
        "ISO-8859-15",  "ISO8859_15",
        "ISO-8859-16",  "ISO8859_16",
        "KOI8-R",       "KOI8R",
        "KOI8-U",       "KOI8U",
        "Shift_JIS",    "SJIS",
        "US-ASCII",     "USASCII",
        "UTF-8",        "UTF8",
        "UTF-16BE",     "UTF16BE",
        "UTF-16LE",     "UTF16LE",
        "UTF-32BE",     "UTF32BE",
        "UTF-32LE",     "UTF32LE",
        "Windows-1251", "CP1251",
        "GB2312",       "EUCKR",         // done via rb_enc_register
        "Windows-31J",  "Windows_31J"           // TODO: Windows-31J is actually a variant of SJIS
    };

    static Entry ascii;

    static final CaseInsensitiveBytesHash<Entry> encodings = new CaseInsensitiveBytesHash<Entry>(builtin.length);
    static final CaseInsensitiveBytesHash<Entry> aliases = new CaseInsensitiveBytesHash<Entry>(builtin.length);

    public static final CaseInsensitiveBytesHash<Entry> getEncodings() {
        return encodings;
    }

    public static final CaseInsensitiveBytesHash<Entry> getAliases() {
        return aliases;
    }

    public static void declare(String name, String encodingClass) {
        byte[]bytes = name.getBytes();
        if (encodings.get(bytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALREADY_REGISTERED, name);
        encodings.putDirect(bytes, new Entry(encodingClass));
    }

    public static void alias(String alias, String original) {
        byte[]origBytes = original.getBytes();
        Entry originalEntry = encodings.get(origBytes);
        if (originalEntry == null) throw new InternalException(ErrorMessages.ERR_NO_SUCH_ENCODNG, original);
        byte[]aliasBytes = alias.getBytes();
        if (aliases.get(aliasBytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALIAS_ALREADY_REGISTERED, alias);
        aliases.putDirect(aliasBytes, originalEntry);
    }

    public static void replicate(String replica, String original) {
        byte[]origBytes = original.getBytes();
        Entry originalEntry = encodings.get(origBytes);
        if (originalEntry == null) throw new InternalException(ErrorMessages.ERR_NO_SUCH_ENCODNG, original);
        byte[]replicaBytes = replica.getBytes();
        if (encodings.get(replicaBytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_REPLICA_ALREADY_REGISTERED, replica);
        encodings.putDirect(replicaBytes, new Entry(replicaBytes, originalEntry));
    }

    public static void set_base(String name, String original) {
    }

    public static Entry dummy(byte[] bytes) {
        if (encodings.get(bytes) != null) throw new InternalException(ErrorMessages.ERR_ENCODING_ALREADY_REGISTERED, new String(bytes));
        Entry entry = new Entry(bytes);
        encodings.putDirect(bytes, entry);
        return entry;
    }

    public static void dummy(String name) {
        dummy(name.getBytes());
    }

    static {
        for (int i = 0; i < builtin.length / 2; i++) {
            declare(builtin[i << 1], builtin[(i << 1) + 1]);
        }
        builtin = null;

        ascii = encodings.get("ASCII-8BIT".getBytes());

        String[][]encList = EncodingList.LIST;
        for (int i = 0; i < encList.length; i++) {
            String[]enc = encList[i];

            switch (enc[0].charAt(0)) {
            case 'R':
                replicate(enc[1], enc[2]);
                break;
            case 'A':
                alias(enc[1], enc[2]);
                break;
            case 'S':
                set_base(enc[1], enc[2]);
                break;
            case 'D':
                dummy(enc[1]);
                break;
            default:
                Thread.dumpStack();
                throw new InternalException("Unknown flag: " + enc[0].charAt(0));
            }
        }
    }
}
