package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 核算任务表(新) Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
@Mapper
public interface CostAccountTaskNewMapper extends PigxBaseMapper<CostAccountTaskNew> {

    IPage<CostAccountTaskNew> listByQueryDto(@Param("objectPage") Page objectPage,@Param("query") CostAccountTaskQueryNewDto query);
}
