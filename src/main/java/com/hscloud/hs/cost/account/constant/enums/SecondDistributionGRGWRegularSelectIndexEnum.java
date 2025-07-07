package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 个人岗位固定的选择指标信息
 * @author banana
 * @create 2023-11-27 17:15
 */
@Getter
public enum SecondDistributionGRGWRegularSelectIndexEnum {

    YCFPJXJE("一次分配绩效金额", "ZBX01"),
    FFDYGWFPXSH("发放单元岗位分配系数和", "ZBX02"),
    GRGWXS("个人岗位系数", "ZBX03"),
    CQL("出勤率", "ZBX04");

    private String label;

    private String value;

    SecondDistributionGRGWRegularSelectIndexEnum(String label, String value){
        this.label = label;
        this.value = value;
    }

}
