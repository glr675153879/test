package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskVo;

/**
 * <p>
 * 核算任务表(新) 服务类
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
public interface ICostAccountTaskNewService extends IService<CostAccountTaskNew> {

    IPage<CostAccountTaskNew> listAccountTaskNew(CostAccountTaskQueryNewDto dto);
}
