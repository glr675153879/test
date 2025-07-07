package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 指标项的计算方式类型
 * @author banana
 * @create 2023-11-25 12:06
 */
@Getter
public enum SecondDistributionFormulaParamCalTypeEnum {


/*
    FPSZ1("1", "分配设置类型1（有值，存id）", "#"),  //id

    FPSZ2("2", "分配设置类型2（无值，数据小组获取）", "#"),   //#：表示在计算时赋值

    FPSZ3("3", "分配设置类型3（无值，自己输）", "#"),      //#：表示在计算时赋值

    ZD("4", "按照字典处理", "#"),           //#为字典项

    INDEX("5", "核算指标类型", "#"),   //#：表示在计算时赋值

    SJXZ("6", "数据小组获取", "#"),    //#



*/

    FPSZ("1", "分配设置类型（计算时，手动输入）", "id"),

    GDZDZB("2", "固定字典指标", "字典vlue"),

    QZHSZBZ("3", "前置核算指标值", "id");

    private String type;

    private String desc;

    private String value;

    SecondDistributionFormulaParamCalTypeEnum(String type, String desc, String value){
        this.type = type;
        this.desc = desc;
        this.value = value;
    }

}
