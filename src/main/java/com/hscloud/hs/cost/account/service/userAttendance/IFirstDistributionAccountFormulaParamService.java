package com.hscloud.hs.cost.account.service.userAttendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaParamDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAccountFormulaParam;

import java.util.List;

/**
 * 一次分配考勤公式参数 服务接口类
 */
public interface IFirstDistributionAccountFormulaParamService extends IService<FirstDistributionAccountFormulaParam> {

    List<FirstDistributionAccountFormulaParamDto> listParam(PageRequest<FirstDistributionAccountFormulaParam> pr);
}
