package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostAccountProportionRelation;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author banana
 * @create 2023-09-13 18:15
 */
@Mapper
public interface CostAccountProportionRelationMapper extends PigxBaseMapper<CostAccountProportionRelation> {

    @Select("select proportion from cost_account_proportion_relation where id=#{id}")
    String selectProportionById(@Param("id") Long id);
}
