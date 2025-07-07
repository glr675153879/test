package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTask;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface CostAccountTaskMapper extends PigxBaseMapper<CostAccountTask> {

    IPage<CostAccountTask> listByQueryDto(Page page, @Param("query") CostAccountTaskQueryDto queryDto);

}
