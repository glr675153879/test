package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsSingle;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsSingleVo;

/**
 * <p>
 * 分配设置单项绩效 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
public interface ISecondDistributionSettingsSingleService extends IService<SecondDistributionSettingsSingle> {

    Boolean updateStatus(EnableDto dto);

    Boolean saveSingle(SecondDistributionSettingsSingle settingsSingle);

    Page<SecondDistributionSettingsSingleVo> getList(SecondQueryDto queryDto);

}
