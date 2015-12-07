package com.moilioncircle.mysql.ast;

import java.util.ArrayList;
import java.util.List;

import static com.moilioncircle.mysql.ast.Pair.pair;

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
public class RenameTable {
    private List<Pair> pairs = new ArrayList<>();

    public RenameTable(List<Pair> pairs) {
        this.pairs = pairs;
    }

    public RenameTable(){

    }

    public void addPair(String tableName,String newTableName){
        pairs.add(pair(tableName,newTableName));
    }

    @Override
    public String toString() {
        //pretty print
        StringBuilder builder = new StringBuilder();
        builder.append("RENAME TABLE\n");
        pairs.forEach(e->builder.append("    "+e.toString()+",\n"));
        builder.deleteCharAt(builder.length()-2);
        return builder.toString();
    }
}

