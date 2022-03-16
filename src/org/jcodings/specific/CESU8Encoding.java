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
package org.jcodings.specific;

import org.jcodings.Config;
import org.jcodings.IntHolder;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.exception.ErrorCodes;
import org.jcodings.unicode.UnicodeEncoding;

import static java.lang.Integer.toUnsignedLong;

public final class CESU8Encoding extends UnicodeEncoding {
  static final boolean USE_INVALID_CODE_SCHEME = true;

  protected CESU8Encoding() {
    super("CESU-8", 1, 6, CESU8EncLen, CESU8Trans);
  }

  @Override
  public String getCharsetName() {
    return "CESU-8";
  }

  @Override
  public int length(byte[] bytes, int p, int end) {
    int b = bytes[p] & 0xff;
    if (b <= 127) {
      return 1;
    }
    int s = TransZero[b];
    if (s < 0)
      return CHAR_INVALID;
    return lengthForOneUptoSix(bytes, p, end, b, s);
  }

  private int lengthForOneUptoSix(byte[] bytes, int p, int end, int b, int s) {
    if (++p == end) {
      return missing(b, 1);
    }
    s = Trans[s][bytes[p] & 0xff];
    if (s < 0) {
      return s == A ? 2 : CHAR_INVALID;
    }
    if (++p == end) {
      return missing(b, s == 4 ? 4 : TransZero[b] - 2);
    }
    s = Trans[s][bytes[p] & 0xff];
    if (s < 0) {
      return s == A ? 3 : CHAR_INVALID;
    }
    if (++p == end)
      return missing(b, 3);
    s = Trans[s][bytes[p] & 0xff];
    if (s < 0) {
      return s == A ? 4 : CHAR_INVALID;
    }
    if (++p == end)
      return missing(b, 2);
    s = Trans[s][bytes[p] & 0xff];
    if (s < 0) {
      return s == A ? 5 : CHAR_INVALID;
    }
    if (++p == end)
      return missing(b, 1);
    s = Trans[s][bytes[p] & 0xff];
    return s == A ? 6 : CHAR_INVALID;
  }

  @Override
  public boolean isNewLine(byte[] bytes, int p, int end) {
    if (p < end) {
      if (bytes[p] == (byte) 0x0a)
        return true;

      if (Config.USE_UNICODE_ALL_LINE_TERMINATORS) {
        if (!Config.USE_CRNL_AS_LINE_TERMINATOR) {
          if (bytes[p] == (byte) 0x0d)
            return true;
        }

        if (p + 1 < end) {
          if (bytes[p + 1] == (byte) 0x85 && bytes[p] == (byte) 0xc2)
            return true;
          if (p + 2 < end) {
            if ((bytes[p + 2] == (byte) 0xa8 || bytes[p + 2] == (byte) 0xa9) &&
                bytes[p + 1] == (byte) 0x80 && bytes[p] == (byte) 0xe2)
              return true;
          }
        }
      }
    }
    return false;
  }

  private static final int INVALID_CODE_FE = 0xfffffffe;
  private static final int INVALID_CODE_FF = 0xffffffff;
  private static final int VALID_CODE_LIMIT = 0x0010ffff;

  @Override
  public int codeToMbcLength(int code) {
    if ((code & 0xffffff80) == 0) {
      return 1;
    } else if ((code & 0xfffff800) == 0) {
      return 2;
    } else if ((code & 0xffff0000) == 0) {
      return 3;
    } else if (toUnsignedLong(code) <= VALID_CODE_LIMIT) {
      return 6;
    } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FE) {
      return 1;
    } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FF) {
      return 1;
    } else {
      return ErrorCodes.ERR_TOO_BIG_WIDE_CHAR_VALUE;
    }
  }

  @Override
  public int mbcToCode(byte[] bytes, int p, int end) {
    int len = length(bytes, p, end);
    int c = bytes[p] & 0xff;

    switch (len) {
      case 1:
        return c;
      case 2:
        return ((c & 0x1F)  << 6) | (bytes[p + 1] & 0xff & 0x3f);
      case 3:
        return ((c & 0xF) << 12) | ((bytes[p + 1] & 0xff & 0x3f) << 6) | (bytes[p + 2] & 0xff & 0x3f);
      case 6:
        {
            int high = ((c & 0xF) << 12) | ((bytes[p + 1] & 0xff & 0x3f) << 6) | (bytes[p + 2] & 0xff & 0x3f);
            int low  = ((bytes[p + 3] & 0xff & 0xF) << 12) | ((bytes[p + 4] & 0xff & 0x3f) << 6) | (bytes[p + 5] & 0xff & 0x3f);
            return ((high & 0x03ff) << 10) + (low & 0x03ff) + 0x10000;
        }
    }

    if (USE_INVALID_CODE_SCHEME) {
      if (c > 0xfd) {
        return ((c == 0xfe) ? INVALID_CODE_FE : INVALID_CODE_FF);
      }
    }
    return c;
  }

  static byte trailS(int code, int shift) {
    return (byte) (((code >>> shift) & 0x3f) | 0x80);
  }

  static byte trail0(int code) {
    return (byte) ((code & 0x3f) | 0x80);
  }

  static byte trailS(long code, int shift) {
    return (byte) (((code >>> shift) & 0x3f) | 0x80);
  }

  static byte trail0(long code) {
    return (byte) ((code & 0x3f) | 0x80);
  }

  @Override
  public int codeToMbc(int code, byte[] bytes, int p) {
    int p_ = p;
    if ((code & 0xffffff80) == 0) {
      bytes[p_] = (byte) code;
      return 1;
    } else {
      if ((code & 0xfffff800) == 0) {
        bytes[p_++] = (byte) (((code >>> 6) & 0x1f) | 0xc0);
      } else if ((code & 0xffff0000) == 0) {
        bytes[p_++] = (byte) (((code >>> 12) & 0x0f) | 0xe0);
        bytes[p_++] = trailS(code, 6);
      } else if (toUnsignedLong(code) <= VALID_CODE_LIMIT) {
        long high = (code >> 10) + 0xD7C0;
        code = (code & 0x3FF) + 0xDC00;
        bytes[p_++] = (byte)(((high>>12) & 0x0f) | 0xe0);
        bytes[p_++] = trailS(high, 6);
        bytes[p_++] = trail0(high);
        bytes[p_++] = (byte)(((code>>12) & 0x0f) | 0xe0);
        bytes[p_++] = trailS(code, 6);
      } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FE) {
        bytes[p_] = (byte) 0xfe;
        return 1;
      } else if (USE_INVALID_CODE_SCHEME && code == INVALID_CODE_FF) {
        bytes[p_] = (byte) 0xff;
        return 1;
      } else {
        return ErrorCodes.ERR_TOO_BIG_WIDE_CHAR_VALUE;
      }
      bytes[p_++] = trail0(code);
      return p_ - p;
    }
  }

  @Override
  public int mbcCaseFold(int flag, byte[] bytes, IntHolder pp, int end, byte[] fold) {
    int p = pp.value;
    int foldP = 0;

    if (isMbcAscii(bytes[p])) {

      if (Config.USE_UNICODE_CASE_FOLD_TURKISH_AZERI) {
        if ((flag & Config.CASE_FOLD_TURKISH_AZERI) != 0) {
          if (bytes[p] == (byte) 0x49) {
            fold[foldP++] = (byte) 0xc4;
            fold[foldP] = (byte) 0xb1;
            pp.value++;
            return 2;
          }
        }
      }

      fold[foldP] = AsciiTables.ToLowerCaseTable[bytes[p] & 0xff];
      pp.value++;
      return 1;
    } else {
      return super.mbcCaseFold(flag, bytes, pp, end, fold);
    }
  }

  @Override
  public int[] ctypeCodeRange(int ctype, IntHolder sbOut) {
    sbOut.value = 0x80;
    return super.ctypeCodeRange(ctype);
  }

  private static boolean utf8IsLead(int c) {
    return ((c & 0xc0) & 0xff) != 0x80;
  }

  @Override
  public int leftAdjustCharHead(byte[] bytes, int p, int s, int end) {
    if (s <= p)
      return s;
    int p_ = s;
    while (!utf8IsLead(bytes[p_] & 0xff) && p_ > p)
      p_--;
    return p_;
  }

  @Override
  public boolean isReverseMatchAllowed(byte[] bytes, int p, int end) {
    return true;
  }

  private static final int CESU8EncLen[] = {
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
      3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3,
      1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
  };

  static final int CESU8Trans[][] = new int[][] {
        { /* S0   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 1 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 2 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 3 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 4 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 5 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 6 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 7 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* d */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* e */ 2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 3, 4, 3, 3,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S1   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* 9 */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* a */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* b */ A, A, A, A, A, A, A, A, A, A, A, A, A, A, A, A,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S2   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S3   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* 9 */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* a */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
      },
      { /* S4   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* 9 */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* a */ 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5, 5,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S5   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
          /* 9 */ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
          /* a */ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
          /* b */ 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S6   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, 7, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
        },
        { /* S7   0  1  2  3  4  5  6  7  8  9  a  b  c  d  e  f */
          /* 0 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 1 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 2 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 3 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 4 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 5 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 6 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 7 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 8 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* 9 */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* a */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* b */ 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
          /* c */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* d */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* e */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F,
          /* f */ F, F, F, F, F, F, F, F, F, F, F, F, F, F, F, F
      }
  };

  public static final CESU8Encoding INSTANCE = new CESU8Encoding();
}
