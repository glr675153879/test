package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailCount;

import java.util.List;

/**
* 科室二次分配detail结果按人汇总 服务接口类
*/
public interface IUnitTaskDetailCountService extends IService<UnitTaskDetailCount> {

    List<UnitTaskDetailCount> listByDetailId(Long detailId);

    List<UnitTaskDetailCount> listByProjectId(Long projectId);
}
