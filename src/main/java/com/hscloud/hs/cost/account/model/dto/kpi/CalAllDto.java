package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexCopy;
import lombok.Data;

import java.util.List;

@Data
public class CalAllDto {
    //子方案code
    private String planCode;
    //周期
    private Long period=-1L;
    //指标
    private KpiIndexCopy index;
    //分摊人标记 true为人
    private boolean alloEmpFlag;
    private KpiAllocationRuleCaDto alloRule;
    //指标的适用对象
    private Long memberId;
    //大json的每个小项
    private KpiFormulaDto2.FieldListDTO param;
    //json里的多条件指标
    private List<KpiFormulaDto2.ConditionListDTO> conditions;
    //json对象
    private KpiFormulaDto2 formulaDto;
    //是否归集
    private boolean isTmp;
    //归集规则
    private String impCode;
    //归集科室id
    private Long impDeptId;
    //父归集
    private String parent_impCode;

    //人口径是否需要计算科室
    private boolean needUserDept = false;
    private Long userDeptId;
}
