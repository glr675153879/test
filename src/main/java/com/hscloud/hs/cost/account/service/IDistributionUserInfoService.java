package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.DistributionUserInfoQueryDto;
import com.hscloud.hs.cost.account.model.entity.DistributionUserInfo;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 人员信息 服务类
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
public interface IDistributionUserInfoService extends IService<DistributionUserInfo> {

    IPage<DistributionUserInfo> listUserInfo(DistributionUserInfoQueryDto dto);
}
