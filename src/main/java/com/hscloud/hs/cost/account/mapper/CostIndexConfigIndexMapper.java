package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostIndexConfigIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskIndexVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <p>
 * 核算指标配置项为核算指标 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-09-10
 */
@Mapper
public interface CostIndexConfigIndexMapper extends BaseMapper<CostIndexConfigIndex> {

    List<CostIndexConfigIndex> getByIndexId(Long id);

    void deleteByIndexId(Long id);
}
