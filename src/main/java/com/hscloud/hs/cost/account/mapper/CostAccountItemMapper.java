package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountItem;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Admin
 */
@Mapper
public interface CostAccountItemMapper extends PigxBaseMapper<CostAccountItem> {

    IPage<CostAccountItem> listByQueryDto(Page page, @Param("query") CostAccountItemQueryDto costAccountItemQueryDto);
}
