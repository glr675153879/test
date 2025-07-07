package com.hscloud.hs.cost.account.service.userAttendance;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceConfig;

/**
 * 人员考勤配置表 服务接口类
 */
public interface ICostUserAttendanceConfigService extends IService<CostUserAttendanceConfig> {

    CostUserAttendanceConfig getByDt(String dt);

}
