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

public interface Config {
    final boolean VANILLA = false;
    
    final int ENC_CASE_FOLD_TURKISH_AZERI = (1<<20);
    final int INTERNAL_ENC_CASE_FOLD_MULTI_CHAR = (1<<30);
    final int ENC_CASE_FOLD_MIN = INTERNAL_ENC_CASE_FOLD_MULTI_CHAR;
    final int ENC_CASE_FOLD_DEFAULT = ENC_CASE_FOLD_MIN;
    
    /* work size */
    final int ENC_CODE_TO_MBC_MAXLEN            = 7;
    final int ENC_MBC_CASE_FOLD_MAXLEN          = 18;

    final int ENC_MAX_COMP_CASE_FOLD_CODE_LEN   = 3;
    final int ENC_GET_CASE_FOLD_CODES_MAX_NUM   = 13;       /* 13 => Unicode:0x1ffc */

    final boolean USE_UNICODE_CASE_FOLD_TURKISH_AZERI = false;
    final boolean USE_UNICODE_ALL_LINE_TERMINATORS = false;
    final boolean USE_CRNL_AS_LINE_TERMINATOR = false;

    final boolean USE_UNICODE_PROPERTIES = true;
}
