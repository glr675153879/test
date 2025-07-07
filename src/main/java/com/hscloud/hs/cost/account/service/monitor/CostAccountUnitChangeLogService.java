package com.hscloud.hs.cost.account.service.monitor;

import com.hscloud.hs.cost.account.constant.OperationTypeConstants;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.model.entity.imputation.CostUnitChangeLog;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 16:56
 **/
@Component
public class CostAccountUnitChangeLogService implements DataChangeMonitor {
    private final static String ADD = "新增科室单元\"%s\"";
    private final static String UPDATE = "科室单元名称由\"%s\"变更为\"%s\"";

    private final static String ENABLE = "启用科室单元\"%s\"";
    private final static String DISABLE = "停用科室单元\"%s\"";

    private final static String DELETE = "删除科室单元\"%s\"";

    @Override
    public String getTableName() {
        return ImputationTableEnum.COST_ACCOUNT_UNIT.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> before = record.getBefore();
        Map<String, Object> after = record.getAfter();
        CostUnitChangeLog costUnitChangeLog = new CostUnitChangeLog();
        costUnitChangeLog.setType(0);
        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            costUnitChangeLog.setOperationType(OperationTypeConstants.ADD);
            costUnitChangeLog.setOperateItem(after.get("name") + "");
            costUnitChangeLog.setOperationTime(LocalDateTime.now());
            costUnitChangeLog.setDescription(String.format(ADD, after.get("name")));
            costUnitChangeLog.setOperatorId(after.get("create_by") + "");
            costUnitChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            costUnitChangeLog.insert();

        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            String beforeName = before.get("name") + "";
            String afterName = after.get("name") + "";

            String beforeStatus = before.get("status") + "";
            String afterStatus = after.get("status") + "";

            costUnitChangeLog.setOperatorId(after.get("update_by") + "");
            costUnitChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            costUnitChangeLog.setOperateItem(afterName);
            costUnitChangeLog.setOperationTime(LocalDateTime.now());
            //变更
            if (!beforeName.equals(afterName)) {
                costUnitChangeLog.setOperationType(OperationTypeConstants.UPDATE);
                costUnitChangeLog.setDescription(String.format(UPDATE, beforeName, afterName));

            }
            //停用、启用
            if (!Objects.equals(beforeStatus, afterStatus)) {
                if (Objects.equals("0", beforeStatus) && Objects.equals("1", afterStatus)) {
                    costUnitChangeLog.setOperationType(OperationTypeConstants.DISABLE);
                    costUnitChangeLog.setDescription(String.format(DISABLE, afterName));
                } else if (Objects.equals("1", beforeStatus) && Objects.equals("0", afterStatus)) {
                    costUnitChangeLog.setOperationType(OperationTypeConstants.ENABLE);
                    costUnitChangeLog.setDescription(String.format(ENABLE, afterName));
                }
            }

            //删除
            if (Objects.equals("1", after.get("del_flag"))) {
                costUnitChangeLog.setOperationType(OperationTypeConstants.DELETE);
                costUnitChangeLog.setDescription(String.format(DELETE, afterName));
            }
            costUnitChangeLog.insert();
        }
        return null;
    }
}
