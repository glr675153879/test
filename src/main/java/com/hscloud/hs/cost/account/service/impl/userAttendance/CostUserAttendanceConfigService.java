package com.hscloud.hs.cost.account.service.impl.userAttendance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.userAttendance.CostUserAttendanceConfigMapper;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceConfig;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceConfigService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员考勤配置表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostUserAttendanceConfigService extends ServiceImpl<CostUserAttendanceConfigMapper, CostUserAttendanceConfig> implements ICostUserAttendanceConfigService {

    @Override
    public CostUserAttendanceConfig getByDt(String dt) {
        return super.getOne(new LambdaQueryWrapper<CostUserAttendanceConfig>().eq(CostUserAttendanceConfig::getDt, dt));
    }
}
