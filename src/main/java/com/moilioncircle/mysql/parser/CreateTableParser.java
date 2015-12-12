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
 * @author leon on 15-12-8
 */
public class CreateTableParser extends AbstractParser {

    private final ExprParser exprParser;
    private final SelectStatementParser selectStatementParser;

    public CreateTableParser(MysqlScanner scanner) {
        super(scanner);
        exprParser = new ExprParser(scanner);
        selectStatementParser = new SelectStatementParser(scanner);
    }

    public void parseCreateTable() {
        accept(CREATE);
        acceptIf(TEMPORARY);
        accept(TABLE);
        if (token().tag == IF) {
            acceptN(IF, NOT, EXISTS);
        }
        String tableName = accept(IDENT).value;
        boolean existCreateDefinition = false;
        if (token().tag == LPAREN) {
            accept(LPAREN);
            if (token().tag == LIKE) {
                accept(LIKE);
                String oldTableName = accept(IDENT).value;
                accept(RPAREN);
                return;
            } else {
                do {
                    parseCreateDefinition();
                } while (tokenIs(COMMA));
                accept(RPAREN);
                existCreateDefinition = true;
            }
        } else if (token().tag == LIKE) {
            accept(LIKE);
            String oldTableName = accept(IDENT).value;
            return;
        }
        switch (token().tag) {
            case ENGINE:
            case AUTO_INCREMENT:
            case AVG_ROW_LENGTH:
            case DEFAULT:
            case CHARACTER:
            case CHECKSUM:
            case COLLATE:
            case COMMENT:
            case CONNECTION:
            case DATA:
            case DELAY_KEY_WRITE:
            case INDEX:
            case INSERT_METHOD:
            case KEY_BLOCK_SIZE:
            case MAX_ROWS:
            case MIN_ROWS:
            case PACK_KEYS:
            case PASSWORD:
            case ROW_FORMAT:
            case STATS_AUTO_RECALC:
            case STATS_PERSISTENT:
            case STATS_SAMPLE_PAGES:
            case TABLESPACE:
            case UNION:
                parseTableOptions();
                break;
            default:
                break;
        }
        if (token().tag == PARTITION) {
            parsePartitionOptions();
        }
        if (!existCreateDefinition) {
            //[IGNORE | REPLACE] [AS] SELECT
            if (token().tag == IGNORE) {
                accept(IGNORE);
            } else if (token().tag == REPLACE) {
                accept(REPLACE);
            }
            if (token().tag == AS) {
                accept(AS);
            }
            selectStatementParser.parseSelectStatement();
        }
        accept(EOF);
    }

    private void parseCreateDefinition() {
        loop:
        while (true) {
            switch (token().tag) {
                case CONSTRAINT:
                    //[CONSTRAINT [symbol]]
                    accept(CONSTRAINT);
                    if (token().tag == IDENT) {
                        String symbol = accept(IDENT).value;
                    }
                    if (token().tag == PRIMARY || token().tag == UNIQUE || token().tag == FOREIGN) {
                        continue loop;
                    } else {
                        reportSyntaxError("Excepted [PRIMARY|UNIQUE|FOREIGN] but " + token().tag);
                        break loop;
                    }
                case PRIMARY:
                    //PRIMARY KEY [index_type] (index_col_name,...) [index_option] ...
                    acceptN(PRIMARY, KEY);
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    accept(LPAREN);
                    do {
                        parseIndexColName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    switch (token().tag) {
                        case KEY_BLOCK_SIZE:
                        case USING:
                        case WITH:
                        case COMMENT:
                            parseIndexOption();
                    }
                    break loop;
                case UNIQUE:
                    //UNIQUE [INDEX|KEY] [index_name] [index_type] (index_col_name,...)  [index_option] ...
                    accept(UNIQUE);
                    if (token().tag == INDEX) {
                        accept(INDEX);
                    } else if (token().tag == KEY) {
                        accept(KEY);
                    }
                    if (token().tag == IDENT) {
                        String indexName = accept(IDENT).value;
                    }
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    accept(LPAREN);
                    do {
                        parseIndexColName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    switch (token().tag) {
                        case KEY_BLOCK_SIZE:
                        case USING:
                        case WITH:
                        case COMMENT:
                            parseIndexOption();
                    }
                    break loop;
                case FOREIGN:
                    //FOREIGN KEY [index_name] (index_col_name,...) reference_definition
                    acceptN(FOREIGN, KEY);
                    if (token().tag == IDENT) {
                        String indexName = accept(IDENT).value;
                    }
                    accept(LPAREN);
                    do {
                        parseIndexColName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    if (token().tag == REFERENCES) {
                        parseReferenceDefinition();
                    }
                    break loop;
                case IDENT:
                    //col_name column_definition
                    String colName = accept(IDENT).value;
                    parseColumnDefinition();
                    break loop;
                case INDEX:
                case KEY:
                    //{INDEX|KEY} [index_name] [index_type] (index_col_name,...) [index_option] ...
                    acceptOr(INDEX, KEY);
                    if (token().tag == IDENT) {
                        String indexName = accept(IDENT).value;
                    }
                    if (token().tag == USING) {
                        parseIndexType();
                    }
                    accept(LPAREN);
                    do {
                        parseIndexColName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    switch (token().tag) {
                        case KEY_BLOCK_SIZE:
                        case USING:
                        case WITH:
                        case COMMENT:
                            parseIndexOption();
                    }
                    break loop;
                case FULLTEXT:
                case SPATIAL:
                    //{FULLTEXT|SPATIAL} [INDEX|KEY] [index_name] (index_col_name,...) [index_option] ...
                    acceptOr(FULLTEXT, SPATIAL);
                    if (token().tag == IDENT) {
                        String indexName = accept(IDENT).value;
                    }
                    accept(LPAREN);
                    do {
                        parseIndexColName();
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    switch (token().tag) {
                        case KEY_BLOCK_SIZE:
                        case USING:
                        case WITH:
                        case COMMENT:
                            parseIndexOption();
                    }
                    break loop;
                case CHECK:
                    //CHECK (expr)
                    accept(CHECK);
                    accept(LPAREN);
                    exprParser.parseExpr();
                    accept(RPAREN);
                    break loop;
                default:
                    reportSyntaxError("Excepted but" + token().tag);
                    break loop;
            }
        }


    }

    public void parseColumnDefinition() {
        parseDataType();
        switch (token().tag) {
            case NOT:
                acceptN(NOT, NULL);
                break;
            case NULL:
                accept(NULL);
                break;
            case DEFAULT:
                accept(DEFAULT);
                parseDefaultValue();
                break;
            case AUTO_INCREMENT:
                accept(AUTO_INCREMENT);
                break;
            case UNIQUE:
                accept(UNIQUE);
                acceptIf(KEY);
                break;
            case PRIMARY:
                acceptN(PRIMARY, KEY);
                break;
            case KEY:
                accept(KEY);
                break;
            case COMMENT:
                //[COMMENT 'string']
                accept(COMMENT);
                String str = accept(STRING).value;
                break;
            case COLUMN_FORMAT:
                //[COLUMN_FORMAT {FIXED|DYNAMIC|DEFAULT}]
                accept(COLUMN_FORMAT);
                acceptOr(FIXED, DYNAMIC, DEFAULT);
                break;
            case STORAGE:
                //[STORAGE {DISK|MEMORY|DEFAULT}]
                accept(STORAGE);
                acceptOr(DISK, MEMORY, DEFAULT);
                break;
            case REFERENCES:
                parseReferenceDefinition();
            default:
                break;
        }
    }

    public void parseDataType() {
        switch (token().tag) {
            case BIT:
                //BIT[(length)]
                accept(BIT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                break;
            case TINYINT:
                //TINYINT[(length)] [UNSIGNED] [ZEROFILL]
                accept(TINYINT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case SMALLINT:
                //SMALLINT[(length)] [UNSIGNED] [ZEROFILL]
                accept(SMALLINT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case MEDIUMINT:
                //MEDIUMINT[(length)] [UNSIGNED] [ZEROFILL]
                accept(MEDIUMINT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case INT:
                //INT[(length)] [UNSIGNED] [ZEROFILL]
                accept(INT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case INTEGER:
                //INTEGER[(length)] [UNSIGNED] [ZEROFILL]
                accept(INTEGER);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case BIGINT:
                //BIGINT[(length)] [UNSIGNED] [ZEROFILL]
                accept(BIGINT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case REAL:
                //REAL[(length,decimals)] [UNSIGNED] [ZEROFILL]
                accept(REAL);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(COMMA);
                    String decimals = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case DOUBLE:
                accept(DOUBLE);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(COMMA);
                    String decimals = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                //DOUBLE[(length,decimals)] [UNSIGNED] [ZEROFILL]
                break;
            case FLOAT:
                accept(FLOAT);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(COMMA);
                    String decimals = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                //FLOAT[(length,decimals)] [UNSIGNED] [ZEROFILL]
                break;
            case DECIMAL:
                //DECIMAL[(length[,decimals])] [UNSIGNED] [ZEROFILL]
                accept(DECIMAL);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    if (token().tag == RPAREN) {
                        accept(RPAREN);
                    } else {
                        accept(COMMA);
                        String decimals = accept(NUMBER).value;
                        accept(RPAREN);
                    }
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case NUMERIC:
                //NUMERIC[(length[,decimals])] [UNSIGNED] [ZEROFILL]
                accept(NUMERIC);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    if (token().tag == RPAREN) {
                        accept(RPAREN);
                    } else {
                        accept(COMMA);
                        String decimals = accept(NUMBER).value;
                        accept(RPAREN);
                    }
                }
                acceptIf(UNSIGNED);
                acceptIf(ZEROFILL);
                break;
            case DATE:
                //DATE
                accept(DATE);
                break;
            case TIME:
                //TIME[(fsp)]
                accept(TIME);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    parseFsp();
                    accept(RPAREN);
                }
                break;
            case TIMESTAMP:
                //TIMESTAMP[(fsp)]
                accept(TIMESTAMP);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    parseFsp();
                    accept(RPAREN);
                }
                break;
            case DATETIME:
                //DATETIME[(fsp)]
                accept(DATETIME);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    parseFsp();
                    accept(RPAREN);
                }
                break;
            case YEAR:
                //YEAR
                accept(YEAR);
                break;
            case CHAR:
                //CHAR[(length)] [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(CHAR);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case VARCHAR:
                //VARCHAR(length) [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(VARCHAR);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case BINARY:
                //BINARY[(length)]
                accept(BINARY);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                break;
            case VARBINARY:
                //VARBINARY(length)
                accept(VARBINARY);
                if (token().tag == LPAREN) {
                    accept(LPAREN);
                    String length = accept(NUMBER).value;
                    accept(RPAREN);
                }
                break;
            case TINYBLOB:
                //TINYBLOB
                accept(TINYBLOB);
                break;
            case BLOB:
                //BLOB
                accept(BLOB);
                break;
            case MEDIUMBLOB:
                //MEDIUMBLOB
                accept(MEDIUMBLOB);
                break;
            case LONGBLOB:
                //LONGBLOB
                accept(LONGBLOB);
                break;
            case TINYTEXT:
                // TINYTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(TINYTEXT);
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case TEXT:
                //TEXT [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(TEXT);
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case MEDIUMTEXT:
                //MEDIUMTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(MEDIUMTEXT);
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case LONGTEXT:
                //LONGTEXT [BINARY] [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(LONGTEXT);
                acceptIf(BINARY);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case ENUM:
                //ENUM(value1,value2,value3,...) [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(ENUM);
                accept(LPAREN);
                List<String> values = new ArrayList<>();
                do {
                    values.add(parseValue());
                } while (tokenIs(COMMA));
                accept(RPAREN);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            case SET:
                //SET(value1,value2,value3,...) [CHARACTER SET charset_name] [COLLATE collation_name]
                accept(SET);
                accept(LPAREN);
                values = new ArrayList<>();
                do {
                    values.add(parseValue());
                } while (tokenIs(COMMA));
                accept(RPAREN);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    String charsetName = accept(IDENT).value;
                }
                if (token().tag == COLLATE) {
                    accept(COLLATE);
                    String collationName = accept(IDENT).value;
                }
                break;
            default:
                // TODO don't support spatial_type for now
                reportSyntaxError("Expected but " + token().tag);
        }
    }

    public void parseReferenceDefinition() {
        accept(REFERENCES);
        String tableName = accept(IDENT).value;
        accept(LPAREN);
        do {
            parseIndexColName();
        } while (tokenIs(COMMA));
        accept(RPAREN);
        switch (token().tag) {
            case MATCH:
                accept(MATCH);
                acceptOr(FULL, PARTIAL, SIMPLE);
                break;
            case ON:
                accept(ON);
                if (token().tag == DELETE) {
                    accept(DELETE);
                    parseReferenceOption();
                } else {
                    accept(UPDATE);
                    parseReferenceOption();
                }
                break;
            default:
                break;
        }
    }

    private void parseReferenceOption() {
        //RESTRICT | CASCADE | SET NULL | NO ACTION
        switch (token().tag) {
            case RESTRICT:
                accept(RESTRICT);
                break;
            case CASCADE:
                accept(CASCADE);
                break;
            case SET:
                acceptN(SET, NULL);
                break;
            case NO:
                acceptN(NO, ACTION);
                break;
            default:
                reportSyntaxError("Expected [RESTRICT | CASCADE | SET | NO] but " + token().tag);
        }
    }

    private void parseIndexColName() {
        //col_name [(length)] [ASC | DESC]
        String colName = accept(IDENT).value;
        switch (token().tag) {
            case LPAREN:
                accept(LPAREN);
                String length = accept(NUMBER).value;
                accept(RPAREN);
                break;
            case ASC:
                accept(ASC);
                break;
            case DESC:
                accept(DESC);
                break;
            default:
                break;
        }
    }

    private void parseSubpartitionDefinition() {
        accept(SUBPARTITION);
        String logicalName = accept(IDENT).value;
        switch (token().tag) {
            case STORAGE:
                //[[STORAGE] ENGINE [=] engine_name]
                acceptN(STORAGE, ENGINE);
                acceptIf(EQUAL);
                String engineName = accept(IDENT).value;
                break;
            case ENGINE:
                acceptN(ENGINE);
                acceptIf(EQUAL);
                engineName = accept(IDENT).value;
                break;
            case COMMENT:
                //[COMMENT [=] 'comment_text' ]
                accept(COMMENT);
                acceptIf(EQUAL);
                String commentText = accept(STRING).value;
                break;
            case DATA:
                //[DATA DIRECTORY [=] 'data_dir']
                acceptN(DATA, DIRECTORY);
                acceptIf(EQUAL);
                String dataDir = accept(STRING).value;
                break;
            case INDEX:
                //[INDEX DIRECTORY [=] 'index_dir']
                acceptN(INDEX, DIRECTORY);
                acceptIf(EQUAL);
                String indexDir = accept(STRING).value;
                break;
            case MAX_ROWS:
                //[MAX_ROWS [=] max_number_of_rows]
                accept(MAX_ROWS);
                acceptIf(EQUAL);
                String maxNumberOfRows = accept(NUMBER).value;
                break;
            case MIN_ROWS:
                //[MIN_ROWS [=] min_number_of_rows]
                accept(MIN_ROWS);
                acceptIf(EQUAL);
                String minNumberOfRows = accept(NUMBER).value;
                break;
            case TABLESPACE:
                //[TABLESPACE [=] tablespace_name]
                accept(TABLESPACE);
                acceptIf(EQUAL);
                String tablespaceName = accept(IDENT).value;
                break;
            case NODEGROUP:
                //[NODEGROUP [=] node_group_id]
                accept(NODEGROUP);
                acceptIf(EQUAL);
                String nodeGroupId = accept(NUMBER).value;
                break;
            default:
                break;
        }
    }

    public void parsePartitionOptions() {
        acceptN(PARTITION, BY);
        loop:
        while (true) {
            switch (token().tag) {
                case LINEAR:
                    accept(LINEAR);
                    if (token().tag == HASH || token().tag == KEY) {
                        continue loop;
                    } else {
                        reportSyntaxError("Excepted [HASH|KEY] but " + token().tag);
                        break loop;
                    }
                case HASH:
                    //HASH(expr)
                    accept(HASH);
                    accept(LPAREN);
                    exprParser.parseExpr();
                    accept(RPAREN);
                    break loop;
                case KEY:
                    //KEY [ALGORITHM={1|2}] (column_list)
                    accept(KEY);
                    if (token().tag == ALGORITHM) {
                        acceptN(ALGORITHM, EQUAL);
                        String number = accept(NUMBER).value;
                        if (!number.equals("1") && !number.equals("2")) {
                            reportSyntaxError("Expected [0,1] but " + number);
                        }
                    }
                    accept(LPAREN);
                    //assume column_list is like (abc,bcd)
                    List<String> columnList = new ArrayList<>();
                    do {
                        columnList.add(accept(IDENT).value);
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                    break loop;
                case RANGE:
                    //RANGE { (expr) | COLUMNS(column_list) }
                    accept(RANGE);
                    if (token().tag == LPAREN) {
                        accept(LPAREN);
                        exprParser.parseExpr();
                        accept(RPAREN);
                    } else {
                        accept(COLUMNS);
                        accept(LPAREN);
                        //assume column_list is like (abc,bcd)
                        columnList = new ArrayList<>();
                        do {
                            columnList.add(accept(IDENT).value);
                        } while (tokenIs(COMMA));
                        accept(RPAREN);
                    }
                    break loop;
                case LIST:
                    //LIST{(expr) | COLUMNS(column_list)}
                    accept(LIST);
                    if (token().tag == LPAREN) {
                        accept(LPAREN);
                        exprParser.parseExpr();
                        accept(RPAREN);
                    } else {
                        accept(COLUMNS);
                        accept(LPAREN);
                        //assume column_list is like (abc,bcd)
                        columnList = new ArrayList<>();
                        do {
                            columnList.add(accept(IDENT).value);
                        } while (tokenIs(COMMA));
                        accept(RPAREN);
                    }
                    break loop;
                default:
                    reportSyntaxError("Excepted but " + token().tag);
                    break loop;
            }
        }

        switch (token().tag) {
            case PARTITIONS:
                //[PARTITIONS num]
                break;
            case SUBPARTITION:
                //[SUBPARTITION BY { [LINEAR] HASH(expr) | [LINEAR] KEY [ALGORITHM={1|2}] (column_list) } [SUBPARTITIONS num]
                acceptN(SUBPARTITION, BY);
                loop:
                while (true) {
                    switch (token().tag) {
                        case LINEAR:
                            accept(LINEAR);
                            if (token().tag == HASH || token().tag == KEY) {
                                continue loop;
                            } else {
                                reportSyntaxError("Excepted [HASH|KEY] but " + token().tag);
                                break loop;
                            }
                        case HASH:
                            accept(HASH);
                            accept(LPAREN);
                            exprParser.parseExpr();
                            accept(RPAREN);
                            break loop;
                        case KEY:
                            //KEY [ALGORITHM={1|2}] (column_list)
                            accept(KEY);
                            if (token().tag == ALGORITHM) {
                                acceptN(ALGORITHM, EQUAL);
                                String number = accept(NUMBER).value;
                                if (!number.equals("1") && !number.equals("2")) {
                                    reportSyntaxError("Expected [0,1] but " + number);
                                }
                            }
                            accept(LPAREN);
                            //assume column_list is like (abc,bcd)
                            List<String> columnList = new ArrayList<>();
                            do {
                                columnList.add(accept(IDENT).value);
                            } while (tokenIs(COMMA));
                            accept(RPAREN);
                            break loop;
                        default:
                            reportSyntaxError("Excepted [LINEAR|HASH|KEY] but " + token().tag);
                            break loop;
                    }
                }
                if (token().tag == SUBPARTITIONS) {
                    accept(SUBPARTITIONS);
                    String num = accept(NUMBER).value;
                }
                break;
            case LPAREN:
                accept(LPAREN);
                do {
                    parsePartitionDefinition();
                } while (tokenIs(COMMA));
                accept(RPAREN);
                break;
            default:
                // NOP
        }

    }

    public void parseTableOptions() {
        //table_option [[,] table_option] ...
        loop:
        do {
            parseTableOption();
            switch (token().tag) {
                case COMMA:
                case ENGINE:
                case AUTO_INCREMENT:
                case AVG_ROW_LENGTH:
                case DEFAULT:
                case CHARACTER:
                case CHECKSUM:
                case COLLATE:
                case COMMENT:
                case CONNECTION:
                case DATA:
                case DELAY_KEY_WRITE:
                case INDEX:
                case INSERT_METHOD:
                case KEY_BLOCK_SIZE:
                case MAX_ROWS:
                case MIN_ROWS:
                case PACK_KEYS:
                case PASSWORD:
                case ROW_FORMAT:
                case STATS_AUTO_RECALC:
                case STATS_PERSISTENT:
                case STATS_SAMPLE_PAGES:
                case TABLESPACE:
                case UNION:
                    continue loop;
                default:
                    break loop;
            }
        } while (true);
    }

    public void parseTableOption() {
        switch (token().tag) {
            case ENGINE:
                //ENGINE [=] engine_name
                accept(ENGINE);
                acceptIf(EQUAL);
                String engineName = accept(IDENT).value;
                break;
            case AUTO_INCREMENT:
                //AUTO_INCREMENT [=] value
                accept(AUTO_INCREMENT);
                acceptIf(EQUAL);
                String value = accept(NUMBER).value;
                break;
            case AVG_ROW_LENGTH:
                //AVG_ROW_LENGTH [=] value
                accept(AVG_ROW_LENGTH);
                acceptIf(EQUAL);
                value = accept(NUMBER).value;
                break;
            case DEFAULT:
                //[DEFAULT] CHARACTER SET [=] charset_name
                //[DEFAULT] COLLATE [=] collation_name
                accept(DEFAULT);
                if (token().tag == CHARACTER) {
                    acceptN(CHARACTER, SET);
                    acceptIf(EQUAL);
                    String charsetName = accept(IDENT).value;
                } else if (token().tag == COLLATE) {
                    accept(COLLATE);
                    acceptIf(EQUAL);
                    String collationName = accept(IDENT).value;
                } else {
                    reportSyntaxError("Expected [CHARACTER,COLLATE] but " + token().tag);
                }
                break;
            case CHARACTER:
                //CHARACTER SET [=] charset_name
                acceptN(CHARACTER, SET);
                acceptIf(EQUAL);
                String charsetName = accept(IDENT).value;
                break;
            case CHECKSUM:
                //CHECKSUM [=] {0 | 1}
                accept(CHECKSUM);
                acceptIf(EQUAL);
                String number = accept(NUMBER).value;
                if (!number.equals("1") && !number.equals("2")) {
                    reportSyntaxError("Expected [0,1] but " + number);
                }
                break;
            case COLLATE:
                //COLLATE [=] collation_name
                accept(COLLATE);
                acceptIf(EQUAL);
                String collationName = accept(IDENT).value;
                break;
            case COMMENT:
                //COMMENT [=] 'string'
                accept(COMMENT);
                acceptIf(EQUAL);
                String str = accept(STRING).value;
                break;
            case CONNECTION:
                //CONNECTION [=] 'connect_string'
                accept(CONNECTION);
                acceptIf(EQUAL);
                String connectString = accept(STRING).value;
                break;
            case DATA:
                //DATA DIRECTORY [=] 'absolute path to directory'
                acceptN(DATA, DIRECTORY);
                acceptIf(EQUAL);
                String path = accept(STRING).value;
                break;
            case DELAY_KEY_WRITE:
                //DELAY_KEY_WRITE [=] {0 | 1}
                accept(DELAY_KEY_WRITE);
                acceptIf(EQUAL);
                number = accept(NUMBER).value;
                if (!number.equals("1") && !number.equals("2")) {
                    reportSyntaxError("Expected [0,1] but " + number);
                }
                break;
            case INDEX:
                //INDEX DIRECTORY [=] 'absolute path to directory'
                acceptN(INDEX, DIRECTORY);
                acceptIf(EQUAL);
                path = accept(STRING).value;
                break;
            case INSERT_METHOD:
                //INSERT_METHOD [=] { NO | FIRST | LAST }
                accept(INSERT_METHOD);
                acceptIf(EQUAL);
                acceptOr(NO, FIRST, LAST);
                break;
            case KEY_BLOCK_SIZE:
                //KEY_BLOCK_SIZE [=] value
                accept(KEY_BLOCK_SIZE);
                acceptIf(EQUAL);
                value = accept(NUMBER).value;
                break;
            case MAX_ROWS:
                //MAX_ROWS [=] value
                accept(MAX_ROWS);
                acceptIf(EQUAL);
                value = accept(NUMBER).value;
                break;
            case MIN_ROWS:
                //MIN_ROWS [=] value
                accept(MIN_ROWS);
                acceptIf(EQUAL);
                value = accept(NUMBER).value;
                break;
            case PACK_KEYS:
                //PACK_KEYS [=] {0 | 1 | DEFAULT}
                accept(PACK_KEYS);
                acceptIf(EQUAL);
                if (token().tag == NUMBER) {
                    number = accept(NUMBER).value;
                    if (!number.equals("1") && !number.equals("2")) {
                        reportSyntaxError("Expected [0,1] but " + number);
                    }
                } else {
                    accept(DEFAULT);
                }
                break;
            case PASSWORD:
                //PASSWORD [=] 'string'
                accept(PASSWORD);
                acceptIf(EQUAL);
                str = accept(STRING).value;
                break;
            case ROW_FORMAT:
                //ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
                accept(ROW_FORMAT);
                acceptIf(EQUAL);
                acceptOr(DEFAULT, DYNAMIC, FIXED, COMPRESSED, REDUNDANT, COMPACT);
                break;
            case STATS_AUTO_RECALC:
                //STATS_AUTO_RECALC [=] {DEFAULT|0|1}
                accept(STATS_AUTO_RECALC);
                acceptIf(EQUAL);
                if (token().tag == NUMBER) {
                    number = accept(NUMBER).value;
                    if (!number.equals("1") && !number.equals("2")) {
                        reportSyntaxError("Expected [0,1] but " + number);
                    }
                } else {
                    accept(DEFAULT);
                }
                break;
            case STATS_PERSISTENT:
                //STATS_PERSISTENT [=] {DEFAULT|0|1}
                accept(STATS_PERSISTENT);
                acceptIf(EQUAL);
                if (token().tag == NUMBER) {
                    number = accept(NUMBER).value;
                    if (!number.equals("1") && !number.equals("2")) {
                        reportSyntaxError("Expected [0,1] but " + number);
                    }
                } else {
                    accept(DEFAULT);
                }
                break;
            case STATS_SAMPLE_PAGES:
                //STATS_SAMPLE_PAGES [=] value
                accept(STATS_SAMPLE_PAGES);
                acceptIf(EQUAL);
                value = accept(NUMBER).value;
                break;
            case TABLESPACE:
                //TABLESPACE tablespace_name [STORAGE {DISK|MEMORY|DEFAULT}]
                accept(TABLESPACE);
                String tablespaceName = accept(IDENT).value;
                if (token().tag == STORAGE) {
                    accept(STORAGE);
                    acceptOr(DISK, MEMORY, DEFAULT);
                }
                break;
            case UNION:
                //UNION [=] (tbl_name[,tbl_name]...)
                accept(UNION);
                acceptIf(EQUAL);
                accept(LPAREN);
                List<String> tableNames = new ArrayList<>();
                do {
                    tableNames.add(accept(IDENT).value);
                } while (tokenIs(COMMA));
                accept(RPAREN);
                break;
            default:
                reportSyntaxError("Expected '' but " + token().tag);
        }
    }

    public void parsePartitionDefinition() {
        accept(PARTITION);
        String partitionName = accept(IDENT).value;
        switch (token().tag) {
            case VALUES:
                //[VALUES {LESS THAN {(expr | value_list) | MAXVALUE} | IN (value_list)}]
                accept(VALUES);
                if (token().tag == LESS) {
                    acceptN(LESS, THAN);
                    if (token().tag == LPAREN) {
                        accept(LPAREN);
                        List<String> valueList = new ArrayList<>();
                        do {
                            if (lookahead(COMMA)) {
                                valueList.add(parseValue());
                            } else {
                                exprParser.parseExpr();
                            }
                        } while (tokenIs(COMMA));
                        accept(RPAREN);
                    } else {
                        accept(MAXVALUE);
                    }
                } else {
                    accept(IN);
                    accept(LPAREN);
                    List<String> valueList = new ArrayList<>();
                    do {
                        valueList.add(parseValue());
                    } while (tokenIs(COMMA));
                    accept(RPAREN);
                }
                break;

            case STORAGE:
                //[[STORAGE] ENGINE [=] engine_name]
                acceptN(STORAGE, ENGINE);
                acceptIf(EQUAL);
                String engineName = accept(IDENT).value;
                break;
            case ENGINE:
                acceptN(ENGINE);
                acceptIf(EQUAL);
                engineName = accept(IDENT).value;
                break;
            case COMMENT:
                //[COMMENT [=] 'comment_text' ]
                accept(COMMENT);
                acceptIf(EQUAL);
                String commentText = accept(STRING).value;
                break;
            case DATA:
                //[DATA DIRECTORY [=] 'data_dir']
                acceptN(DATA, DIRECTORY);
                acceptIf(EQUAL);
                String dataDir = accept(STRING).value;
                break;
            case INDEX:
                //[INDEX DIRECTORY [=] 'index_dir']
                acceptN(INDEX, DIRECTORY);
                acceptIf(EQUAL);
                String indexDir = accept(STRING).value;
                break;
            case MAX_ROWS:
                //[MAX_ROWS [=] max_number_of_rows]
                accept(MAX_ROWS);
                acceptIf(EQUAL);
                String maxNumberOfRows = accept(NUMBER).value;
                break;
            case MIN_ROWS:
                //[MIN_ROWS [=] min_number_of_rows]
                accept(MIN_ROWS);
                acceptIf(EQUAL);
                String minNumberOfRows = accept(NUMBER).value;
                break;
            case TABLESPACE:
                //[TABLESPACE [=] tablespace_name]
                accept(TABLESPACE);
                acceptIf(EQUAL);
                String tablespaceName = accept(IDENT).value;
                break;
            case NODEGROUP:
                //[NODEGROUP [=] node_group_id]
                accept(NODEGROUP);
                acceptIf(EQUAL);
                String nodeGroupId = accept(NUMBER).value;
                break;
            case LPAREN:
                //[(subpartition_definition [, subpartition_definition] ...)]
                accept(LPAREN);
                do {
                    parseSubpartitionDefinition();
                } while (tokenIs(COMMA));
                break;
            default:
                break;
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

    private String parseFsp() {
        String fsp = accept(STRING).value;
        return fsp;
    }

    private String parseDefaultValue() {
        switch (token().tag) {
            case STRING:
                return accept(STRING).value;
            case NUMBER:
                return accept(NUMBER).value;
            case CURRENT_DATE:
                accept(CURRENT_DATE);
                return "CURRENT_DATE";
            case CURRENT_TIMESTAMP:
                accept(CURRENT_TIMESTAMP);
                if (token().tag == LPAREN) {
                    acceptN(LPAREN, RPAREN);
                    return "CURRENT_TIMESTAMP()";
                }
                return "CURRENT_TIMESTAMP";
            case LOCALTIME:
                accept(LOCALTIME);
                if (token().tag == LPAREN) {
                    acceptN(LPAREN, RPAREN);
                    return "LOCALTIME()";
                }
                return "LOCALTIME";
            case LOCALTIMESTAMP:
                accept(LOCALTIMESTAMP);
                if (token().tag == LPAREN) {
                    acceptN(LPAREN, RPAREN);
                    return "LOCALTIMESTAMP()";
                }
                return "LOCALTIMESTAMP";
            case NOW:
                acceptN(NOW, LPAREN, RPAREN);
                return "NOW()";
            case NULL:
                accept(NULL);
                return "NULL";
            default:
                reportSyntaxError("Expected but " + token().tag);
                return null;
        }
    }

    private String parseValue() {
        return parseDefaultValue();
    }

    public static void main(String[] args) {
        String sql = "CREATE TABLE `t2` (\n" +
                "  `id` int(11) NOT NULL,\n" +
                "  `1a` varchar(45) NOT NULL,\n" +
                "  `1.23E5` varchar(20) DEFAULT 'abc',\n" +
                "  `\\nid` varchar(45) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=latin1\n";
        MysqlScanner scanner = new MysqlScanner(sql.toCharArray());
        CreateTableParser parser = new CreateTableParser(scanner);
        parser.parseCreateTable();
    }
}
