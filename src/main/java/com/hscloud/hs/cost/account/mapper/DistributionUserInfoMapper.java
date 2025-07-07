package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.DistributionUserInfoQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionUserInfo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <p>
 * 人员信息 Mapper 接口
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Mapper
public interface DistributionUserInfoMapper extends BaseMapper<DistributionUserInfo> {

    IPage<DistributionUserInfo> listByQueryDto(@Param("page")Page page,@Param("dto") DistributionUserInfoQueryDto dto);
}
