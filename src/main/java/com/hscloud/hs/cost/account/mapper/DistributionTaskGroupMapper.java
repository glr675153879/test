package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.DistributionTaskGroupQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 任务分组 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Mapper
public interface DistributionTaskGroupMapper extends BaseMapper<DistributionTaskGroup> {

    IPage<DistributionTaskGroup> listByQueryDto(@Param("dto") DistributionTaskGroupQueryDto dto, @Param("page")Page page);
}
