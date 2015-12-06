package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.ast.DropTable;
import com.moilioncircle.mysql.tokenizer.MysqlScanner;

import java.util.ArrayList;
import java.util.List;

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
 * @author leon on 15-12-6
 */
public class DropTableParser extends MysqlScanner {

    public DropTableParser(char[] chars) {
        super(chars);
    }

    public DropTable parseDropTable() {
        boolean isTemporary = false;
        boolean ifExists = false;
        boolean restrict = false;
        boolean cascade = false;
        List<String> tables = new ArrayList<>();

        accept(DROP);
        if (token.tag == TEMPORARY) {
            accept(TEMPORARY);
            isTemporary = true;
        }
        accept(TABLE);
        if (token.tag == IF) {
            accept(IF);
            accept(EXISTS);
            ifExists = true;
        }
        if (token.tag == IDENT) {
            do {
                tables.add(token.value);
                next();
            } while (tokenIs(COMMA));
        } else {
            reportSyntaxError("");
        }
        switch (token.tag) {
            case RESTRICT:
                accept(RESTRICT);
                restrict = true;
                break;
            case CASCADE:
                accept(CASCADE);
                cascade = true;
                break;
        }
        accept(EOF);
        return new DropTable(isTemporary, ifExists, restrict, cascade, tables);
    }

    public static void main(String[] args) {
        String str = "drop Temporary table if exists abc RESTRICT";
        MysqlScanner scanner = new MysqlScanner(str.toCharArray());
        System.out.println(scanner.tokens);
        DropTableParser parser = new DropTableParser(str.toCharArray());
        DropTable table = parser.parseDropTable();
        System.out.println(table);
    }
}
