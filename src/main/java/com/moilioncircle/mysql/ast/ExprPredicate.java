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
 * @author leon on 15-12-13
 */

import java.util.List;

/**
 * bit_expr [NOT] IN (expr [, expr] ...)
 */
public class ExprPredicate extends Predicate {
    public BitExpr bitExpr;
    public boolean hasNot;
    public List<Expr> exprList;
}
