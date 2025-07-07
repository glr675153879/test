package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostIndexConfigItem;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.CostAccountIndexVo;

import java.util.List;

/**
 * <p>
 * 核算指标配置项 服务类
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
public interface ICostIndexConfigItemService extends IService<CostIndexConfigItem> {

    List<CostIndexConfigItem> getByIndexId(Long id);

    void deleteByIndexId(Long id);

}
