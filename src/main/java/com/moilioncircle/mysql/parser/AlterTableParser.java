package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.ast.AlterTable;
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
public class AlterTableParser extends AbstractParser {
    private final CreateTableParser createTableParser;

    public AlterTableParser(MysqlScanner scanner) {
        super(scanner);
        createTableParser = new CreateTableParser(scanner);
    }

    public AlterTable parseAlterTable() {
        accept(ALTER);
        if (token().tag == ONLINE) {
            accept(ONLINE);
        } else if (token().tag == OFFLINE) {
            accept(OFFLINE);
        }
        acceptIf(IGNORE);
        accept(TABLE);

        loop:
        while (true) {
            switch (token().tag) {
                case PARTITION:
                    createTableParser.parsePartitionOptions();
                    accept(EOF);
                    break loop;
                case EOF:
                    accept(EOF);
                    break loop;
                default:
                    do {
                        parseAlterSpecification();
                    } while (tokenIs(COMMA));
                    continue loop;
            }
        }
        return null;
    }

    private void parseAlterSpecification() {
        switch (token().tag) {
            case ADD:
                parseAddStatment();
                break;
            //TODO
//            | table_options
//            | ALGORITHM [=] {DEFAULT|INPLACE|COPY}
//            | ALTER [COLUMN] col_name {SET DEFAULT literal | DROP DEFAULT}
//            | CHANGE [COLUMN] old_col_name new_col_name column_definition
//                    [FIRST|AFTER col_name]
//            | LOCK [=] {DEFAULT|NONE|SHARED|EXCLUSIVE}
//            | MODIFY [COLUMN] col_name column_definition
//            [FIRST | AFTER col_name]
//            | DROP [COLUMN] col_name
//                    | DROP PRIMARY KEY
//            | DROP {INDEX|KEY} index_name
//                    | DROP FOREIGN KEY fk_symbol
//                    | DISABLE KEYS
//                    | ENABLE KEYS
//                    | RENAME [TO|AS] new_tbl_name
//                    | ORDER BY col_name [, col_name] ...
//            | CONVERT TO CHARACTER SET charset_name [COLLATE collation_name]
//            | [DEFAULT] CHARACTER SET [=] charset_name [COLLATE [=] collation_name]
//            | DISCARD TABLESPACE
//            | IMPORT TABLESPACE
//            | FORCE
//                    | ADD PARTITION (partition_definition)
//                    | DROP PARTITION partition_names
//            | TRUNCATE PARTITION {partition_names | ALL}
//            | COALESCE PARTITION number
//                    | REORGANIZE PARTITION partition_names INTO (partition_definitions)
//                    | EXCHANGE PARTITION partition_name WITH TABLE tbl_name
//                    | ANALYZE PARTITION {partition_names | ALL}
//            | CHECK PARTITION {partition_names | ALL}
//            | OPTIMIZE PARTITION {partition_names | ALL}
//            | REBUILD PARTITION {partition_names | ALL}
//            | REPAIR PARTITION {partition_names | ALL}
//            | REMOVE PARTITIONING

        }
    }

    private void parseAddStatment() {
        accept(ADD);
        loop:
        while (true) {
            switch (token().tag) {
                case COLUMN:
                    accept(COLUMN);
                    if (lookahead(IDENT, LPAREN)) {
                        continue loop;
                    } else {
                        next();
                        reportSyntaxError("Expected [IDENT,LPAREN] but " + token().tag);
                    }
                case CONSTRAINT:
                    accept(CONSTRAINT);
                    if (token().tag == IDENT) {
                        accept(IDENT);
                    }
                    if (lookahead1(PRIMARY, UNIQUE, FOREIGN)) {
                        continue loop;
                    } else {
                        next();
                        reportSyntaxError("Expected [PRIMARY,UNIQUE,FOREIGN] but " + token().tag);
                    }
                case FULLTEXT:
                case SPATIAL:
                    //{FULLTEXT|SPATIAL}
                    if (token().tag == FULLTEXT) {
                        accept(FULLTEXT);
                    } else {
                        accept(SPATIAL);
                    }
                    //[INDEX|KEY]
                    if (token().tag == INDEX) {
                        accept(INDEX);
                    } else if (token().tag == KEY) {
                        accept(KEY);
                    }
                    //[index_name]
                    String indexName = acceptIf(IDENT).value;
                    //(index_col_name,...)
                    accept(LPAREN);
                    do {
                        parseIndexColumnName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    //[index_option]
                    parseIndexOption();
                    break loop;
                case INDEX:
                case KEY:
                    //{INDEX|KEY}
                    if (token().tag == INDEX) {
                        accept(INDEX);
                    } else {
                        accept(KEY);
                    }
                    //[index_name]
                    indexName = acceptIf(IDENT).value;
                    //[index_type]
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    //(index_col_name,...)
                    accept(LPAREN);
                    do {
                        parseIndexColumnName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    //[index_option]
                    parseIndexOption();
                    break loop;
                case LPAREN:
                    accept(LPAREN);
                    do {
                        String colName = accept(IDENT).value;
                        createTableParser.parseColumnDefinition();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    break loop;
                case IDENT:
                    //col_name
                    String colName = accept(IDENT).value;
                    //column_definition
                    createTableParser.parseColumnDefinition();
                    //[FIRST | AFTER col_name ]
                    if (token().tag == FIRST) {
                        accept(FIRST);
                    } else if (token().tag == AFTER) {
                        accept(AFTER);
                        String suffixColName = accept(IDENT).value;
                    }
                    break loop;
                case PRIMARY:
                    //PRIMARY KEY
                    acceptN(PRIMARY, KEY);
                    //[index_type]
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    //(index_col_name,...)
                    accept(LPAREN);
                    do {
                        parseIndexColumnName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    parseIndexOption();
                    break loop;
                case UNIQUE:
                    accept(UNIQUE);
                    //[INDEX|KEY]
                    if (token().tag == INDEX) {
                        accept(INDEX);
                    } else if (token().tag == KEY) {
                        accept(KEY);
                    }
                    //[index_name]
                    indexName = acceptIf(IDENT).value;
                    //[index_type]
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    //(index_col_name,...)
                    accept(LPAREN);
                    do {
                        parseIndexColumnName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    parseIndexOption();
                    break loop;
                case FOREIGN:
                    acceptN(FOREIGN, KEY);
                    //[index_name]
                    indexName = acceptIf(IDENT).value;
                    //(index_col_name,...)
                    accept(LPAREN);
                    do {
                        parseIndexColumnName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    createTableParser.parseReferenceDefinition();
                    break loop;
                default:
                    reportSyntaxError("Eexcpted but " + token().tag);
            }
        }

    }

    private void parseIndexColumnName() {
        String colName = accept(IDENT).value;
        if (token().tag == LPAREN) {
            accept(LPAREN);
            String length = accept(NUMBER).value;
            accept(RPAREN);
        }
        if (token().tag == ASC) {
            accept(ASC);
        } else if (token().tag == DESC) {
            accept(DESC);
        }
    }

    private void parseIndexOption() {
        switch (token().tag) {
            case KEY_BLOCK_SIZE:
                accept(KEY_BLOCK_SIZE);
                if (token().tag == EQUAL) {
                    accept(EQUAL);
                }
                accept(NUMBER);
                break;
            case USING:
                parseIndexType();
                break;
            case WITH:
                acceptN(WITH, PARSER);
                String parserName = accept(IDENT).value;
                break;
            case COMMENT:
                accept(COMMENT);
                String str = accept(STRING).value;
                break;
        }
    }

    private void parseIndexType() {
        accept(USING);
        if (token().tag == BTREE) {
            accept(BTREE);
        } else {
            accept(HASH);
        }
    }

}
