package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsWorkload;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsWorkloadVo;

/**
 * <p>
 * 分配设置工作量绩效 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
public interface ISecondDistributionSettingsWorkloadService extends IService<SecondDistributionSettingsWorkload> {

    Boolean updateStatus(EnableDto dto);

    Boolean saveWorkload(SecondDistributionSettingsWorkload settingsWorkload);

    Page<SecondDistributionSettingsWorkloadVo> getList(SecondQueryDto queryDto);
}
