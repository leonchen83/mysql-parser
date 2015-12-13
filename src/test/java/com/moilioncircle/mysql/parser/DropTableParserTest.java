package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import junit.framework.TestCase;

public class DropTableParserTest extends TestCase {

    public void testParseDropTable() {
        String str = "drop Temporary table if exists abc,bcd RESTRICT";
        MysqlScanner scanner = new MysqlScanner(str.toCharArray());
        DropTableParser parser = new DropTableParser(scanner);
        parser.parseDropTable();
    }

    public void testParseDropTable1() {
        String str = "drop Temporary table if exists";
        try {
            MysqlScanner scanner = new MysqlScanner(str.toCharArray());
            DropTableParser parser = new DropTableParser(scanner);
            parser.parseDropTable();
            fail();
        } catch (RuntimeException e) {
            assertEquals("Expected IDENT but EOF", e.getMessage());
        }

    }

}