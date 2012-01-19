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
package org.jcodings.unicode;

import org.jcodings.util.IntArrayHash;
import org.jcodings.util.IntHash;

public class UnicodeCaseFolds {
%{body}
    private static IntHash<int[]> initializeFoldHash() {
        IntHash<int[]> fold = new IntHash<int[]>(1200);
        for (int i=0; i<CaseFold_From.length; i++)
            fold.putDirect(CaseFold_From[i], CaseFold_To[i]);
        for (int i=0; i<CaseFold_Locale_From.length; i++) 
            fold.putDirect(CaseFold_Locale_From[i], CaseFold_Locale_To[i]);
        return fold;
    }

    private static IntHash<int[]> initializeUnfold1Hash() {
        IntHash<int[]> unfold1 = new IntHash<int[]>(1000);
        for (int i=0; i<CaseUnfold_11_From.length; i++) 
            unfold1.putDirect(CaseUnfold_11_From[i], CaseUnfold_11_To[i]);
        for (int i=0; i<CaseUnfold_11_Locale_From.length; i++) 
            unfold1.putDirect(CaseUnfold_11_Locale_From[i], CaseUnfold_11_Locale_To[i]);
        return unfold1;
    }

    private static IntArrayHash<int[]> initializeUnfold2Hash() {
        IntArrayHash<int[]> unfold2 = new IntArrayHash<int[]>(200);
        for (int i=0; i<CaseUnfold_12.length; i+=2)
            unfold2.putDirect(CaseUnfold_12[i], CaseUnfold_12[i + 1]);
        for (int i=0; i<CaseUnfold_12_Locale.length; i+=2)
            unfold2.putDirect(CaseUnfold_12_Locale[i], CaseUnfold_12_Locale[i + 1]);
        return unfold2;
    }

    private static IntArrayHash<int[]> initializeUnfold3Hash() {
        IntArrayHash<int[]> unfold3 = new IntArrayHash<int[]>(30);
        for (int i=0; i<CaseUnfold_13.length; i+=2)
            unfold3.putDirect(CaseUnfold_13[i], CaseUnfold_13[i + 1]);
        return unfold3;
    }

    static final IntHash<int[]> FoldHash = initializeFoldHash();
    static final IntHash<int[]> Unfold1Hash = initializeUnfold1Hash();
    static final IntArrayHash<int[]> Unfold2Hash = initializeUnfold2Hash();
    static final IntArrayHash<int[]> Unfold3Hash = initializeUnfold3Hash();    
}
