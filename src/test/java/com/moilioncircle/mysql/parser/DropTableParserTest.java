package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.ast.DropTable;
import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import junit.framework.TestCase;

public class DropTableParserTest extends TestCase {

    public void testParseDropTable(){
        String str = "drop Temporary table if exists abc,bcd RESTRICT";
        MysqlScanner scanner = new MysqlScanner(str.toCharArray());
        DropTableParser parser = new DropTableParser(scanner);
        DropTable table = parser.parseDropTable();
        assertEquals("DROP TEMPORARY IF EXISTS \n" +
                "    abc,\n" +
                "    bcd\n" +
                "RESTRICT",table.toString());
    }

}