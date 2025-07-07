package com.hscloud.hs.cost.account.service.impl.userAttendance;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.userAttendance.UserAttendanceLogMapper;
import com.hscloud.hs.cost.account.model.entity.userAttendance.UserAttendanceLog;
import com.hscloud.hs.cost.account.service.userAttendance.IUserAttendanceLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 人员考勤表变更日志 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserAttendanceLogService extends ServiceImpl<UserAttendanceLogMapper, UserAttendanceLog> implements IUserAttendanceLogService {

}
