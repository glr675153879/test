package com.hscloud.hs.cost.account.service.impl.dataReport;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportTaskLogMapper;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTaskLog;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskLogService;
import com.pig4cloud.pigx.admin.api.dto.UserCoreDto;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
* 上报任务变更日志 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportTaskLogService extends ServiceImpl<CostReportTaskLogMapper, CostReportTaskLog> implements ICostReportTaskLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateLog(String opsType, String originName, CostReportTask costReportTask) {
        //日志记录
        PigxUser user = SecurityUtils.getUser();
        CostReportTaskLog costReportTaskLog = new CostReportTaskLog();
        costReportTaskLog.setOpsType(opsType);
        costReportTaskLog.setOpsItem(StringUtils.isBlank(costReportTask.getTaskName()) ? originName : costReportTask.getTaskName());
        costReportTaskLog.setOpsTime(LocalDateTime.now());
        costReportTaskLog.setOpsById(user.getId());
        UserCoreDto userCoreDto = new UserCoreDto();
        userCoreDto.setUserId(user.getId());
        costReportTaskLog.setOpsBy(user.getName());
        costReportTaskLog.setJobNumber(user.getJobNumber());
        costReportTaskLog.setType(costReportTask.getType());

        // 上报任务日志描述描述处理
        String description = "";
        OpsTypeEnum opsTypeEnum = OpsTypeEnum.getByVal(opsType);
        switch (opsTypeEnum) {
            case ADD:
                description = "新增上报任务 " +  costReportTask.getTaskName();
                break;
            case UPDATE:
                description = "上报任务由 " + originName + " 变更为 " + costReportTask.getTaskName();
                break;
            case DEL:
                description = "删除上报任务 " +  costReportTask.getTaskName();
                break;
            case ENABLE:
                description = "上报任务 " +  originName + (costReportTask.getStatus().equals("0")?" 启用":"停用");
                break;
            default:
                break;
        }
        costReportTaskLog.setDescription(description);
        save(costReportTaskLog);
        System.out.println("日志生成...");
    }
}
