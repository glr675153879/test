package com.hscloud.hs.cost.account.service.impl.kpi;

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiUserAttendanceCopyMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCopy;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceCopyService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
* 人员考勤备份 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class KpiUserAttendanceCopyService extends ServiceImpl<KpiUserAttendanceCopyMapper, KpiUserAttendanceCopy> implements IKpiUserAttendanceCopyService {


}
