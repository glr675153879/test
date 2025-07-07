package com.hscloud.hs.cost.account.constant.enums;

import lombok.Getter;

/**
 * 金唐通用请求大数据接口类型枚举类
 * @author banana
 * @create 2023-10-19 17:46
 */
@Getter
public enum JTBigDataPlatformApiTypeEnum {

    EMPLOYEE(1, "职工信息", "sync_hosp_organizational_structure_url"),
    DEPT(2, "部门信息", "sync_hosp_organizational_structure_url"),
    TRACINGITEMS(3, "追溯物品信息", "get_tracking_system_item_url");

    private final Integer type;

    private final String desc;

    private final String url;

    JTBigDataPlatformApiTypeEnum(Integer type, String desc, String url) {
        this.type = type;
        this.desc = desc;
        this.url = url;
    }

    /**
     * 根据type获得对应的url
     * @param type
     * @return url
     */
    public static String getTypeUrl(Integer type){
        for(JTBigDataPlatformApiTypeEnum value : JTBigDataPlatformApiTypeEnum.values()){
            if(value.type.equals(type)){
                return value.url;
            }
        }
        return null;
    }
}
