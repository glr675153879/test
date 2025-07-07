package com.hscloud.hs.cost.account.service.userAttendance;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAccountFormulaParam;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAttendanceFormula;
import com.pig4cloud.pigx.common.core.util.R;

import java.util.List;

/**
 * 一次分配考勤公式配置表 服务接口类
 */
public interface IFirstDistributionAttendanceFormulaService extends IService<FirstDistributionAttendanceFormula> {

    /**
     * 计算考勤天数
     *
     * @param id                 公式id
     * @param costUserAttendance 人员考勤数据
     * @return 考勤天数
     */
    String calculateAttendDays(Long id, CostUserAttendance costUserAttendance);

    String calculateAttendDays2(Long planId, KpiUserAttendance kpiUserAttendance, List<KpiAccountUnit> accountUnitList
            , List<FirstDistributionAttendanceFormula> firstDistributionAttendanceFormulas
    , List<FirstDistributionAccountFormulaParam> firstDistributionAccountFormulaParams, List<CostUserAttendanceCustomFields> costUserAttendanceCustomField);

    List<FirstDistributionAccountFormulaDto> formulaList(QueryWrapper<FirstDistributionAttendanceFormula> wrapper);

    R saveData(FirstDistributionAttendanceFormula firstDistributionAttendanceFormula);

    R updateData(FirstDistributionAttendanceFormula firstDistributionAttendanceFormula);

    List<FirstDistributionAttendanceFormula> listData(QueryWrapper<FirstDistributionAttendanceFormula> wrapper);
}
