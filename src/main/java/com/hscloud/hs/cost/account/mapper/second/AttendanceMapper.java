package com.hscloud.hs.cost.account.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 考勤表 Mapper 接口
*
*/
@Mapper
public interface AttendanceMapper extends PigxBaseMapper<Attendance> {

    List<Attendance> listByTypeDept(String cycle);


    void insertBatchSomeColumn(@Param("list") List<Attendance> userStudyList);

    @Delete("delete from cost_attendance where `cycle` = #{period} ")
    void deleteByPeriod(Long period);
}

