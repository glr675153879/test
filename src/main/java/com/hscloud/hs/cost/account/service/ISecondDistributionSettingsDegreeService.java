package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsDegree;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsDegreeVo;

import java.util.List;

/**
 * <p>
 * 分配设置学位系数 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
public interface ISecondDistributionSettingsDegreeService extends IService<SecondDistributionSettingsDegree> {

    Boolean saveRank(SecondDistributionSettingsDegree settingsDegree);

    Boolean updateStatus(EnableDto dto);

    Page<SecondDistributionSettingsDegreeVo> getList(SecondQueryDto queryDto);
}
