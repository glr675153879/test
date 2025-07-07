package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.DistributionUserInfoQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionUserInfo;
import com.hscloud.hs.cost.account.model.entity.MaterialCharge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 物资收费管理
 * @author  lian
 * @date  2024/6/2 15:08
 *
 */

@Mapper
public interface MaterialChargeMapper extends BaseMapper<MaterialCharge> {

}
