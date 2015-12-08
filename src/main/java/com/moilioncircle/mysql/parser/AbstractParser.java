package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import com.moilioncircle.mysql.tokenizer.Token;
import com.moilioncircle.mysql.tokenizer.TokenTag;

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
 * @author leon on 15-12-7
 */
public abstract class AbstractParser {

    private final MysqlScanner scanner;

    public AbstractParser(MysqlScanner scanner) {
        this.scanner = scanner;
    }

    public Token accept(TokenTag tag) {
        if (token().tag == tag) {
            if (token().tag != TokenTag.EOF) {
                Token temp = token();
                next();
                return temp;
            }
        } else {
            reportSyntaxError("Expected " + tag + " but " + token().tag);
        }
        return null;
    }

    public boolean tokenIs(TokenTag tag) {
        if (token().tag == tag) {
            next();
            return true;
        } else {
            return false;
        }
    }

    protected void reportSyntaxError(String message) {
        throw new RuntimeException(message);
    }

    public boolean lookahead(TokenTag... tags) {
        return scanner.lookahead(tags);
    }

    public boolean lookahead1(TokenTag... tags) {
        return scanner.lookahead1(tags);
    }

    public Token token() {
        return scanner.token();
    }

    public boolean hasNext() {
        return scanner.hasNext();
    }

    public Token previous() {
        return scanner.previous();
    }

    public void next() {
        scanner.next();
    }

    public void back(Token token) {
        scanner.back(token);
    }

    public void acceptN(TokenTag... tags) {
        for (TokenTag tag : tags) {
            accept(tag);
        }
    }

    public Token acceptIf(TokenTag tag) {
        if (token().tag == tag) {
            return accept(tag);
        }
        return null;
    }
}
