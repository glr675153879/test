package com.hscloud.hs.cost.account.mapper.second;

import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.AttendanceDept;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 考勤表 Mapper 接口
*
*/
@Mapper
public interface AttendanceDeptMapper extends PigxBaseMapper<AttendanceDept> {

    List<AttendanceDept> listByTypeDept(String cycle);


    void insertBatchSomeColumn(@Param("list") List<AttendanceDept> userStudyList);
}

