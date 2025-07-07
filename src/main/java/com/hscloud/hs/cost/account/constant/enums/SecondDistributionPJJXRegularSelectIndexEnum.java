package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * @author banana
 * @create 2023-11-30 19:47
 */
@Getter
public enum  SecondDistributionPJJXRegularSelectIndexEnum {

    YCFPJXJE("一次分配绩效金额", "ZBX01", null),
    FFDWZRS("发放单位总人数", "ZBX02", null),
    KZRRS("科主任人数","ZBX03", "科主任"),
    FKZRRS("副科主任人数", "ZBX04","副科主任"),
    JSGWRS("技术顾问人数", "ZBX05", "技术顾问"),
    HSZRW("护士长人数", "ZBX06", "护士长"),
    FHSZRWW("副护士长人数", "ZBX07", "副护士长");

    private String label;

    private String value;

    private String title;

    SecondDistributionPJJXRegularSelectIndexEnum(String label, String value, String title){
        this.label = label;
        this.value = value;
        this.title = title;
    }
}
