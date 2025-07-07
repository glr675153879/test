package com.hscloud.hs.cost.account.service.imputation.monitor;

import com.hscloud.hs.cost.account.constant.ChangeModelConstant;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.constant.enums.imputation.YesNoEnum;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.service.IMaterialChargeService;
import com.hscloud.hs.cost.account.service.monitor.DataChangeMonitor;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Objects;

/**
 * 物资收费管理变更日志
 * @author  lian
 * @date  2024/6/3 9:42
 *
 */

@Slf4j
@Component
public class MaterialChargeChangeService implements DataChangeMonitor {

    @Resource
    private IMaterialChargeService materialChargeService;

    @Resource
    private RemoteUserService remoteUserService;

    private static final String UPDATE = "是否收费修改为\"%s\"";
    private static final String ADD = "新增\"%s\"";

    private static final String SWITCH = "\"%s\"\"%s\"";

    private static final String MATERIAL_CHARGE_CODE = "MATERIAL_CHARGE";
    private static final String MATERIAL_CHARGE_NAME = "物资管理";


    @Override
    public String getTableName() {
        return ImputationTableEnum.MATERIAL_CHARGE.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> after = record.getAfter();
        Map<String, Object> before = record.getBefore();
        log.info("before: {}, after: {}", before, after);
        ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
        imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        imputationChangeLog.setChangeModel(ChangeModelConstant.MATERIAL_CHARGE);
        imputationChangeLog.setImputationCode(MATERIAL_CHARGE_CODE);
        imputationChangeLog.setImputationName(MATERIAL_CHARGE_NAME);
        if(Objects.nonNull(after)) {
            if (DataOpEnum.CREATE.getCode().equals(handleType)) {
                //新增
                imputationChangeLog.setChangeType(DataOpEnum.CREATE.getDesc());
                imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
                imputationChangeLog.setCreateBy("系统");
                imputationChangeLog.setChangeUserName("系统");
                Long imputationId = Long.parseLong(after.get("id") + "");
                imputationChangeLog.setImputationId(imputationId);
                imputationChangeLog.setChangeItem(after.get("resource_name") + "");
                imputationChangeLog.setChangeDesc(String.format(ADD, after.get("resource_name")));
                imputationChangeLog.insert();
            } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
                //编辑
                imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
                imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
                imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
                imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
                Long imputationId = Long.parseLong(after.get("id") + "");
                imputationChangeLog.setImputationId(imputationId);
                imputationChangeLog.setChangeItem(after.get("resource_name") + "");
                //变更
                if(!Objects.equals(before.get("is_charge"),after.get("is_charge"))){
                    YesNoEnum isCharge = YesNoEnum.getByCode(after.get("is_charge").toString());
                    if(Objects.nonNull(isCharge)){
                        imputationChangeLog.setChangeDesc(String.format(UPDATE, isCharge.getDesc()));
                        imputationChangeLog.insert();
                    }
                }
                //停用启用
                if(!Objects.equals(before.get("status"),after.get("status"))){
                    EnableEnum statusEnum = EnableEnum.getByCode(after.get("status").toString());
                    if(Objects.nonNull(statusEnum)){
                        imputationChangeLog.setChangeDesc(String.format(SWITCH, statusEnum.getDesc(),after.get("resource_name")));
                        imputationChangeLog.insert();
                    }
                }

            }
        }
        return null;
    }
}
