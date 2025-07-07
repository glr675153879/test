package com.hscloud.hs.cost.account.service.imputation.monitor;

import com.hscloud.hs.cost.account.constant.ChangeModelConstant;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.hscloud.hs.cost.account.service.monitor.DataChangeMonitor;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;


/**
 * @author xiechenyu
 * @Description：归集科室人员明细变更日志
 * @date 2024/4/26 11:00
 */
@Slf4j
@Component
public class ImputationDetailsChangeService implements DataChangeMonitor {
    private static final String ADD = "新增科室单元\"%s\",对应归集人员为\"%s\"";
    private static final String UPDATE = "科室单元\"%s\",对应归集人员由\"%s\"变更为\"%s\"";
    private static final String DELETE = "删除科室单元\"%s\"";

    @Autowired
    private IImputationService imputationService;

    @Override
    public String getTableName() {
        return ImputationTableEnum.IM_IMPUTATION_DETAILS.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> after = record.getAfter();
        Map<String, Object> before = record.getBefore();
        log.info("before: {}, after: {}", before, after);
        ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
        imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        imputationChangeLog.setChangeModel(ChangeModelConstant.IMPUTATION_PERSON);

        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("create_by")));
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("create_by")));
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
            imputationChangeLog.setChangeDesc(String.format(ADD, after.get("account_unit_name"), CommonUtils.getNullOrObject(after.get("emp_names"))));
            imputationChangeLog.insert();
        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            if (Objects.equals(before.get("del_flag"), after.get("del_flag"))) {
                imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
                imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
                imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
                imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
                Long imputationId = Long.parseLong(after.get("imputation_id") + "");
                imputationChangeLog.setImputationId(imputationId);
                imputationService.setImputation(imputationChangeLog, imputationId);
                imputationChangeLog.setChangeItem(after.get("account_unit_name") + "");
                imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
                imputationChangeLog.setChangeDesc(String.format(UPDATE, after.get("account_unit_name"), CommonUtils.getNullOrObject(before.get("emp_names")), CommonUtils.getNullOrObject(after.get("emp_names"))));
                imputationChangeLog.insert();
            }
        }

        return null;
    }


}
