package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskSubmitDto;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskUnitInfo;
import com.hscloud.hs.cost.account.model.pojo.SecondDistributionTaskUnitDetail;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveRecordVo;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceApproveDto;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceRejectDto;
import com.hscloud.hs.oa.workflow.api.vo.ProcessInstanceVo;
import com.pig4cloud.pigx.common.core.util.R;

/**
 * <p>
 * 二次分配任务和科室单元关联表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-23
 */
public interface ISecondDistributionTaskUnitInfoService extends IService<SecondDistributionTaskUnitInfo> {

    /**
     * 提交审核
     * @param taskSubmitDto
     * @return
     */
    Object create(SecondDistributionTaskSubmitDto taskSubmitDto);

    /**
     * 待我审核列表
     * @return
     */
    com.pig4cloud.pigx.common.core.util.R todo(SecondDistributionTaskApproveQueryDto approveQueryDto);

    /**
     * 审核中列表
     *
     * @param approveQueryDto 参数
     * @return 审核中列表
     */
    com.pig4cloud.pigx.common.core.util.R listApproving(SecondDistributionTaskApproveQueryDto approveQueryDto);

    /**
     *审核通过列表
     * @param approveQueryDto
     * @return
     */
    com.pig4cloud.pigx.common.core.util.R listPassed(SecondDistributionTaskApproveQueryDto approveQueryDto);

    /**
     *审核驳回列表
     * @param approveQueryDto
     * @return
     */
    com.pig4cloud.pigx.common.core.util.R listReject(SecondDistributionTaskApproveQueryDto approveQueryDto);

    /**
     * 审核任务详情
     * @param id
     * @return
     */
    ProcessInstanceVo processDetail(String id);

    /**
     * 未提交列表
     * @param approveQueryDto
     * @return
     */
    R unCommit(SecondDistributionTaskApproveQueryDto approveQueryDto);

    /**
     * 获取分配审核列表
     * @param dto
     * @return
     */
    Page<SecondDistributionTaskApproveRecordVo> getList(SecondTaskApprovingRecordQueryDto dto);


    /**
     * 审核通过
     * @param processInstanceApproveDto
     * @return
     */
    com.pig4cloud.pigx.common.core.util.R approve(ProcessInstanceApproveDto processInstanceApproveDto);

    /**
     * 审核驳回
     * @param processInstanceRejectDto
     * @return
     */
    com.pig4cloud.pigx.common.core.util.R reject(ProcessInstanceRejectDto processInstanceRejectDto);

    SecondDistributionTaskUnitDetail getTaskUnitInfoById(Long id);
}
