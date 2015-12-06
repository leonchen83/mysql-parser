package com.moilioncircle.mysql.ast;

import java.util.ArrayList;
import java.util.List;

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
public class DropTable {
    private boolean isTemporary;
    private boolean ifExists;
    private boolean restrict;
    private boolean cascade;
    private List<String> tables = new ArrayList<>();

    public DropTable(boolean isTemporary, boolean ifExists, boolean restrict, boolean cascade, List<String> tables) {
        this.isTemporary = isTemporary;
        this.ifExists = ifExists;
        this.restrict = restrict;
        this.cascade = cascade;
        this.tables = tables;
    }

    @Override
    public String toString() {
        return "DropTable{" +
                "isTemporary=" + isTemporary +
                ", ifExists=" + ifExists +
                ", restrict=" + restrict +
                ", cascade=" + cascade +
                ", tables=" + tables +
                '}';
    }
}
