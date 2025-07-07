package com.hscloud.hs.cost.account.constant.enums.kpi;

public enum MemberEnum {

    IMPUTATION_RULE_USER("imputation_rule_user", "归集规则关联个人"),

    IMPUTATION_RULE_GROUP("imputation_rule_group", "归集规则关联群体"),


    IMPUTATION_RULE_ITEM("imputation_rule_item", "归集规则关联核算项"),

    IMPUTATION_RULE_DEPT("imputation_rule_dept", "归集规则关联科室"),

    IMPUTATION_DEPT_EMP("imputation_dept_emp", "归集分组关联科室—人"),

    USER_DEPT("user_dept", "考勤人员变动 人员-科室"),
    ROLE_EMP("role_emp", "核算人员分组"),
    ACCOUNT_UNIT_RELATION("account_unit_relation", "医护关系类型"),
    EMP_TYPE("emp_type", "核算人员类型"),

    ROLE_EMP_GROUP("role_emp_group", "分组核算人员手动分配核算单元"),

    ROLE_EMP_ZW("role_emp_zw", "分组核算人员手动分配职务"),

    ITEM_EMP("item_emp", "核算项适用人员"),
    ITEM_DEPT("item_dept", "核算项适用科室"),
    FORMULA_ITEM("formula_item", "指标公式项"),
    ALLOCATION_RULE_ITEM_X("allocation_rule_item_x", "分摊公式核算项"),
    ALLOCATION_RULE_ITEM_Z("allocation_rule_item_z", "分摊公式指标"),
    ALLOCATION_RULE_ITEM_F("allocation_rule_item_f", "分摊公式分摊指标"),
    IN_MEMBERS_EMP("in_members_emp", "摊入人员"),
    IN_MEMBERS_DEPT("in_members_dept", "摊入科室"),
    OUT_MEMBERS_IMP("out_members_imp", "摊出归集"),
    OUT_MEMBERS_EMP("out_members_emp", "摊出人员"),
    OUT_MEMBERS_DEPT("out_members_dept", "摊出科室"),
    OUT_MEMBER_DEPT_GROUP("out_member_dept_group", "摊出科室组别"),
    OUT_MEMBER_DEPT_EXCEPT("out_member_dept_except", "摊出科室剔除科室"),
    CLUSTER_DEPT("cluster_dept", "摊出归集科室关联");

    private final String type;
    private final String name;

    MemberEnum(String type, String name) {
        this.type = type;
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }
}
