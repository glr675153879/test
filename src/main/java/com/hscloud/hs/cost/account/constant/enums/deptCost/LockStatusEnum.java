package com.hscloud.hs.cost.account.constant.enums.deptCost;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author banana
 * @create 2024-09-23 15:01
 */
@Getter
@AllArgsConstructor
public enum LockStatusEnum {

    LOCK("1", "锁定"),
    UNLOCK("0", "未锁定");

    private String val;

    private String desc;
}
