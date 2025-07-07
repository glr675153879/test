package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.util.StrUtil;

/**
 * @author pc
 * @date 2025/5/13
 */
public class ServiceItemUtil {
    public static boolean checkCode(String serviceItemCode) {
        if (StrUtil.length(serviceItemCode) == 1
                || StrUtil.length(serviceItemCode) == 2
                || StrUtil.length(serviceItemCode) == 4
                || StrUtil.length(serviceItemCode) == 6) {
            return true;
        }
        return false;
    }

}
