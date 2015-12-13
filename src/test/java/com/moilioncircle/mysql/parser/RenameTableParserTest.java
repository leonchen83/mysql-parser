package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import junit.framework.TestCase;

public class RenameTableParserTest extends TestCase {

    public void testParseRenameTable() {
        String str = "rename table aa to bb ,cc to dd ";
        MysqlScanner scanner = new MysqlScanner(str.toCharArray());
        RenameTableParser parser = new RenameTableParser(scanner);
        parser.parseRenameTable();
    }

    public void testParseRenameTable1() {
        String str = "rename table aa ";
        try {
            MysqlScanner scanner = new MysqlScanner(str.toCharArray());
            RenameTableParser parser = new RenameTableParser(scanner);
            parser.parseRenameTable();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Expected TO but EOF", e.getMessage());
        }

    }
}