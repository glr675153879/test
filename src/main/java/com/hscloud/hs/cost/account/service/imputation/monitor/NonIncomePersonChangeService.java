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
 * @Description：不计入收入人员变更日志
 * @date 2024/4/26 11:01
 */
@Slf4j
@Component
public class NonIncomePersonChangeService implements DataChangeMonitor {
    private static final String ADD = "新增科室单元/人为\"%s\",需计入收入人员、归集指标分别为\"%s\"、\"%s\"";
    private static final String UPDATE = "科室单元/人\"%s\",需计入收入人员、归集指标分别由\"%s\"、\"%s\"变更为\"%s\"、\"%s\"";
    private static final String DELETE = "删除科室单元/人\"%s\"";

    @Autowired
    private IImputationService imputationService;

    @Override
    public String getTableName() {
        return ImputationTableEnum.IM_NON_INCOME_PERSON.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> after = record.getAfter();
        Map<String, Object> before = record.getBefore();
        log.info("before: {}, after: {}", before, after);
        ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
        imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        imputationChangeLog.setChangeModel(ChangeModelConstant.NON_INCOME_PERSON);

        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("create_by")));
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("create_by")));
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            imputationChangeLog.setChangeItem(after.get("dept_or_user_name") + "");
            imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
            imputationChangeLog.setChangeDesc(String.format(ADD, after.get("dept_or_user_name"), CommonUtils.getNullOrObject(after.get("need_income_persons")), after.get("imputation_index_names")));
        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            imputationChangeLog.setChangeItem(after.get("dept_or_user_name") + "");
            imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
            if (!Objects.equals(before.get("del_flag"), after.get("del_flag"))) {
                imputationChangeLog.setChangeType(DataOpEnum.DELETE.getDesc());
                imputationChangeLog.setChangeDesc(String.format(DELETE, before.get("dept_or_user_name")));
            } else {
                imputationChangeLog.setChangeDesc(String.format(UPDATE, after.get("dept_or_user_name"), CommonUtils.getNullOrObject(before.get("need_income_persons")), before.get("imputation_index_names"),
                        CommonUtils.getNullOrObject(after.get("need_income_persons")), after.get("imputation_index_names")));
            }

        }
        imputationChangeLog.insert();
        return null;
    }
}
