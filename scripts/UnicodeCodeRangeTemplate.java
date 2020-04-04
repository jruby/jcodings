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

import org.jcodings.util.ArrayReader;
import org.jcodings.CodeRange;

public enum UnicodeCodeRange {
%{extcrs};

    private final String table;
    final byte[]name;
    private int[]range;

    private UnicodeCodeRange(String name, String table) {
        this.table = table;
        this.name = name.getBytes();
    }

    int[]getRange() {
        if (range == null) range = ArrayReader.readIntArray(table);
        return range;
    }

    public boolean contains(int code) {
        return CodeRange.isInCodeRange(range, code);
    }

    static final UnicodeCodeRange[]CodeRangeTable = UnicodeCodeRange.values();
    static final int MAX_WORD_LENGTH = %{max_length};
}
