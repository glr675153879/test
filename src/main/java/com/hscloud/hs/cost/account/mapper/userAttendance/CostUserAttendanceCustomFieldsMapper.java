package com.hscloud.hs.cost.account.mapper.userAttendance;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 人员考勤自定义字段表 Mapper 接口
 */
@Mapper
public interface CostUserAttendanceCustomFieldsMapper extends BaseMapper<CostUserAttendanceCustomFields> {



    List<CostUserAttendanceCustomFields> findLatestByDt();

    @Select("select column_id,name from cost_user_attendance_custom_fields group by column_id ,name  ")
    List<CostUserAttendanceCustomFields> listGroup();
}

