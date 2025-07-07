package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 可选指标字典
 * @author banana
 * @create 2023-11-28 16:28
 */
@Getter
public enum SecondDistributionSelectIndexDictTypeEnum {

    GRGWJX("grgwjx_index", "个人岗位绩效"),

    PJJX("pjjx_index", "平均绩效"),

    DOCPJJX("doc_pjjx_index", "医生组平均绩效"),

    NURSEPJJX("nurse_pjjx_index", "护理组平均绩效"),

    YJPJJX("yj_pjjx_index", "医技组平均绩效"),

    ADMINPJJX("admin_pjjx_index", "行政组平均绩效"),

    OTHERJJX("other_pjjx_index", "其他组平均绩效指标项");

    private String type;

    private String label;

    SecondDistributionSelectIndexDictTypeEnum(String type, String label){
        this.type = type;
        this.label = label;
    }
}
