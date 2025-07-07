package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCustom;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCustomCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 人员考勤自定义字段表 Mapper 接口
*
*/
@Mapper
public interface KpiUserAttendanceCustomMapper extends BaseMapper<KpiUserAttendanceCustom> {

    void insertBatchSomeColumn(@Param("list") List<KpiUserAttendanceCustom> userStudyList);

    List<KpiUserAttendanceCustomCopy> getList(@Param("ew") QueryWrapper<KpiUserAttendanceCustomCopy> ew);
}

