package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsManagement;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsManagementVo;

/**
 * <p>
 * 分配设置管理绩效 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-15
 */
public interface ISecondDistributionSettingsManagementService extends IService<SecondDistributionSettingsManagement> {

    /**
     * 修改状态
     * @param dto
     * @return
     */
    Boolean updateStatus(EnableDto dto);

    /**
     * 新增管理绩效
     * @param settingsManagement
     * @return
     */
    Boolean saveManagement(SecondDistributionSettingsManagement settingsManagement);

    /**
     * 查询绩效列表
     * @param queryDto
     * @return
     */
    Page<SecondDistributionSettingsManagementVo> getList(SecondQueryDto queryDto);

    /**
     * 修改管理绩效
     * @param settingsManagement
     * @return
     */
    Boolean updateManagement(SecondDistributionSettingsManagement settingsManagement);
}

