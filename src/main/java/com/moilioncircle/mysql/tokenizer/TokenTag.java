package com.moilioncircle.mysql.tokenizer;

import static com.moilioncircle.mysql.tokenizer.TokenType.DEFAULT;
import static com.moilioncircle.mysql.tokenizer.TokenType.KEYWORDS;

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
public enum TokenTag {
    HEX("HEX", DEFAULT), BIT("BIT", DEFAULT), IDENT("IDENT", DEFAULT), STRING("STRING", DEFAULT), TRUE("TRUE", KEYWORDS), FALSE("FALSE", KEYWORDS), NULL("NULL", KEYWORDS), NUMBER("NUMBER", KEYWORDS),
    DOT("DOT", KEYWORDS), PLUS("PLUS", KEYWORDS), MINUS("MINUS", KEYWORDS), EOF("EOF", DEFAULT),
    //DROP TABLE
    DROP("DROP", KEYWORDS), TEMPORARY("TEMPORARY", KEYWORDS), TABLE("TABLE", KEYWORDS), IF("IF", KEYWORDS),
    EXISTS("EXISTS", KEYWORDS), RESTRICT("RESTRICT", KEYWORDS), CASCADE("CASCADE", KEYWORDS),
    COMMA("COMMA", KEYWORDS);
    public String tagName;
    public TokenType type;

    TokenTag(String tagName, TokenType type) {
        this.tagName = tagName;
        this.type = type;
    }
}
