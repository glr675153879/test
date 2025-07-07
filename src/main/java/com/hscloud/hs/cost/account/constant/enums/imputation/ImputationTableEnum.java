package com.hscloud.hs.cost.account.constant.enums.imputation;

import lombok.Getter;

/**
 * @author Admin
 */
@Getter
public enum ImputationTableEnum {

    /**
     * 表枚举
     */
    IM_IMPUTATION_DEPT_UNIT("im_imputation_dept_unit", "归集科室单元"),
    IM_IMPUTATION_DETAILS("im_imputation_details", "归集人员明细"),
    IM_NON_INCOME_PERSON("im_non_income_person", "不计入收入人员"),
    IM_SPECIAL_IMPUTATION_PERSON("im_special_imputation_person", "特殊归集人员"),
    SEC_UNIT_TASK_PROJECT_DETAIL("sec_unit_task_project_detail", "单项绩效"),
    SEC_UNIT_TASK_DETAIL_ITEM("sec_unit_task_detail_item", "科室二次分配"),
    MATERIAL_CHARGE("material_charge", "物资收费管理"),
    COST_ACCOUNT_UNIT("cost_account_unit", "核算单元"),
    COST_CLUSTER_UNIT("cost_cluster_unit", "归集单元");

    private final String code;

    private final String desc;

    ImputationTableEnum(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
