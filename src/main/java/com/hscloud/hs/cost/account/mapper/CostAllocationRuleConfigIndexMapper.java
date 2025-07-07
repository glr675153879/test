package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAllocationRuleConfigIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 分摊规则配置项为核算指标 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-09-11
 */
@Mapper
public interface CostAllocationRuleConfigIndexMapper extends BaseMapper<CostAllocationRuleConfigIndex> {

    void removeByAllocationRuleId(Long id);

    List<CostAllocationRuleConfigIndex> getByAllocationRuleId(Long id);
}
