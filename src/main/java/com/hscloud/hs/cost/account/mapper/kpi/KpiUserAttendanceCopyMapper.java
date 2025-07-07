package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnitCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMember;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMemberCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendanceCopy;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 人员考勤备份 Mapper 接口
*
*/
@Mapper
public interface KpiUserAttendanceCopyMapper extends BaseMapper<KpiUserAttendanceCopy> {
    void insertBatchSomeColumn(@Param("list") List<KpiUserAttendanceCopy> list);

    @Select("select * from kpi_user_attendance ${ew.customSqlSegment}")
    List<KpiUserAttendanceCopy> getList(@Param("ew") QueryWrapper<KpiUserAttendanceCopy> ew);

    List<KpiUserAttendanceCopy> getList2(@Param("period") Long period);

}

