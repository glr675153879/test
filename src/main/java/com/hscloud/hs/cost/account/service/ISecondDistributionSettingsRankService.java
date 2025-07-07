package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsRank;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsDegreeVo;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsRankVo;

/**
 * <p>
 * 分配设置职称绩效 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
public interface ISecondDistributionSettingsRankService extends IService<SecondDistributionSettingsRank> {

    Boolean saveRank(SecondDistributionSettingsRank settingsRank);

    Boolean updateStatus(EnableDto dto);

    Page<SecondDistributionSettingsRankVo> getList(SecondQueryDto queryDto);
}
