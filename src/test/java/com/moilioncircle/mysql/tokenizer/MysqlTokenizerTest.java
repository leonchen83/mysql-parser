package com.moilioncircle.mysql.tokenizer;

import junit.framework.TestCase;
import org.junit.Test;

public class MysqlTokenizerTest extends TestCase {

    @Test
    public void testScanBit() {
        String bit = "b'10101'";
        MysqlTokenizer tokenizer = new MysqlTokenizer(bit.toCharArray());
        Token token = tokenizer.nextToken();
        assertEquals("10101", token.value);
        assertEquals(TokenTag.BIT, token.tag);
    }

    @Test
    public void testScanString() {
        String bit = "'abcd'";
        MysqlTokenizer tokenizer = new MysqlTokenizer(bit.toCharArray());
        Token token = tokenizer.nextToken();
        assertEquals("abcd", token.value);
        assertEquals(TokenTag.STRING, token.tag);

        bit = "'ab''cd'";
        tokenizer = new MysqlTokenizer(bit.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("ab'cd", token.value);
        assertEquals(TokenTag.STRING, token.tag);

        bit = "'ab'''";
        tokenizer = new MysqlTokenizer(bit.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("ab'", token.value);
        assertEquals(TokenTag.STRING, token.tag);

        bit = "'b\\0'";
        tokenizer = new MysqlTokenizer(bit.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("b", token.value);
        assertEquals(TokenTag.STRING, token.tag);

        bit = "'b\\n'";
        tokenizer = new MysqlTokenizer(bit.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("b\n", token.value);
        assertEquals(TokenTag.STRING, token.tag);
    }

    @Test
    public void testScanHex() {
        String bit = "X'0AF'";
        MysqlTokenizer tokenizer = new MysqlTokenizer(bit.toCharArray());
        Token token = tokenizer.nextToken();
        assertEquals("0AF", token.value);
        assertEquals(TokenTag.HEX, token.tag);

        try {
            bit = "X'0AF";
            tokenizer = new MysqlTokenizer(bit.toCharArray());
            tokenizer.nextToken();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Invalid hex number.", e.getMessage());
        }
    }

    @Test
    public void testScanIdent() {
        String bit = "abc1231.23esfs";
        MysqlTokenizer tokenizer = new MysqlTokenizer(bit.toCharArray());
        Token token = tokenizer.nextToken();
        assertEquals("abc1231", token.value);
        assertEquals(TokenTag.IDENT, token.tag);

        bit = "`1.23e5`";
        tokenizer = new MysqlTokenizer(bit.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("1.23e5", token.value);
        assertEquals(TokenTag.IDENT, token.tag);
    }

    @Test
    public void testScanNumber() {
        String num = "1.2e+5+6";
        MysqlTokenizer tokenizer = new MysqlTokenizer(num.toCharArray());
        Token token = tokenizer.nextToken();
        assertEquals("1.2e+5", token.value);
        assertEquals(TokenTag.NUMBER, token.tag);

        num = "1.2e+56";
        tokenizer = new MysqlTokenizer(num.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("1.2e+56", token.value);
        assertEquals(TokenTag.NUMBER, token.tag);

        num = "1.256";
        tokenizer = new MysqlTokenizer(num.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("1.256", token.value);
        assertEquals(TokenTag.NUMBER, token.tag);

        num = "1256";
        tokenizer = new MysqlTokenizer(num.toCharArray());
        token = tokenizer.nextToken();
        assertEquals("1256", token.value);
        assertEquals(TokenTag.NUMBER, token.tag);

        num = ".256";
        tokenizer = new MysqlTokenizer(num.toCharArray());
        token = tokenizer.nextToken();
        assertEquals(".256", token.value);
        assertEquals(TokenTag.NUMBER, token.tag);

        try {
            num = ".2E";
            tokenizer = new MysqlTokenizer(num.toCharArray());
            tokenizer.nextToken();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Invalid number.", e.getMessage());
        }

        try {
            num = "0.E5";
            tokenizer = new MysqlTokenizer(num.toCharArray());
            tokenizer.nextToken();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Invalid number.", e.getMessage());
        }

        try {
            num = "1.24e+";
            tokenizer = new MysqlTokenizer(num.toCharArray());
            tokenizer.nextToken();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Invalid number.", e.getMessage());
        }
    }
}