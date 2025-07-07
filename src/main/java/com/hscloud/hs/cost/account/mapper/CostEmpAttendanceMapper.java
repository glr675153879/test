package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostEmpAttendance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 人员考勤表 Mapper 接口
 * </p>
 *
 * @author 
 * @since 2023-12-01
 */
@Mapper
public interface CostEmpAttendanceMapper extends BaseMapper<CostEmpAttendance> {
    void insertBatchSomeColumn(@Param("list") List<CostEmpAttendance> list);
}
