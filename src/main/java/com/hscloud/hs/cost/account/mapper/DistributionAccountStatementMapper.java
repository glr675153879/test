package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 小小w
 * @date 2023/11/30 15:54
 */
@Mapper
public interface DistributionAccountStatementMapper extends BaseMapper<CostTaskExecuteResult> {
}
