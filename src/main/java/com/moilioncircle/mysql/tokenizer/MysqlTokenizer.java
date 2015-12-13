package com.moilioncircle.mysql.tokenizer;

import java.util.Optional;

import static com.moilioncircle.mysql.tokenizer.TokenTag.*;

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

    private Pos startPos;

    private StringBuilder value = new StringBuilder();

    public MysqlTokenizer(char[] chars) {
        this.chars = chars;
    }

    public Token nextToken() {
        value.setLength(0);
        loop:
        while (true) {
            char c = current();
            startPos = newPos();
            switch (c) {
                case 'a':
                case 'b':
                    if (lookahead('\'')) {
                        scanBit();
                        tag = TokenTag.BIT_VALUE;
                        next();
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
                        tag = TokenTag.HEX_VALUE;
                        next();
                        break loop;
                    }
                case 'y':
                case 'z':
                case 'A':
                case 'B':
                    if (lookahead('\'')) {
                        scanBit();
                        tag = TokenTag.BIT_VALUE;
                        next();
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
                        tag = TokenTag.HEX_VALUE;
                        next();
                        break loop;
                    }
                case 'Y':
                case 'Z':
                case '$':
                case '_':
                    scanIdent();
                    Optional<TokenTag> tagOpt = lookupKeywords(value.toString());
                    if (tagOpt.isPresent()) {
                        tag = tagOpt.get();
                    } else {
                        tag = TokenTag.IDENT;
                    }
                    break loop;
                case '`':
                    scanEscapedIdent();
                    tag = TokenTag.IDENT;
                    next();
                    break loop;
                case '\'':
                    scanString(current());
                    tag = TokenTag.STRING;
                    next();
                    break loop;
                case '"':
                    scanString(current());
                    tag = TokenTag.STRING;
                    next();
                    break loop;
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
                    scanNumber();
                    tag = TokenTag.NUMBER;
                    break loop;
                case '.':
                    if (lookahead1('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
                        scanFractionNumber();
                        tag = TokenTag.NUMBER;
                    } else {
                        operator(DOT);
                    }
                    break loop;
                case ',':
                    operator(COMMA);
                    break loop;
                case '+':
                    operator(PLUS);
                    break loop;
                case '-':
                    operator(MINUS);
                    break loop;
                case '=':
                    operator(EQUAL);
                    break loop;
                case '(':
                    operator(LPAREN);
                    break loop;
                case ')':
                    operator(RPAREN);
                    break loop;
                case '!':
                    if (lookahead('=')) {
                        tag = TokenTag.NOT_EQ;
                        value.append("!=");
                        next();
                        next();
                    } else {
                        operator(NOT_OPERATOR);
                    }
                    break loop;
                case '~':
                    operator(UNARY_BIT);
                    break loop;
                case '^':
                    operator(EOR);
                    break loop;
                case '*':
                    operator(STAR);
                    break loop;
                case '/':
                    operator(SLASH);
                    break loop;
                case '%':
                    operator(REMAINDER);
                    break loop;
                case '?':
                    operator(PARAM_MARKER);
                    break loop;
                case '<':
                    if (lookahead('<')) {
                        tag = TokenTag.LTLT;
                        value.append("<<");
                        next();
                        next();
                    } else if (lookahead('=', '>')) {
                        tag = TokenTag.LTEQGT;
                        value.append("<=>");
                        next();
                        next();
                        next();
                    } else if (lookahead('=')) {
                        tag = TokenTag.LE;
                        value.append("<=");
                        next();
                        next();
                    } else if (lookahead('>')) {
                        tag = TokenTag.LTGT;
                        value.append("<>");
                        next();
                        next();
                    } else {
                        tag = TokenTag.LT;
                        value.append("<");
                        next();
                    }
                    break loop;
                case '>':
                    if (lookahead('>')) {
                        tag = TokenTag.GTGT;
                        value.append(">>");
                        next();
                        next();
                    } else if (lookahead('=')) {
                        tag = TokenTag.GE;
                        value.append(">=");
                        next();
                        next();
                    } else {
                        tag = TokenTag.GT;
                        value.append(">");
                        next();
                    }
                    break loop;
                case '&':
                    if (lookahead('&')) {
                        tag = TokenTag.BABA;
                        value.append("&&");
                        next();
                        next();
                    } else {
                        tag = TokenTag.BIT_AND;
                        value.append("&");
                        next();
                    }
                    break loop;
                case '|':
                    if (lookahead('|')) {
                        tag = TokenTag.BOBO;
                        value.append("||");
                        next();
                        next();
                    } else {
                        tag = TokenTag.BIT_OR;
                        value.append("|");
                        next();
                    }
                    break loop;
                case ':':
                    next();
                    if (current() == '=') {
                        tag = TokenTag.COLONASSIGN;
                        value.append(":=");
                        next();
                    } else {
                        reportLexerError("Expected '=' but " + current());
                    }
                    break loop;
                case '\n':
                case '\b':
                case '\r':
                case '\t':
                case '\f':
                case ' ':
                    next();
                    continue;
                case 0x1a:
                    tag = TokenTag.EOF;
                    break loop;

            }

        }
        return new Token(tag, value.toString(), startPos, newPos());
    }

    private void operator(TokenTag tokenTag) {
        tag = tokenTag;
        value.append(current());
        next();
    }

    private void scanBit() {
        next(); // '\''
        while (next() != '\'') {
            if (current() == '0' || current() == '1') {
                value.append(current());
            } else {
                reportLexerError("Invalid bit number.");
            }
        }
    }

    private void scanHex() {
        next(); // '\''
        while (next() != '\'') {
            if ((current() >= '0' && current() <= '9') || (current() >= 'a' && current() <= 'f') || (current() >= 'A' && current() <= 'F')) {
                value.append(current());
            } else {
                reportLexerError("Invalid hex number.");
            }
        }
    }

    private void scanIdent() {
        loop:
        while (true) {
            if ((current() >= 'a' && current() <= 'z') || (current() >= 'A' && current() <= 'Z') || (current() >= '0' && current() <= '9') || current() == '_' || current() == '$') {
                value.append(current());
                next();
            } else {
                break;
            }
        }
    }

    private void scanEscapedIdent() {
        while (next() != '`') {
            if (current() == 0x1a) {
                reportLexerError("Un-closed ident.");
            } else {
                value.append(current());
            }
        }
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
                reportLexerError("Un-closed String.");
                break;
            } else if (current() == '\\') {
                if (lookahead1('\\', '\'', '"', 'b', 'n', 'r', 't', 'Z', '0', '_', '%')) {
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
        while (isDigit(current())) {
            value.append(current());
            next();
        }
        switch (current()) {
            case '.':
                if (lookahead1('0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
                    scanFractionNumber();
                    break;
                } else {
                    reportLexerError("Invalid number.");
                }

            case 'E':
            case 'e':
                if (lookahead1('+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
                    scanScientificNumber();
                } else {
                    reportLexerError("Invalid number.");
                }
                break;
        }
    }

    private void scanFractionNumber() {
        value.append(current());
        next();
        while (isDigit(current())) {
            value.append(current());
            next();
        }
        switch (current()) {
            case 'E':
            case 'e':
                if (lookahead1('+', '-', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9')) {
                    scanScientificNumber();
                } else {
                    reportLexerError("Invalid number.");
                }
                break;

        }

    }

    private void scanScientificNumber() {
        value.append(current());
        next();
        if (current() == '+' || current() == '-') {
            value.append(current());
            next();
        }
        if (isDigit(current())) {
            while (isDigit(current())) {
                value.append(current());
                next();
            }
        } else {
            reportLexerError("Invalid number.");
        }

    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private char next() {
        switch (current()) {
            case '\n':
                newLine();
                break;
            default:
                column++;
        }

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

    private boolean lookahead1(char... aheads) {
        for (char ahead : aheads) {
            if (lookahead(ahead)) {
                return true;
            }
        }
        return false;
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

    private void reportLexerError(String message) {
        throw new RuntimeException(message);
    }

    private void newLine() {
        row++;
        column = 0;
    }

    private Pos newPos() {
        return new Pos(row, column);
    }

    public static void main(String[] args) {
        String num = "WHEN THEN CASE ELSE NOT OR AND XOR LIKE IS := || <=>>>|||&&&<<<=< >>>=> !==:= ~^+-*/%";
        MysqlTokenizer tokenizer = new MysqlTokenizer(num.toCharArray());
        Token token = tokenizer.nextToken();
        System.out.println(token);
        while (token.tag != TokenTag.EOF) {
            token = tokenizer.nextToken();
            System.out.println(token);
        }
    }
}
