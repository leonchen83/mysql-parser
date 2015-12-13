package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import com.moilioncircle.mysql.tokenizer.Token;

import static com.moilioncircle.mysql.tokenizer.TokenTag.*;
/**
 * Copyright leon
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http:*www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author leon on 15-12-11
 */

/**
 * Operator Precedence
 * INTERVAL
 * BINARY, COLLATE
 * !
 * - (unary minus), ~ (unary bit inversion)
 * ^
 * *, /, DIV, %, MOD
 * -, +
 * <<, >>
 * &
 * |
 * = (comparison), <=>, >=, >, <=, <, <>, !=, IS, LIKE, REGEXP, IN
 * BETWEEN, CASE, WHEN, THEN, ELSE
 * NOT
 * AND, &&
 * XOR
 * OR, ||
 * = (assignment), :=
 *
 * The shunting yard algorithm
 * [href = http://www.engr.mun.ca/~theo/Misc/exp_parsing.htm ]
 */
public class ExprParser extends AbstractParser {

    public ExprParser(MysqlScanner scanner) {
        super(scanner);
    }
    /**
     * expr =
     *                         NOT expr expr'
     *                       | ! expr expr'
     *                       | boolean_primary IS [NOT] {TRUE | FALSE | UNKNOWN} expr'
     *                       | boolean_primary expr'
     */
    public void parseExpr() {
        switch (token().tag){
            case NOT:
                accept(NOT);
                parseExpr();
                break;
            case BIT_OR:
                accept(BIT_OR);
                parseExpr();
                break;
            default:
                parseBooleanPrimary();
                if(token().tag == IS){
                    accept(IS);
                    acceptIf(NOT);
                    Token tok = acceptOr(TRUE,FALSE, UNKNOWN);
                }
                break;
        }
        parseExprRest();
    }

    /**
     * expr'=
     *                         OR expr expr'
     *                       | '||' expr expr'
     *                       | XOR expr expr'
     *                       | AND expr expr'
     *                       | && expr expr'
     *                       | ε
     */
    public void parseExprRest(){
        switch (token().tag){
            case OR:
                accept(OR);
                parseExpr();
                parseExprRest();
                break;
            case BOBO:
                accept(BOBO);
                parseExpr();
                parseExprRest();
                break;
            case XOR:
                accept(XOR);
                parseExpr();
                parseExprRest();
                break;
            case AND:
                accept(AND);
                parseExpr();
                parseExprRest();
                break;
            case BABA:
                accept(BABA);
                parseExpr();
                parseExprRest();
                break;
            default:
                break;
        }
    }

    /**
     * boolean_primary=
     *                         predicate boolean_primary'
     *
     */
    public void parseBooleanPrimary(){
        parsePredicate();
        parseBooleanPrimaryRest();
    }

    /**
     * boolean_primary'=
     *                         IS [NOT] NULL boolean_primary'
     *                       | <=> predicate boolean_primary'
     *                       | comparison_operator predicate boolean_primary'
     *                       | comparison_operator {ALL | ANY} (subquery) boolean_primary'
     *                       | ε
     *
     */
    public void parseBooleanPrimaryRest(){
        switch (token().tag) {
            case IS:
                accept(IS);
                acceptIf(NOT);
                accept(NULL);
                parseBooleanPrimaryRest();
                break;
            case LTEQGT:
                accept(LTEQGT);
                parsePredicate();
                parseBooleanPrimaryRest();
                break;
            default:
                if (isComparisonOperator()) {
                    parseComparisonOperator();
                    if(token().tag == ALL || token().tag == ANY){
                        acceptOr(ALL,ANY);
                        accept(LPAREN);
                        parseSubQuery();
                        accept(RPAREN);
                        parseBooleanPrimaryRest();
                    }else{
                        parsePredicate();
                        parseBooleanPrimaryRest();
                    }
                }
                break;
        }
    }

    /**
     * comparison_operator=
     *                         =
     *                       | >=
     *                       | >
     *                       | <=
     *                       | <
     *                       | <>
     *                       | !=
     */
    public Token parseComparisonOperator(){
        switch (token().tag){
            case EQUAL:
                return accept(EQUAL);
            case GE:
                return accept(GE);
            case GT:
                return accept(GT);
            case LE:
                return accept(LE);
            case LT:
                return accept(LT);
            case LTGT:
                return accept(LTGT);
            case NOT_EQ:
                return accept(NOT_EQ);
            default:
                reportSyntaxError("Expected but"+token().tag);
                return null;
        }
    }

    private boolean isComparisonOperator() {
        switch (token().tag) {
            case EQUAL:
            case GE:
            case GT:
            case LE:
            case LT:
            case LTGT:
            case NOT_EQ:
                return true;
            default:
                return false;
        }
    }

    /**
     * predicate=
     *                         bit_expr [NOT] IN (subquery)
     *                       | bit_expr [NOT] IN (expr [, expr] ...)
     *                       | bit_expr [NOT] BETWEEN bit_expr AND predicate
     *                       | bit_expr SOUNDS LIKE bit_expr
     *                       | bit_expr [NOT] LIKE simple_expr [ESCAPE simple_expr]
     *                       | bit_expr [NOT] REGEXP bit_expr
     *                       | bit_expr
     */
    public void parsePredicate(){
        parseBitExpr();
        loop:
        while (true) {
            switch (token().tag) {
                case NOT:
                    accept(NOT);
                    if (token().tag == IN || token().tag == BETWEEN || token().tag == LIKE || token().tag == REGEXP) {
                        continue loop;
                    } else {
                        reportSyntaxError("Expected but" + token().tag);
                    }
                case IN:
                    accept(IN);
                    accept(LPAREN);
                    if (token().tag == SELECT) {
                        parseSubQuery();
                    } else {
                        do {
                            parseExpr();
                        } while (tokenIs(COMMA));
                    }
                    accept(RPAREN);
                    break loop;
                case BETWEEN:
                    accept(BETWEEN);
                    parseBitExpr();
                    accept(AND);
                    parsePredicate();
                    break loop;
                case SOUNDS:
                    acceptN(SOUNDS,LIKE);
                    parseBitExpr();
                    break loop;
                case LIKE:
                    //LIKE simple_expr [ESCAPE simple_expr]
                    accept(LIKE);
                    parseSimpleExpr();
                    if(token().tag == ESCAPE){
                        accept(ESCAPE);
                        parseSimpleExpr();
                    }
                    break loop;
                case REGEXP:
                    //REGEXP bit_expr
                    accept(REGEXP);
                    parseBitExpr();
                    break loop;
                default:
                    break loop;
            }
        }

    }

    /**
     * bit_expr=
     *                         simple_expr bit_expr'
     */
    public void parseBitExpr(){
        parseSimpleExpr();
        parseBitExprRest();
    }

    /**
     * bit_expr'=
     *                         '|' bit_expr bit_expr'
     *                       | & bit_expr bit_expr'
     *                       | << bit_expr bit_expr'
     *                       | >> bit_expr bit_expr'
     *                       | + bit_expr bit_expr'
     *                       | - bit_expr bit_expr'
     *                       | * bit_expr bit_expr'
     *                       | / bit_expr bit_expr'
     *                       | DIV bit_expr bit_expr'
     *                       | MOD bit_expr bit_expr'
     *                       | % bit_expr bit_expr'
     *                       | ^ bit_expr bit_expr'
     *                       | ε
     *
     *
     *                       | + interval_expr bit_expr' // DON'T SUPPORT
     *                       | - interval_expr bit_expr' // DON'T SUPPORT
     *
     */
    public void parseBitExprRest(){
        switch (token().tag){
            case BIT_OR:
                accept(BIT_OR);
                parseBitExpr();
                parseBitExprRest();
                break;
            case BIT_AND:
                accept(BIT_AND);
                parseBitExpr();
                parseBitExprRest();
                break;
            case LTLT:
                accept(LTLT);
                parseBitExpr();
                parseBitExprRest();
                break;
            case PLUS:
                accept(PLUS);
                parseBitExpr();
                parseBitExprRest();
                break;
            case MINUS:
                accept(MINUS);
                parseBitExpr();
                parseBitExprRest();
                break;
            case STAR:
                accept(STAR);
                parseBitExpr();
                parseBitExprRest();
                break;
            case SLASH:
                accept(SLASH);
                parseBitExpr();
                parseBitExprRest();
                break;
            case DIV:
                accept(DIV);
                parseBitExpr();
                parseBitExprRest();
                break;
            case MOD:
                accept(MOD);
                parseBitExpr();
                parseBitExprRest();
                break;
            case REMAINDER:
                accept(REMAINDER);
                parseBitExpr();
                parseBitExprRest();
                break;
            case EOR:
                accept(EOR);
                parseBitExpr();
                parseBitExprRest();
                break;
            default:
                break;
        }
    }

    /**
     * simple_expr=
     *                         literal simple_expr'
     *                       | identifier simple_expr'
     *                       | function_call simple_expr'
     *                       | param_marker simple_expr'
     *                       | + simple_expr simple_expr'
     *                       | - simple_expr simple_expr'
     *                       | ~ simple_expr simple_expr'
     *                       | ! simple_expr simple_expr'
     *                       | BINARY simple_expr simple_expr'
     *                       | (expr [, expr] ...) simple_expr'
     *                       | ROW (expr, expr [, expr] ...) simple_expr'
     *                       | (subquery) simple_expr'
     *                       | EXISTS (subquery) simple_expr'
     *
     *
     *                       | case_expr simple_expr' // DON'T SUPPORT for now
     *                       | match_expr simple_expr' // DON'T SUPPORT
     *                       | interval_expr simple_expr' // DON'T SUPPORT
     *                       | {identifier expr} simple_expr' // DON'T SUPPORT
     *                       | variable simple_expr' // DON'T SUPPORT
     */
    public void parseSimpleExpr() {
        switch (token().tag) {
            case IDENT:
                if (lookahead(LPAREN)) {
                    parseFunctionCall();
                } else {
                    String identifier = accept(IDENT).value;
                }
                break;
            case STRING:
            case NUMBER:
            case TRUE:
            case FALSE:
            case BIT_VALUE:
            case HEX_VALUE:
            case NULL:
                parseLiteral();
                break;
            case PARAM_MARKER:
                accept(PARAM_MARKER);
                break;
            case MINUS:
                accept(MINUS);
                //实际上是UNARY_MINUS
                parseSimpleExpr();
                break;
            case PLUS:
                accept(PLUS);
                //实际上是UNARY_PLUS
                parseSimpleExpr();
                break;
            case NOT_OPERATOR:
                accept(NOT_OPERATOR);
                //实际上是UNARY_NOT_OPERATOR
                parseSimpleExpr();
                break;
            case UNARY_BIT:
                accept(UNARY_BIT);
                //实际上是UNARY_BIT
                parseSimpleExpr();
                break;
            case BINARY:
                accept(BINARY);
                parseSimpleExpr();
                break;
            case LPAREN:
                if(lookahead(SELECT)){
                    accept(LPAREN);
                    parseSubQuery();
                    accept(RPAREN);
                }else{
                    accept(LPAREN);
                    do{
                        parseExpr();
                    }while(tokenIs(COMMA));
                    accept(RPAREN);
                }
                break;
            case ROW:
                accept(ROW);
                accept(LPAREN);
                do{
                    parseExpr();
                }while(tokenIs(COMMA));
                accept(RPAREN);
                break;
            case EXISTS:
                accept(EXISTS);
                accept(LPAREN);
                parseSubQuery();
                accept(RPAREN);
                break;
            default:
                reportSyntaxError("Expected but "+token().tag);

        }
        parseSimpleExprRest();
    }

    /**
     * simple_expr'=
     *                         COLLATE collation_name simple_expr'
     *                       | '||' simple_expr simple_expr'
     *                       | ε
     *
     */
    public void parseSimpleExprRest(){
        switch (token().tag){
            case COLLATE:
                accept(COLLATE);
                String collationName = accept(IDENT).value;
                parseSimpleExprRest();
                break;
            case BOBO:
                accept(BOBO);
                parseSimpleExpr();
                parseSimpleExprRest();
                break;
            default:
                break;
        }
    }

    private void parseSubQuery() {
        //TODO
    }

    private void parseLiteral(){
        switch (token().tag){
            case STRING:
                accept(STRING);
                break;
            case NUMBER:
                accept(NUMBER);
                break;
            case HEX_VALUE:
                accept(HEX_VALUE);
                break;
            case BIT_VALUE:
                accept(BIT_VALUE);
                break;
            case TRUE:
                accept(TRUE);
                break;
            case FALSE:
                accept(FALSE);
                break;
            case NULL:
                accept(NULL);
                break;
            default:
                reportSyntaxError("Expected but "+token().tag);
        }
    }

    private void parseFunctionCall(){
        // identifier(expr,expr....)
        String functionName = accept(IDENT).value;
        accept(LPAREN);
        do{
            parseExpr();
        }while(tokenIs(COMMA));
        accept(RPAREN);
    }

    public static void main(String[] args) {
        String expr = "abc is not null";
        MysqlScanner scanner = new MysqlScanner(expr.toCharArray());
        ExprParser parser = new ExprParser(scanner);
        parser.parseExpr();
    }
}
