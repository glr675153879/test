package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 小小w
 * @date 2023/9/21 16:31
 */
@Mapper
public interface CostAccountStatementMapper extends BaseMapper<CostTaskExecuteResult> {

}
