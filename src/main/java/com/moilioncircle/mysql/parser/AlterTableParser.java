package com.moilioncircle.mysql.parser;

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
 * @author leon on 15-12-7
 */
public class AlterTableParser extends AbstractParser {
    private final CreateTableParser createTableParser;

    public AlterTableParser(MysqlScanner scanner) {
        super(scanner);
        createTableParser = new CreateTableParser(scanner);
    }

    public void parseAlterTable() {
        accept(ALTER);
        if (token().tag == ONLINE) {
            accept(ONLINE);
        } else if (token().tag == OFFLINE) {
            accept(OFFLINE);
        }
        acceptIf(IGNORE);
        accept(TABLE);
        String tableName = accept(IDENT).value;
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
    }

    private void parseAlterSpecification() {
        switch (token().tag) {
            case ADD:
                parseAddStatement();
                break;
            case ALGORITHM:
                accept(ALGORITHM);
                acceptIf(EQUAL);
                acceptOr(DEFAULT, INPLACE, COPY);
                break;
            case ALTER:
                accept(ALTER);
                acceptIf(COLUMN);
                String colName = accept(IDENT).value;
                if (token().tag == SET) {
                    acceptN(SET, DEFAULT);
                    String literal = accept(STRING).value;
                } else {
                    acceptN(DROP, DEFAULT);
                }
                break;
            case CHANGE:
                accept(CHANGE);
                acceptIf(COLUMN);
                String oldColumnName = accept(IDENT).value;
                String newColumnName = accept(IDENT).value;
                createTableParser.parseColumnDefinition();
                if (token().tag == FIRST) {
                    accept(FIRST);
                } else if (token().tag == AFTER) {
                    accept(AFTER);
                    colName = accept(IDENT).value;
                }
                break;
            case LOCK:
                //LOCK [=] {DEFAULT|NONE|SHARED|EXCLUSIVE}
                accept(LOCK);
                acceptIf(EQUAL);
                acceptOr(DEFAULT, NONE, SHARED, EXCLUSIVE);
                break;
            case MODIFY:
                //MODIFY [COLUMN] col_name column_definition [FIRST | AFTER col_name]
                accept(MODIFY);
                acceptIf(COLUMN);
                String modifyColName = accept(IDENT).value;
                createTableParser.parseColumnDefinition();
                if (token().tag == FIRST) {
                    accept(FIRST);
                } else if (token().tag == AFTER) {
                    accept(AFTER);
                    colName = accept(IDENT).value;
                }
                break;
            case DROP:
                accept(DROP);
                switch (token().tag) {
                    case PRIMARY:
                        acceptN(PRIMARY, KEY);
                        break;
                    case INDEX:
                        accept(INDEX);
                        String indexName = accept(IDENT).value;
                        break;
                    case KEY:
                        accept(KEY);
                        indexName = accept(IDENT).value;
                        break;
                    case FOREIGN:
                        acceptN(FOREIGN, KEY);
                        String fkSymbol = accept(IDENT).value;
                        break;
                    case PARTITION:
                        accept(PARTITION);
                        String partitionNames = accept(IDENT).value;
                        break;
                    case COLUMN:
                        accept(COLUMN);
                        colName = accept(IDENT).value;
                        break;
                    case IDENT:
                        colName = accept(IDENT).value;
                        break;
                    default:
                        reportSyntaxError("Expected but " + token().tag);
                }
                break;
            case DISABLE:
                acceptN(DISABLE, KEYS);
                break;
            case ENABLE:
                acceptN(ENABLE, KEYS);
                break;
            case RENAME:
                accept(RENAME);
                //{INDEX|KEY} old_index_name TO new_index_name
                //[TO|AS] new_tbl_name
                if (token().tag == INDEX || token().tag == KEY) {
                    acceptOr(INDEX,KEY);
                    String oldIndexName = accept(IDENT).value;
                    accept(TO);
                    String newIndexName = accept(IDENT).value;
                } else {
                    if (token().tag == TO) {
                        accept(TO);
                    } else if (token().tag == AS) {
                        accept(AS);
                    }
                    String newTableName = accept(IDENT).value;
                }


                break;
            case ORDER:
                //ORDER BY col_name [, col_name] ...
                acceptN(ORDER, BY);
                List<String> orderBy = new ArrayList<>();
                do {
                    orderBy.add(accept(IDENT).value);
                } while (tokenIs(COMMA));
                break;
            case CONVERT:
                acceptN(CONVERT, TO, CHARACTER, SET);
                String charSet = accept(IDENT).value;
                break;
            case DEFAULT:
                //[DEFAULT] CHARACTER SET [=] charset_name
                acceptN(DEFAULT, CHARACTER, SET);
                acceptIf(EQUAL);
                String charsetName = accept(STRING).value;
                //[COLLATE [=] collation_name]
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    acceptIf(EQUAL);
                    String collatioName = accept(IDENT).value;
                }
                break;
            case CHARACTER:
                //CHARACTER SET [=] charset_name
                acceptN(CHARACTER, SET);
                acceptIf(EQUAL);
                charsetName = accept(STRING).value;
                //[COLLATE [=] collation_name]
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    acceptIf(EQUAL);
                    String collatioName = accept(IDENT).value;
                }
                break;
            case DISCARD:
                acceptN(DISCARD, TABLESPACE);
                break;
            case IMPORT:
                acceptN(IMPORT, TABLESPACE);
                break;
            case FORCE:
                accept(FORCE);
                break;
            case TRUNCATE:
                acceptN(TRUNCATE, PARTITION);
                //{partition_names | ALL}
                if (token().tag == IDENT) {
                    String partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case COALESCE:
                acceptN(COALESCE, PARTITION);
                String number = accept(NUMBER).value;
            case REORGANIZE:
                acceptN(REORGANIZE, PARTITION);
                String partitionNames = accept(IDENT).value;
                accept(INTO);
                accept(LPAREN);
                createTableParser.parsePartitionDefinition();
                accept(RPAREN);
                break;
            case EXCHANGE:
                acceptN(EXCHANGE, PARTITION);
                partitionNames = accept(IDENT).value;
                acceptN(WITH, TABLE);
                String tableName = accept(IDENT).value;
                break;
            case ANALYZE:
                acceptN(ANALYZE, PARTITION);
                if (token().tag == IDENT) {
                    partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case CHECK:
                acceptN(CHECK, PARTITION);
                if (token().tag == IDENT) {
                    partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case OPTIMIZE:
                acceptN(OPTIMIZE, PARTITION);
                if (token().tag == IDENT) {
                    partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case REBUILD:
                acceptN(REBUILD, PARTITION);
                if (token().tag == IDENT) {
                    partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case REPAIR:
                acceptN(REPAIR, PARTITION);
                if (token().tag == IDENT) {
                    partitionNames = accept(IDENT).value;
                } else {
                    accept(ALL);
                }
                break;
            case REMOVE:
                acceptN(REMOVE, PARTITIONING);
                break;
            default:
                createTableParser.parseTableOption();
        }
    }

    private void parseAddStatement() {
        accept(ADD);
        loop:
        while (true) {
            switch (token().tag) {
                case COLUMN:
                    accept(COLUMN);
                    if (token().tag == IDENT || token().tag == LPAREN) {
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
                    if (token().tag == PRIMARY || token().tag == UNIQUE || token().tag == FOREIGN) {
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
                case PARTITION:
                    //PARTITION
                    accept(PARTITION);
                    //(partition_definition)
                    accept(LPAREN);
                    createTableParser.parsePartitionOptions();
                    accept(RPAREN);
                default:
                    reportSyntaxError("Excepted but " + token().tag);
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

    public static void main(String[] args) {
        String sql = "alter table abc DEFAULT CHARACTER SET = 'charset_name' COLLATE = collation_name," +
                "DEFAULT CHARACTER SET = 'utf8' COLLATE = aaaa," +
                "DROP index_name," +
                "RENAME KEY old_index_name TO new_index_name," +
                "RENAME new_table," +
                "DROP FOREIGN KEY fk_symbol";
        MysqlScanner scanner = new MysqlScanner(sql.toCharArray());
        AlterTableParser parser = new AlterTableParser(scanner);
        parser.parseAlterTable();
    }

}
