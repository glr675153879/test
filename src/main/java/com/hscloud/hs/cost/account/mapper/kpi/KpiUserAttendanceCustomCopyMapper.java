package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCustomCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 人员考勤自定义字段表 Mapper 接口
*
*/
@Mapper
public interface KpiUserAttendanceCustomCopyMapper extends BaseMapper<KpiUserAttendanceCustomCopy> {

    void insertBatchSomeColumn(@Param("list") List<KpiUserAttendanceCustomCopy> userStudyList);

}

