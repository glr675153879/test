package com.hscloud.hs.cost.account.service.imputation.monitor;

import com.hscloud.hs.cost.account.constant.ChangeModelConstant;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationTableEnum;
import com.hscloud.hs.cost.account.constant.enums.second.InputType;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.service.monitor.DataChangeMonitor;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectDetailService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 科室二次分配变更日志
 *
 * @author lian
 * @date 2024/5/20 9:40
 */

@Slf4j
@Component
public class SecDetailItemChangeService implements DataChangeMonitor {

    @Resource
    private  IUnitTaskProjectDetailService unitTaskProjectDetailService;

    @Resource
    private RemoteUserService remoteUserService;

    private static final String UPDATE_DETAIL = "\"%s\"" + "的\"%s\"数量由\"%.2f\"改为\"%.2f\"" + ",分值由\"%.2f\"改为\"%.2f\""
            + ",合计分值由\"%.2f\"改为\"%.2f\"";


    @Override
    public String getTableName() {
        return ImputationTableEnum.SEC_UNIT_TASK_DETAIL_ITEM.getCode();
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> after = record.getAfter();
        Map<String, Object> before = record.getBefore();
        if(Objects.nonNull(after) && Objects.nonNull(before)) {
            List<UnitTaskProjectDetailVo> progProjectIdList = unitTaskProjectDetailService.userList(Long.valueOf(after.get("unit_task_project_id").toString()));
            HashMap<String,String> hashMapUser = new HashMap<>();
            progProjectIdList.forEach(detail -> hashMapUser.put(detail.getEmpCode(), detail.getEmpName()));
            log.info("before: {}, after: {}", before, after);
            ImputationChangeLog imputationChangeLog = new ImputationChangeLog();
            imputationChangeLog.setChangeTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            imputationChangeLog.setChangeModel(ChangeModelConstant.UNIT_TASK_DETAIL_ITEM);

            // 检查 before 中的 input_type 字段是否为 null
            String inputType = "";
            Object inputTypeObj = before.get("input_type");
            if (Objects.nonNull(inputTypeObj)) {
                inputType =  inputTypeObj.toString();
            }
            // ifUpdate内值发生变化并且指标为"系统采集"
            if (DataOpEnum.UPDATE.getCode().equals(handleType) && ifUpdate(after, before) &&
                    Objects.nonNull(inputTypeObj) && inputType.contains(InputType.auto.toString())) {
                imputationChangeLog.setTenantId(Long.valueOf(after.get("tenant_id") + ""));
                imputationChangeLog.setCreateBy(CommonUtils.getNullOrObject(after.get("update_by")));
                Long imputationId = Long.parseLong(after.get("prog_project_detail_id") + "");
                imputationChangeLog.setChangeUserName(CommonUtils.getNullOrObject(after.get("update_by")));
                imputationChangeLog.setImputationId(imputationId);
                imputationChangeLog.setChangeItem(after.get("user_name") + "");
                imputationChangeLog.setChangeType(DataOpEnum.UPDATE.getDesc());
                BigDecimal beforePoint = new BigDecimal(before.get("point").toString()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal afterPoint = new BigDecimal(after.get("point").toString()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal beforePrice = new BigDecimal(before.get("price_value").toString()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal afterPrice = new BigDecimal(after.get("price_value").toString()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal beforeAmt = new BigDecimal(before.get("amt").toString()).setScale(2, RoundingMode.HALF_UP);
                BigDecimal afterAmt = new BigDecimal(after.get("amt").toString()).setScale(2, RoundingMode.HALF_UP);

                String format = String.format(UPDATE_DETAIL, hashMapUser.get(after.get("emp_code").toString()), after.get("name"),
                        beforePoint.doubleValue(),
                        afterPoint.doubleValue(),
                        beforePrice.doubleValue(),
                        afterPrice.doubleValue(),
                        beforeAmt.doubleValue(),
                        afterAmt.doubleValue());
                imputationChangeLog.setChangeDesc(format);
                imputationChangeLog.setChangeItem(after.get("account_item_name") + "");
                R<UserVO> details = remoteUserService.details(Long.valueOf(imputationChangeLog.getChangeUserName()), SecurityConstants.FROM_IN);
                if(Objects.nonNull(details) && Objects.nonNull(details.getData())){
                    imputationChangeLog.setChangeUserNameText(details.getData().getName());
                }
                imputationChangeLog.insert();
            }
        }
        return null;
    }

    public boolean ifUpdate(Map<String, Object> after, Map<String, Object> before) {
        // 检查特定字段是否发生变化
        List<String> fieldsToCheck = Arrays.asList("point", "price_value", "amt");
        for (String field : fieldsToCheck) {
            if (!Objects.equals(after.get(field), before.get(field))) {
                return true;
            }
        }
        return false;
    }
}
