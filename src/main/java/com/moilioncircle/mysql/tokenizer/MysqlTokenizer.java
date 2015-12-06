package com.moilioncircle.mysql.tokenizer;

import java.util.Optional;

/**
 * Copyright leon
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author leon on 15-12-5
 */
public class MysqlTokenizer {

    private final char[] chars;

    private int index;

    private int column;

    private int row;

    private TokenTag tag;

    private StringBuilder value = new StringBuilder();

    public MysqlTokenizer(char[] chars) {
        this.chars = chars;
    }

    public Token nextToken() {
        value.setLength(0);
        loop:
        while (true) {
            char c = current();
            switch (c) {
                case 'a':
                case 'b':
                    if (lookahead('\'')) {
                        scanBit();
                        tag = TokenTag.BIT;
                        break loop;
                    }
                case 'c':
                case 'd':
                case 'e':
                case 'f':
                case 'g':
                case 'h':
                case 'i':
                case 'j':
                case 'k':
                case 'l':
                case 'm':
                case 'n':
                case 'o':
                case 'p':
                case 'q':
                case 'r':
                case 's':
                case 't':
                case 'u':
                case 'v':
                case 'w':
                case 'x':
                    if (lookahead('\'')) {
                        scanHex();
                        tag = TokenTag.HEX;
                        break loop;
                    }
                case 'y':
                case 'z':
                case 'A':
                case 'B':
                    if (lookahead('\'')) {
                        scanBit();
                        tag = TokenTag.BIT;
                        break loop;
                    }
                case 'C':
                case 'D':
                case 'E':
                case 'F':
                case 'G':
                case 'H':
                case 'I':
                case 'J':
                case 'K':
                case 'L':
                case 'M':
                case 'N':
                case 'O':
                case 'P':
                case 'Q':
                case 'R':
                case 'S':
                case 'T':
                case 'U':
                case 'V':
                case 'W':
                case 'X':
                    if (lookahead('\'')) {
                        scanHex();
                        tag = TokenTag.HEX;
                        break loop;
                    }
                case 'Y':
                case 'Z':
                    scanIdent();
                    break loop;
                case '`':
                    scanEscapedIdent();
                    tag = TokenTag.IDENT;
                    break loop;
                case '\'':
                    scanString(current());
                    tag = TokenTag.STRING;
                    break loop;
                case '"':
                    scanString(current());
                    tag = TokenTag.STRING;
                    break loop;
                case '+':
                case '-':
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9':
                case '.':
                    scanNumber();
                    tag = TokenTag.NUMBER;
                    break loop;
                case '\n':
                case '\b':
                case '\r':
                case '\t':
                case '\f':
                    continue;
            }

        }
        return new Token(tag, value.toString());
    }

    private void scanEscapedIdent() {
        while (next() != '`') {
            if (current() == 0x1a) {
                reportLexerError("Un-close ident.");
            } else {
                value.append(current());
            }

        }

    }

    private void scanBit() {
        next(); // '\''
        while (next() != '\'') {
            if (current() == '0' || current() == '1') {
                value.append(current());
            } else {
                reportLexerError("invalid bit number.");
            }
        }
    }

    private void reportLexerError(String message) {
        throw new RuntimeException(message);
    }

    private void scanHex() {
        next(); // '\''
        while (next() != '\'') {
            if ((current() >= '0' && current() <= '9') || (current() >= 'a' && current() <= 'f') || (current() >= 'A' && current() <= 'F')) {
                value.append(current());
            } else {
                reportLexerError("invalid hex number.");
            }
        }
    }

    private void scanIdent() {
        loop:
        while (true) {
            switch (current()) {
                case 0x1a:
                    break loop;
                case '.':
                case '\n':
                case '\b':
                case '\r':
                case '\t':
                case '\f':
                    break loop;
                default:
                    value.append(current());
                    break;
            }
            next();
        }
        Optional<TokenTag> tagOpt = lookupKeywords(value.toString());
        if (tagOpt.isPresent()) {
            tag = tagOpt.get();
        } else {
            tag = TokenTag.IDENT;
        }
    }

    private Optional<TokenTag> lookupKeywords(String str) {
        TokenTag[] tags = TokenTag.values();
        for (TokenTag tag : tags) {
            if (tag.type == TokenType.KEYWORDS && str.equalsIgnoreCase(tag.tagName)) {
                return Optional.of(tag);
            }
        }
        return Optional.empty();
    }

    private void scanString(char quoteChar) {
        next();// '"'
        while (true) {
            if (current() == quoteChar) {
                if (lookahead(quoteChar)) {
                    next();
                    value.append(current());
                    next();
                } else {
                    break;
                }
            } else if (current() == 0x1a) {
                reportLexerError("un-close String.");
                break;
            } else if (current() == '\\') {
                if (lookahead('\\') || lookahead('\'') || lookahead('"') || lookahead('b') || lookahead('n') || lookahead('r') || lookahead('t') || lookahead('Z') || lookahead('0') || lookahead('_') || lookahead('%')) {
                    next();
                    switch (current()) {
                        case '\\':
                            value.append('\\');
                            break;
                        case '\'':
                            value.append('\'');
                            break;
                        case 'b':
                            value.append('\b');
                            break;
                        case 'n':
                            value.append('\n');
                            break;
                        case 'r':
                            value.append('\r');
                            break;
                        case 't':
                            value.append('\t');
                            break;
                        case 'Z':
                            value.append((char) 0x1a);
                            break;
                        case '0':
                            value.append("");
                            break;
                        case '_':
                            value.append("\\_");
                            break;
                        case '%':
                            value.append("\\%");
                            break;
                    }
                    next();
                } else {
                    next();
                    value.append(current());
                    next();
                }
            } else {
                value.append(current());
                next();
            }

        }
    }

    private void scanNumber() {
        while (true) {
            switch (current()) {

            }
            next();
        }
    }

    private char next() {
        index++;
        if (index < chars.length) {
            return chars[index];
        }
        return 0x1a;
    }

    private char current() {
        if (index < chars.length) {
            return chars[index];
        }
        return 0x1a;
    }

    private boolean lookahead(char... aheads) {
        int j = index;
        for (int i = 0; i < aheads.length; i++) {
            j++;
            if (j >= chars.length || aheads[i] != chars[j]) {
                return false;
            }

        }
        return true;
    }

    public static void main(String[] args) {
        String bit = "Null";
        MysqlTokenizer tokenizer = new MysqlTokenizer(bit.toCharArray());
        Token token = tokenizer.nextToken();
        System.out.println(token);
    }
}
