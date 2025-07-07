package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum TaskStatusEnum {
    S_0(0, "前置操作"),
    S_1(1, "人员锁定归集计算"),
    S_2(2, "核算项计算"),

    S_10(10, "转存数据"),
    S_11(11, "核算方案、核算子方案转存成功"),
    S_12(12,"核算单元转存成功"),
    S_13(13,"分摊公式转存成功"),
    S_14(14,"分类转存成功"),
    S_15(15,"指标转存成功"),
    S_16(16,"指标公式转存成功"),
    S_17(17,"指标公式适用对象转存成功"),
    S_18(18,"核算项结果集转存成功"),
    S_19(19,"关系表转存成功"),
    S_20(20,"人员考勤转存成功"),
    S_21(21,"归集转存成功"),
    S_22(22,"指标转存成功"),
    S_23(23,"职级系数转存成功"),
    S_24(24,"调整系数转存成功"),
    S_25(25,"人员考勤自定义字段转存成功"),
    S_26(26,"人员系数转存成功"),
    S_27(27,"字典项转存成功"),
    S_50(50,"指标计算中"),
    S_96(96,"测算异常"),
    S_97(97,"计算异常"),
    S_98(98,"计算完成，有无法计算指标"),
    S_99(99,"计算完成"),
    ;

    private final int type;
    private final String name;

    TaskStatusEnum(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static String findStr(int type) {
        for (TaskStatusEnum categoryEnum : TaskStatusEnum.values()) {
            if (categoryEnum.getType()==type) {
                return categoryEnum.getName();
            }
        }
        throw new IllegalArgumentException("未找到类型为" + type + "的枚举");
    }
}
