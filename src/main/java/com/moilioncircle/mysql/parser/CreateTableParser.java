package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;

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

    public CreateTableParser(MysqlScanner scanner) {
        super(scanner);
    }

    public void parseColumnDefinition(){
        System.out.println(token().value);
    }

    public void parseReferenceDefinition(){
        //TODO
    }

    public void parsePartitionOptions() {
        //TODO
    }

    public void parseTableOption(){
//        table_option:
//        ENGINE [=] engine_name
//        | AUTO_INCREMENT [=] value
//        | AVG_ROW_LENGTH [=] value
//        | [DEFAULT] CHARACTER SET [=] charset_name
//        | CHECKSUM [=] {0 | 1}
//        | [DEFAULT] COLLATE [=] collation_name
//        | COMMENT [=] 'string'
//        | CONNECTION [=] 'connect_string'
//        | DATA DIRECTORY [=] 'absolute path to directory'
//        | DELAY_KEY_WRITE [=] {0 | 1}
//        | INDEX DIRECTORY [=] 'absolute path to directory'
//        | INSERT_METHOD [=] { NO | FIRST | LAST }
//        | KEY_BLOCK_SIZE [=] value
//        | MAX_ROWS [=] value
//        | MIN_ROWS [=] value
//        | PACK_KEYS [=] {0 | 1 | DEFAULT}
//        | PASSWORD [=] 'string'
//        | ROW_FORMAT [=] {DEFAULT|DYNAMIC|FIXED|COMPRESSED|REDUNDANT|COMPACT}
//        | STATS_AUTO_RECALC [=] {DEFAULT|0|1}
//        | STATS_PERSISTENT [=] {DEFAULT|0|1}
//        | STATS_SAMPLE_PAGES [=] value
//        | TABLESPACE tablespace_name [STORAGE {DISK|MEMORY|DEFAULT}]
//        | UNION [=] (tbl_name[,tbl_name]...)
    }

    public void parsePartitionDefinition(){

    }
}
