package com.hscloud.hs.cost.account.constant.enums.kpi;

/**
 * @author Administrator
 */

public enum ItemResultEnum {
    /**
     * 序号
     */
    SEQ("seq","序号"),
    PERIOD("period","周期"),
    EMP("user_id","人员"),
    DEPT("dept_id","科室单元"),
    IMPUTATION_DEPT("imputation_dept_id","归集科室"),
    SOURCE_DEPT("source_dept","数据发生科室"),
    ZDYS("zdys","主刀医生"),
    BRKS("brks","病人科室"),
    KZYS("kzys","开医医生"),
    WARD("ward","病区"),
    VALUE("value","数值"),
    BUSINESS_CODE("busi_code","唯一码"),
    ZDYSKS("zdysks","主刀医生科室"),
    KZYSKS("kzysks","开医嘱医生科室"),
    KZYH("kzyh","开医嘱医生/护士"),
    BRBQ("brbq","病人病区"),
    PROJECT_ID("project_id","项目ID"),
    GHKB("ghkb","挂号科别"),
    ;
    private final String type;
    private final String name;

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    ItemResultEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public static ItemResultEnum findEnumByType(String type) {
        for (ItemResultEnum var1 : ItemResultEnum.values()) {
            if (var1.getType().equals(type)) {
                return var1;
            }
        }
        return null;
        //throw new IllegalArgumentException("未找到类型为【" + type + "】的枚举值");
    }

    public static boolean chargeFieldExist(String fieldName) {
        boolean flag = false;
        for (ItemResultEnum var1 : ItemResultEnum.values()){
            if (var1.getType().equals(fieldName)) {
                flag = true;
                break;
            }
        }
        return flag;
    }
}
