package com.hscloud.hs.cost.account.service.imputation.monitor;

import com.hscloud.hs.cost.account.constant.ChangeModelConstant;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
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
 * @Description：特殊归集人员变更日志
 * @date 2024/4/26 11:02
 */
@Slf4j
@Component
public class SpecialImputationPersonChangeService implements DataChangeMonitor {
    private static final String ADD_WORKLOAD = "新增姓名\"%s\",对应专家等级为\"%s\"";
    private static final String UPDATE_WORKLOAD = "姓名\"%s\"" + ",对应专家等级由\"%s\"变更为\"%s\"";
    private static final String ADD_INCOME = "新增姓名\"%s\",对应归集指标为\"%s\"";
    ;
    private static final String UPDATE_INCOME = "姓名\"%s\"" + ",对应归集指标由\"%s\"变更为\"%s\"";
    private static final String DELETE = "删除姓名\"%s\"";
    @Autowired
    private IImputationService imputationService;

    @Override
    public String getTableName() {
        return ImputationTableEnum.IM_SPECIAL_IMPUTATION_PERSON.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> after = record.getAfter();
        Map<String, Object> before = record.getBefore();
        log.info("before: {}, after: {}", before, after);
        ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
        imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        imputationChangeLog.setChangeModel(ChangeModelConstant.SPECIAL_IMPUTATION_PERSON);

        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("create_by")));
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("create_by")));
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            imputationChangeLog.setChangeItem(after.get("user_name") + "");
            imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
            if (Objects.equals(after.get("imputation_code") + "", ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
                imputationChangeLog.setChangeDesc(String.format(ADD_WORKLOAD, after.get("user_name"), CommonUtils.getDicLabel(after.get("expert_level"))));
            } else {
                imputationChangeLog.setChangeDesc(String.format(ADD_INCOME, after.get("user_name"), after.get("imputation_index_names")));

            }
        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
            imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
            Long imputationId = Long.parseLong(after.get("imputation_id") + "");
            imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
            imputationChangeLog.setImputationId(imputationId);
            imputationService.setImputation(imputationChangeLog, imputationId);
            imputationChangeLog.setChangeItem(after.get("user_name") + "");
            imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
            if (!Objects.equals(before.get("del_flag"), after.get("del_flag"))) {
                imputationChangeLog.setChangeType(DataOpEnum.DELETE.getDesc());
                imputationChangeLog.setChangeDesc(String.format(DELETE, before.get("user_name")));
            } else {
                if (Objects.equals(after.get("imputation_code") + "", ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_WORKLOAD, after.get("user_name"), CommonUtils.getDicLabel(before.get("expert_level")), CommonUtils.getDicLabel(after.get("expert_level"))));
                } else {
                    imputationChangeLog.setChangeDesc(String.format(UPDATE_INCOME, after.get("user_name"), before.get("imputation_index_names"), after.get("imputation_index_names")));

                }
            }

        }
        imputationChangeLog.insert();
        return null;
    }
}
