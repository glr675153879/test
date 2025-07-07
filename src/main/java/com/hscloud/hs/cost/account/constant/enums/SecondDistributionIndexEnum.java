package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 二次分配核算指标/方案配置字典项
 * @author banana
 * @create 2023-11-20 15:40
 */
@Getter
public enum SecondDistributionIndexEnum {

    KNGLJX("{\"label\":\"科内管理绩效\",\"value\":\"KNGLJX\"}", "科内管理绩效", "KNGLJX","1"),
    DXJX("{\"label\":\"单项绩效\",\"value\":\"DXJX\"}", "单项绩效", "DXJX","2"),
    GRZCXS("{\"label\":\"个人职称绩效\",\"value\":\"GRZCJX\"}", "个人职称绩效", "GRZCXS","2"),
    GZLJX("{\"label\":\"工作量绩效\",\"value\":\"GZLJX\"}", "工作量绩效", "GZLJX","2"),
    PJFPJX("{\"label\":\"平均分配绩效\",\"value\":\"PJFPJX\"}", "平均分配绩效", "PJFPJX","2");

    private String item;

    private String type;

    private String label;

    //界面类型: 管理类型1 公式类型2
    private String tag;

    SecondDistributionIndexEnum(String item, String label, String type, String tag) {
        this.item = item;
        this.label = label;
        this.type = type;
        this.tag = tag;
    }


}
