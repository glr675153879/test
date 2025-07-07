package com.hscloud.hs.cost.account.model.dto.kpi;

import com.alibaba.fastjson.JSON;
import lombok.Data;

import java.util.List;

/**
 * @Classname KpiUserCalculationRuleDto
 * @Description TODO
 * @Date 2025/1/6 16:57
 * @Created by sch
 */
@Data
public class KpiHsUserRuleDto {

//    {
//        "accountUnit": "核算单元ID", // '0' 是独立核算单元
//            "accountUnitName": "核算单元名称", // 中文
//            "accountGroup": "核算组别",
//            "accountGroupName": "核算组别", // 中文,
//            "jobNature": "工作性质编码",
//            "jobNatureName": "工作性质", // 中文
//            "duties": "职务编码",
//            "dutiesName": "职务名称", // 中文
//            "reward": "是否拿奖金", // 0 - 否 1 - 是
//    }

    private String accountUnit;

    private String accountUnitName;

    private String accountGroup;

    private String accountGroupName;

    private String jobNature;

    private String jobNatureName;

    private String duties;

    private String dutiesName;

    private String reward;

    public static void main(String[] args)
    {
        String a ="{\n" +
                "  \"accountUnit\": \"核算单元ID\"," +
                "  \"accountUnitName\": \"核算单元名称\", \n" +
                "  \"accountGroup\": \"核算组别\",\n" +
                "  \"accountGroupName\": \"核算组别\", // 中文,\n" +
                "  \"jobNature\": \"工作性质编码\",\n" +
                "  \"jobNatureName\": \"工作性质\", // 中文\n" +
                "  \"duties\": \"职务编码\",\n" +
                "  \"dutiesName\": \"职务名称\", // 中文\n" +
                "  \"reward\": \"是否拿奖金\", // 0 - 否 1 - 是\n" +
                "}";
        KpiHsUserRuleDto kpiUserCalculationRuleDto = JSON.parseObject(a, KpiHsUserRuleDto.class);
        System.out.println("1");
    }

}
