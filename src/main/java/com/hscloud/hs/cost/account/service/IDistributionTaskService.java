package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.TaskResultQueryDto;
import com.hscloud.hs.cost.account.model.pojo.AdsCostShareTempNew;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskResultDetailVo;

public interface IDistributionTaskService extends IService<AdsCostShareTempNew> {

    CostAccountTaskResultDetailVo listResult(TaskResultQueryDto taskResultQueryDto);
}
