package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResultItem;
import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResultRule;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 任务执行结果-分摊规则
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Mapper
public interface CostTaskExecuteResultRuleMapper extends BaseMapper<CostTaskExecuteResultRule> {
}
