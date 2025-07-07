package com.hscloud.hs.cost.account.service.userAttendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.userAttendance.CostUserAttendanceCustomFieldsDto;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 人员考勤自定义字段表 服务接口类
 */
public interface ICostUserAttendanceCustomFieldsService extends IService<CostUserAttendanceCustomFields> {

    @Transactional(rollbackFor = Exception.class)
    List<CostUserAttendanceCustomFields> listByDt(String dt);

    Boolean activate(List<Long> ids);

    Boolean saveUpdate(CostUserAttendanceCustomFieldsDto customParams);

    List<CostUserAttendanceCustomFields> listGroup();

}
