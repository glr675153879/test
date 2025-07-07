package com.hscloud.hs.cost.account.service.impl.dataReport;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.dataReport.OpsTypeEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportItemLogMapper;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItemLog;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemLogService;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
* 上报项变更日志 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportItemLogService extends ServiceImpl<CostReportItemLogMapper, CostReportItemLog> implements ICostReportItemLogService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateLog(String opsType, String originName, CostReportItem costReportItem) {
        PigxUser user = SecurityUtils.getUser();
        //日志记录
        CostReportItemLog costReportItemLog = new CostReportItemLog();
        costReportItemLog.setOpsType(opsType);
        costReportItemLog.setOpsItem(StringUtils.isBlank(costReportItem.getName()) ? originName : costReportItem.getName());
        costReportItemLog.setOpsTime(LocalDateTime.now());
        costReportItemLog.setOpsById(user.getId());
        costReportItemLog.setOpsBy(user.getName());
        costReportItemLog.setJobNumber(user.getJobNumber());
        costReportItemLog.setType(costReportItem.getType());

        // 上报项描述描述处理
        String description = "";
        OpsTypeEnum opsTypeEnum = OpsTypeEnum.getByVal(opsType);
        switch (opsTypeEnum) {
            case ADD:
                description = "新增上报项 " +  costReportItem.getName();
                break;
            case UPDATE:
                description = "上报项说明由 " + originName + " 变更为 " + costReportItem.getName();
                break;
            case DEL:
                description = "删除上报项 " +  costReportItem.getName();
                break;
            case ENABLE:
                description = "上报项 " +  originName + (costReportItem.getStatus().equals("0")?" 启用":"停用");
                break;
            default:
                break;
        }
        costReportItemLog.setDescription(description);

        save(costReportItemLog);
    }
}
