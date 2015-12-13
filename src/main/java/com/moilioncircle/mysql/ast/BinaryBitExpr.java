package com.moilioncircle.mysql.ast;

import com.moilioncircle.mysql.tokenizer.TokenTag;

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

/**
 * bit_expr | bit_expr
 | bit_expr & bit_expr
 | bit_expr << bit_expr
 | bit_expr >> bit_expr
 | bit_expr + bit_expr
 | bit_expr - bit_expr
 | bit_expr * bit_expr
 | bit_expr / bit_expr
 | bit_expr DIV bit_expr
 | bit_expr MOD bit_expr
 | bit_expr % bit_expr
 | bit_expr ^ bit_expr
 */
public class BinaryBitExpr extends BitExpr{
    public BitExpr left;
    public TokenTag binaryOp;
    public BitExpr right;
}
