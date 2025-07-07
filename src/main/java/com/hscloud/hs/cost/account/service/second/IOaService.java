package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.SecondTaskApprovingRecordQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskApproveQueryDto;
import com.hscloud.hs.cost.account.model.dto.second.TaskSubmitDto;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionTaskApproveRecordVo;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceApproveDto;
import com.hscloud.hs.oa.workflow.api.dto.ProcessInstanceRejectDto;
import com.hscloud.hs.oa.workflow.api.dto.process.ProcessFormChangeDto;
import com.pig4cloud.pigx.common.core.util.R;

/**
 * @author 小小w
 * @date 2024/3/9 11:46
 */
public interface IOaService extends IService<UnitTask> {

    Object create(TaskSubmitDto taskSubmitDto);

    R approve(ProcessInstanceApproveDto processInstanceApproveDto);

    Object processDetail(String id);

    R reject(ProcessInstanceRejectDto processInstanceRejectDto);

    Page<SecondDistributionTaskApproveRecordVo> getList(SecondTaskApprovingRecordQueryDto dto);

    R unCommit(TaskApproveQueryDto approveQueryDto);

    R todo(TaskApproveQueryDto approveQueryDto);

    R listApproving(TaskApproveQueryDto approveQueryDto);

    R listPassed(TaskApproveQueryDto approveQueryDto);

    R listReject(TaskApproveQueryDto approveQueryDto);

    R revoke(ProcessFormChangeDto processFormChangeDto);

    R reject1(ProcessInstanceRejectDto processInstanceRejectDto);
}
