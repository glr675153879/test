package com.hscloud.hs.cost.account.service.monitor;

import com.alibaba.fastjson.JSON;
import com.hscloud.hs.cost.account.model.entity.CostDataChangeRecord;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import com.pig4cloud.pigx.common.data.monitor.pojo.ChangeData;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;

/**
 * @author Admin
 */
@Component
public class SysDeptDataChangeService implements DataChangeMonitor {


    @Override
    public String getTableName() {
        return "sys_dept";
    }

    @Override
    public Map<String, Object> dealDataChange(String handleType, ChangeData record) {
        Map<String, Object> before = record.getBefore();
        Map<String, Object> after = record.getAfter();
        if (DataOpEnum.CREATE.getCode().equals(handleType)) {
            // 新增
            CostDataChangeRecord costDataChangeRecord = new CostDataChangeRecord();
            costDataChangeRecord.setChangeTime(LocalDateTime.now());
            costDataChangeRecord.setChangeType("新增");
            costDataChangeRecord.setChangeSource("系统");
            costDataChangeRecord.setChangeAfter(JSON.toJSONString(after));
            costDataChangeRecord.setChangeItem("核算科室");
            costDataChangeRecord.setChangeDesc("新增核算科室"+after.get("name"));
            costDataChangeRecord.insert();

        } else if (DataOpEnum.UPDATE.getCode().equals(handleType)) {
            // 科室状态变更
            CostDataChangeRecord costDataChangeRecord = new CostDataChangeRecord();
            if (Objects.nonNull(after.get("status")) && !Objects.equals(before.get("status"),after.get("status"))) {
                String status = "0".equals(after.get("status"))?"停用":"启用";
                costDataChangeRecord.setChangeTime(LocalDateTime.now());
                costDataChangeRecord.setChangeType(status) ;
                costDataChangeRecord.setChangeSource("系统");
                costDataChangeRecord.setChangeBefore(JSON.toJSONString(before));
                costDataChangeRecord.setChangeAfter(JSON.toJSONString(after));
                costDataChangeRecord.setChangeItem("核算科室");
                costDataChangeRecord.setChangeDesc(status+"核算科室"+after.get("name"));
                costDataChangeRecord.insert();
            }
        } else if (DataOpEnum.DELETE.getCode().equals(handleType)) {
            // 删除 暂无逻辑
        }
        return null;
    }


}
