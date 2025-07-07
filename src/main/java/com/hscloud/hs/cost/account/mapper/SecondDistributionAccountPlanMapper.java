package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.SecondDistributionAccountPlan;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 二次分配方案表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@Mapper
public interface SecondDistributionAccountPlanMapper extends BaseMapper<SecondDistributionAccountPlan> {


    SecondDistributionAccountPlan getAccountPlanLasted(@Param("unitId") Long unitId);

}
