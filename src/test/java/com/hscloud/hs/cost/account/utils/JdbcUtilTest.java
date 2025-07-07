package com.hscloud.hs.cost.account.utils;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JdbcUtilTest {

    @Autowired
    private JdbcUtil jdbcUtil;

    @Test
    void getMetaDataBySql() {
//        jdbcUtil.getMetaDataBySql("SELECT * FROM( SELECT t1.* FROM hsx.t_third_account_unit t1 LEFT JOIN hsx.t_third t2 ON t1.id = t2.id WHERE t1.id IS NOT NULL) t WHERE id IS NOT NULL LIMIT 0, 0");
        jdbcUtil.getMetaDataBySql("SELECT * FROM( SELECT t1.id AS xxxx, t1.third_id AS thirdId, t1.third_unit_code AS '中文' FROM hsx.t_third_account_unit t1 LEFT JOIN hsx.t_third t2 ON t1.id = t2.id WHERE t1.id IS NOT NULL) t WHERE xxxx IS NOT NULL LIMIT 0, 0");
    }
}