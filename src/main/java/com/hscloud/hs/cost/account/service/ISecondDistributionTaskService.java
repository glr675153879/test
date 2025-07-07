package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionManagementQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskDistributionDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTask;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionManagementVo;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskVo;
import com.hscloud.hs.cost.account.model.vo.SecondTaskDistributionDetailVo;

import javax.servlet.http.HttpServletResponse;

/**
 * <p>
 * 二次分配任务表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskService extends IService<SecondDistributionTask> {

    Page<SecondDistributionTaskVo> getTaskAllocationPreview(SecondTaskQueryDto queryDto);

    /**
     * 获取审核列表
     * @param queryDto
     * @return
     */
    Page<SecondDistributionManagementVo> getList(SecondDistributionManagementQueryDto queryDto);


    SecondTaskDistributionDetailVo getTaskAllocationDetail(SecondTaskDistributionDto secondTaskDistributionDto);

    void export(HttpServletResponse response, SecondTaskDistributionDto secondTaskDistributionDto);
}
