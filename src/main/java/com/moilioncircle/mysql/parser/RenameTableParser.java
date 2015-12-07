package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.ast.RenameTable;
import com.moilioncircle.mysql.tokenizer.MysqlScanner;

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
 * @author leon on 15-12-7
 */
public class RenameTableParser extends AbstractParser {

    public RenameTableParser(MysqlScanner scanner) {
        super(scanner);
    }

    public RenameTable parseRenameTable() {
        RenameTable result = new RenameTable();
        accept(RENAME);
        accept(TABLE);
        do {
            if (token().tag == IDENT) {
                String tableName = token().value;
                next();
                accept(TO);
                if (token().tag == IDENT) {
                    String newTableName = token().value;
                    next();
                    result.addPair(tableName, newTableName);
                } else {
                    reportSyntaxError("Expected IDENT but " + token().tag);
                }
            } else {
                reportSyntaxError("Expected IDENT but " + token().tag);
            }
        } while (tokenIs(COMMA));
        accept(EOF);
        return result;
    }

}
