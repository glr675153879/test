package com.hscloud.hs.cost.account.mapper.userAttendance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.userAttendance.UserAttendanceLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 人员考勤表变更日志 Mapper 接口
 */
@Mapper
public interface UserAttendanceLogMapper extends BaseMapper<UserAttendanceLog> {

}

