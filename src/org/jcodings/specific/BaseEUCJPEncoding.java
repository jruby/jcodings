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

import org.jcodings.CodeRange;
import org.jcodings.Config;
import org.jcodings.EucEncoding;
import org.jcodings.IntHolder;
import org.jcodings.ascii.AsciiTables;
import org.jcodings.constants.CharacterType;
import org.jcodings.exception.EncodingException;
import org.jcodings.exception.ErrorMessages;
import org.jcodings.exception.InternalException;
import org.jcodings.util.BytesHash;

abstract class BaseEUCJPEncoding extends EucEncoding {

    protected BaseEUCJPEncoding(int[][]Trans) {
        super(1, 3, EUCJPEncLen, Trans, AsciiTables.AsciiCtypeTable);
    }
    
    @Override
    public String toString() {
        return "EUC-JP";
    }

    @Override
    public int mbcToCode(byte[]bytes, int p, int end) {
        return mbnMbcToCode(bytes, p, end);
    }
    
    @Override
    public int codeToMbcLength(int code) {
        if (isAscii(code)) return 1;
        if (Config.VANILLA) {
            if ((code & 0xff0000) != 0) return 3;
            if ((code &   0xff00) != 0) return 2;
        } else {
            if (code > 0xffffff) return 0;
            if ((code & 0xff0000) >= 0x800000) return 3;
            if ((code & 0xff00) >= 0x8000) return 2;
        }
        throw new EncodingException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
    }
    
    @Override
    public int codeToMbc(int code, byte[]bytes, int p) {
        int p_ = p;
        if ((code & 0xff0000) != 0) bytes[p_++] = (byte)((code >> 16) & 0xff); // need mask here ??
        if ((code &   0xff00) != 0) bytes[p_++] = (byte)((code >>  8) & 0xff);
        bytes[p_++] = (byte)(code & 0xff);
        
        if (length(bytes, p, p_) != p_ - p) throw new EncodingException(ErrorMessages.ERR_INVALID_CODE_POINT_VALUE);
        return p_ - p;
    }
    
    @Override
    public int mbcCaseFold(int flag, byte[]bytes, IntHolder pp, int end, byte[]lower) {
        int p = pp.value;        
        int lowerP = 0;
        
        if (isMbcAscii(bytes[p])) {
            lower[lowerP] = AsciiTables.ToLowerCaseTable[bytes[p] & 0xff];
            pp.value++;
            return 1;
        } else {
            int len = length(bytes, p, end);
            for (int i=0; i<len; i++) {
                lower[lowerP++] = bytes[p++];
            }
            pp.value += len;
            return len; /* return byte length of converted char to lower */
        }
    }
    
    protected boolean isLead(int c) {
        return ((c - 0xa1) & 0xff) > 0xfe - 0xa1;
    }
    
    @Override
    public boolean isReverseMatchAllowed(byte[]bytes, int p, int end) {
        int c = bytes[p] & 0xff;
        return c <= 0x7e || c == 0x8e || c == 0x8f;
    }
    
    private static final int CR_Hiragana[] = {
        1,
        0xa4a1, 0xa4f3
    }; /* CR_Hiragana */
    
    private static final int CR_Katakana[] = {
        3,
        0xa5a1, 0xa5f6,
        0xaaa6, 0xaaaf,
        0xaab1, 0xaadd
    }; /* CR_Katakana */
    
    private static final int PropertyList[][] = new int[][] {
        CR_Hiragana,
        CR_Katakana
    };
    
    private static final BytesHash<Integer> CTypeNameHash = new BytesHash<Integer>();

    static {
        CTypeNameHash.put("Hiragana".getBytes(), 1 + CharacterType.MAX_STD_CTYPE);
        CTypeNameHash.put("Katakana".getBytes(), 2 + CharacterType.MAX_STD_CTYPE);        
    }
    
    @Override
    public int propertyNameToCType(byte[]bytes, int p, int end) {
        Integer ctype;
        if ((ctype = CTypeNameHash.get(bytes, p, end)) == null) {
            return super.propertyNameToCType(bytes, p, end);
        }
        return ctype;
    }
    
    @Override
    public boolean isCodeCType(int code, int ctype) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            if (code < 128) { 
                // ctype table is configured with ASCII
                return isCodeCTypeInternal(code, ctype);
            } else {
                if (isWordGraphPrint(ctype)) {
                    return codeToMbcLength(code) > 1;
                }
            }
        } else {
            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return CodeRange.isInCodeRange(PropertyList[ctype], code);
        }
        return false;        
    }
    
    @Override 
    public int[]ctypeCodeRange(int ctype, IntHolder sbOut) {
        if (ctype <= CharacterType.MAX_STD_CTYPE) {
            return null;
        } else {
            sbOut.value = 0x80;
            
            ctype -= (CharacterType.MAX_STD_CTYPE + 1);
            if (ctype >= PropertyList.length) throw new InternalException(ErrorMessages.ERR_TYPE_BUG);
            return PropertyList[ctype];
        }
    }

    static final int EUCJPEncLen[] = {
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 3,
        1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1,
        1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2,
        2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 1
    };
}
