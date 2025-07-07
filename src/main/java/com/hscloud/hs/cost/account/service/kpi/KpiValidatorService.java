package com.hscloud.hs.cost.account.service.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.KpiValidatorDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * 校验类
 * @author Administrator
 */
public interface KpiValidatorService {

    /**
     * 核算项校验
     * @param dto 入参 type为口径颗粒度或者上报项id
     * @param changeFlag 是否转换数据
     * @return 校验结果
     */
    ValidatorResultVo itemValidator(KpiValidatorDTO dto, Boolean changeFlag, List<KpiMember> members, List<KpiAccountUnit> units);

    HashMap<String, Object> getSqlInputParam(KpiValidatorDTO dto, Boolean changeFlag, List<KpiValidatorDTO.SqlValidatorParam> params);

    /**
     * 转换数据
     * @param retainDecimal 指标保留小数
     * @param carryRule 进位规则
     * @param map 入参
     * @param changeFlag 是否转换数据
     * @param busiType 业务类型
     * @return json字符串
     */
    String changeData(Integer retainDecimal, String carryRule, List<LinkedHashMap<String, Object>> map, Boolean changeFlag, String busiType);
}
