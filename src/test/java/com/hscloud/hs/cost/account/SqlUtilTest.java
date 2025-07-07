package com.hscloud.hs.cost.account;

import com.hscloud.hs.cost.account.utils.SqlUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SpringBootTest
public class SqlUtilTest {

    @Autowired
    private SqlUtil sqlUtil;

    @Test
    public void test(){

        String sql = "select sum(amount_treat) from nfjx_dw.dws_finance_inpat_fee_30d where dt between #{startTime} and #{endTime}  \n" +
                "and emp_id=(SELECT other_emp_id from nfjx_dim.dim_emp_match where emp_code=#{user_id} and type='HIS')";
        Map<String,String> map = new HashMap<>();
        map.put("startTime","202112;");
        map.put("endTime","202202-");
        map.put("user_id","1109");
        String s = sqlUtil.executeSql(sql, map);
        System.out.println(s);
    }


    @Test
    public void test2(){

        String sql = "SELECT sum(a.VALUE) FROM hsx_cost.cost_report_detail a LEFT JOIN hsx_cost.cost_report_task b ON a.task_id = b.id LEFT JOIN hsx_cost.cost_report_item c ON a.item_id = c.id where c.id=5 and CONCAT( SUBSTR(a.`year`,1,4),'0', SUBSTR(a.`month`,1,1) ) BETWEEN #{startTime} and #{endTime} and a.biz_id = (select dept_id from sys_dept where code = #{dept_code})";
        Pattern pattern = Pattern.compile("AND", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(sql);
        int lastIndex = -1;

        while (matcher.find()) {
            lastIndex = matcher.start();
        }

        if (lastIndex >= 0) {
            sql = sql.substring(0, lastIndex);
        }
        System.out.println(sql);
    }


}
