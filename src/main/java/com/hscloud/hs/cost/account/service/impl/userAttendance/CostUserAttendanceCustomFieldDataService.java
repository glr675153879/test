package com.hscloud.hs.cost.account.service.impl.userAttendance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.userAttendance.CostUserAttendanceCustomFieldDataMapper;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFieldData;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员考勤自定义字段表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostUserAttendanceCustomFieldDataService extends ServiceImpl<CostUserAttendanceCustomFieldDataMapper, CostUserAttendanceCustomFieldData> implements ICostUserAttendanceCustomFieldDataService {


}
