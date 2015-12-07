package com.moilioncircle.mysql.tokenizer;

import java.util.ArrayList;
import java.util.List;

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
 * @author leon on 15-12-6
 */
public class MysqlScanner {
    protected Token token;
    protected Token previousToken;
    private MysqlTokenizer tokenizer;
    public List<Token> tokens = new ArrayList<>();

    public MysqlScanner(char[] chars) {
        tokenizer = new MysqlTokenizer(chars);
        Token token = null;
        while ((token = tokenizer.nextToken()).tag != TokenTag.EOF) {
            tokens.add(token);
        }
        tokens.add(token);
        this.token = tokens.get(0);
    }

    public Token token() {
        token = tokens.get(0);
        return token;
    }

    public boolean hasNext() {
        return tokens.size() >= 0;
    }

    public Token previous() {
        return previousToken;
    }

    public void next() {
        previousToken = token;
        if (tokens.size() > 0) {
            tokens.remove(0);
            token = tokens.get(0);
        }
    }

    public void back(Token token) {
        tokens.add(0, token);
    }

    public boolean lookahead(TokenTag... tags) {
        for (int i = 0; i < tags.length; i++) {
            if ((i + 1) >= tokens.size() || tokens.get(i + 1).tag != tags[i]) {
                return false;
            }
        }
        return true;
    }

}
