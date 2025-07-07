package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountIndexQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 核算指标表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-09-04
 */
@Mapper
public interface CostAccountIndexMapper extends BaseMapper<CostAccountIndex> {
    String selectNameById(@Param("id") Long id);

    IPage<CostAccountIndex> listByQueryDto(Page page,@Param("query")CostAccountIndexQueryDto queryDto);
}
