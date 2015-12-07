package com.moilioncircle.mysql.ast;

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
public class Pair {
    private final String tableName;
    private final String newTableName;

    private Pair(String tableName, String newTableName) {
        this.tableName = tableName;
        this.newTableName = newTableName;
    }

    public static Pair pair(String tableName, String newTableName) {
        return new Pair(tableName, newTableName);
    }

    @Override
    public String toString() {
        return tableName+" TO "+newTableName;
    }
}
