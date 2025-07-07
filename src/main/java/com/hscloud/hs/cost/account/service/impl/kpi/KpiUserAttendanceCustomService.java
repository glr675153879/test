package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserAttendanceCustomMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCustom;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceCustomService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 人员考勤自定义字段表 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiUserAttendanceCustomService extends ServiceImpl<KpiUserAttendanceCustomMapper, KpiUserAttendanceCustom> implements IKpiUserAttendanceCustomService {


}
