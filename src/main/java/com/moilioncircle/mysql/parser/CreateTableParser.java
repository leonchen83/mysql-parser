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

    public CreateTableParser(MysqlScanner scanner) {
        super(scanner);
        exprParser = new ExprParser(scanner);
    }

    public void parseColumnDefinition() {
        //TODO
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
        //TODO
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
                                valueList.add(accept(NUMBER).value);
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
                        valueList.add(accept(NUMBER).value);
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
}
