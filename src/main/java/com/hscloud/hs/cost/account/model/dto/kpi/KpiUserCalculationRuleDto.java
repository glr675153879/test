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
public class KpiUserCalculationRuleDto {

    //{
    //  "rangeType": "filter", // range | filter
    //  "range": {
    //      "paramType": "15",
    //      "paramValues": [{
    //          "value": "外包",
    //          "label": "外包"
    //      }, {
    //          "value": "测试",
    //          "label": "测试"
    //      }]
    //  },
    //  "filter": [{
    //    "key": "gzxz",
    //    "value": [{
    //        "value": "外包",
    //        "label": "外包"
    //    }, {
    //        "value": "测试",
    //        "label": "测试"
    //    }]
    //  }],
    //  "mapValues": [{
    //    "type": "system",
    //    "value": "0",
    //    "label": "是否拿奖金",
    //    "code": "reward"
    //    }, {
    //    "type": "system",
    //    "value": "0",
    //    "label": "奖金系数",
    //    "code": "rewardIndex"
    //  }]
    //}

    private String rangeType;

    private KpiUserCalculationRuleRangeDto range;

    private List<KpiUserCalculationRuleFilterDto>  filter;

    private List<KpiUserCalculationRuleValueDto> mapValues;

    private List<Long> userIdsList;

    public static void main(String[] args)
    {
        String a ="{\n" +
                "    \"rangeType\": \"filter\",\n" +
                "    \"range\": {\n" +
                "        \"paramType\": \"15\",\n" +
                "        \"paramValues\": [\n" +
                "            {\n" +
                "                \"value\": \"外包\",\n" +
                "                \"label\": \"外包\"\n" +
                "            },\n" +
                "            {\n" +
                "                \"value\": \"测试\",\n" +
                "                \"label\": \"测试\"\n" +
                "            }\n" +
                "        ]\n" +
                "    },\n" +
                "    \"filter\": [\n" +
                "        {\n" +
                "            \"key\": \"gzxz\",\n" +
                "            \"value\": [\n" +
                "                {\n" +
                "                    \"value\": \"外包\",\n" +
                "                    \"label\": \"外包\"\n" +
                "                },\n" +
                "                {\n" +
                "                    \"value\": \"测试\",\n" +
                "                    \"label\": \"测试\"\n" +
                "                }\n" +
                "            ]\n" +
                "        }\n" +
                "    ],\n" +
                "    \"mapValues\": [\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"0\",\n" +
                "            \"label\": \"是否拿奖金\",\n" +
                "            \"code\": \"reward\"\n" +
                "        },\n" +
                "        {\n" +
                "            \"type\": \"system\",\n" +
                "            \"value\": \"0\",\n" +
                "            \"label\": \"奖金系数\",\n" +
                "            \"code\": \"rewardIndex\"\n" +
                "        }\n" +
                "    ]\n" +
                "}";
        KpiUserCalculationRuleDto kpiUserCalculationRuleDto = JSON.parseObject(a, KpiUserCalculationRuleDto.class);
        System.out.println("1");
    }

}
