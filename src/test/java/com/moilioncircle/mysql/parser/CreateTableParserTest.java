package com.moilioncircle.mysql.parser;

import com.moilioncircle.mysql.tokenizer.MysqlScanner;
import junit.framework.TestCase;

public class CreateTableParserTest extends TestCase {

    public void testParseCreateTable1() throws Exception {
        String sql = "CREATE TABLE `aw_member_info` (\n" +
                "  `id` int(11) NOT NULL,\n" +
                "  `member_id` int(11) NOT NULL,\n" +
                "  `authemail` tinyint(1) NOT NULL COMMENT '是否进行了邮箱认证',\n" +
                "  `truename` varchar(45) DEFAULT NULL,\n" +
                "  `authpersonal` tinyint(1) NOT NULL COMMENT '个人银行卡认证状态：0未认证。1已经认证。',\n" +
                "  `authcompany` tinyint(1) NOT NULL COMMENT '公司认证状态',\n" +
                "  `question` int(5) DEFAULT NULL COMMENT '问题ID',\n" +
                "  `answer` varchar(100) DEFAULT '' COMMENT '回答问题',\n" +
                "  `avatar` varchar(255) NOT NULL DEFAULT '' COMMENT '头像',\n" +
                "  `mycate` varchar(255) DEFAULT NULL COMMENT '会员自主标签|栏目标签',\n" +
                "  `mytech` varchar(255) DEFAULT NULL COMMENT '我的技能',\n" +
                "  `sex` tinyint(1) NOT NULL DEFAULT '0' COMMENT '性别',\n" +
                "  `birtyday` int(11) DEFAULT now() COMMENT '生日时间',\n" +
                "  `homeaddr` varchar(255) DEFAULT '' COMMENT '家庭住址',\n" +
                "  `address` varchar(255) DEFAULT '' COMMENT '详细的居住地址',\n" +
                "  `province` int(20) DEFAULT NULL COMMENT '所在省份',\n" +
                "  `city` int(20) DEFAULT NULL COMMENT '所在城市',\n" +
                "  `area` int(20) DEFAULT NULL COMMENT '所在区域',\n" +
                "  `figure` tinyint(2) DEFAULT NULL COMMENT '体型',\n" +
                "  `profession` tinyint(2) DEFAULT NULL COMMENT '职业',\n" +
                "  `height` varchar(20) DEFAULT '' COMMENT '身高',\n" +
                "  `culture` int(11) DEFAULT NULL COMMENT '教育水平',\n" +
                "  `weight` varchar(20) DEFAULT '' COMMENT '体重',\n" +
                "  `income` tinyint(3) DEFAULT NULL COMMENT '收入情况',\n" +
                "  `forgetrnd` varchar(45) DEFAULT NULL,\n" +
                "  `forgettime` int(11) DEFAULT NULL,\n" +
                "  `istj` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否系统推荐用户',\n" +
                "  `isfactory` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否代工厂',\n" +
                "  `isdesign` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否设计师',\n" +
                "  `isadmin` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否后台管理员',\n" +
                "  `isfirstlogin` tinyint(2) NOT NULL DEFAULT '0' COMMENT '是否初次登录',\n" +
                "  `updatetime` int(11) DEFAULT NULL COMMENT '更新时间',\n" +
                "  `createtime` int(11) DEFAULT NULL,\n" +
                "  PRIMARY KEY (`id`)\n" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COMMENT='会员表、此表信息不经常变动。'\n";
        MysqlScanner scanner = new MysqlScanner(sql.toCharArray());
        CreateTableParser parser = new CreateTableParser(scanner);
        parser.parseCreateTable();
    }
}