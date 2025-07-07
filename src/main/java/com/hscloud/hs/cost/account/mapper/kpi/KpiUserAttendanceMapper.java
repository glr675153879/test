package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 人员考勤表(cost_user_attendance) Mapper 接口
*
*/
@Mapper
public interface KpiUserAttendanceMapper extends BaseMapper<KpiUserAttendance> {


    void insertBatchSomeColumn(@Param("list") List<KpiUserAttendance> userStudyList);


    IPage<KpiAccountUserDto> listByQueryDto(Page page, @Param("input") KpiAccountUseSearchDto queryDto);


    IPage<KpiAccountUserDto> listByQueryDto2(Page page, @Param("input") KpiAccountUseSearchDto queryDto);

    List<KpiAccountUserDto> listByQueryDto2_list(@Param("busiType")String busiType,@Param("categoryCode") String categoryCode);

    List<KpiUserAttendance> findList(@Param("input") KpiAccountUseSearchDto dto);


    IPage<KpiUserAttendanceDto> findMatch(Page page,@Param("input") KpiChangeUseSearchDto dto);


    IPage<KpiValueAdjustPageDto> findAdjust(Page page,@Param("input") KpiValueAdjustSearchDto dto);


    @Select("SELECT sud.*,sd.name from hsx.sys_user_dept sud join hsx.sys_dept sd on sud.dept_id  = sd.dept_id ")
    List<UserIdAndDeptId> getUserDept();


    List<SysUser> getItemUsers(@Param("busiType")String busiType,@Param("period") Long period);

    List<AttendanceCheckDTO> getItemNames(@Param("ew") QueryWrapper<AttendanceCheckDTO> ew);


    IPage<DicPageOutDto> findDictype(Page page, @Param("input") DicPageDto input);



}

