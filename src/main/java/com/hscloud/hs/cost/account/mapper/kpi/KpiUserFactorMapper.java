/*
 *
 *      Copyright (c) 2018-2025, lengleng All rights reserved.
 *
 *  Redistribution and use in source and binary forms, with or without
 *  modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice,
 *  this list of conditions and the following disclaimer.
 *  Redistributions in binary form must reproduce the above copyright
 *  notice, this list of conditions and the following disclaimer in the
 *  documentation and/or other materials provided with the distribution.
 *  Neither the name of the hscloud.com developer nor the names of its
 *  contributors may be used to endorse or promote products derived from
 *  this software without specific prior written permission.
 *  Author: lengleng (wangiegie@gmail.com)
 *
 */

package com.hscloud.hs.cost.account.mapper.kpi;

import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserFactor;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 字典表 Mapper 接口
 * </p>
 *
 * @author lengleng
 * @since 2017-11-19
 */
@Mapper
public interface KpiUserFactorMapper extends PigxBaseMapper<KpiUserFactor> {

    void insertBatchSomeColumn(@Param("list") List<KpiUserFactor> userStudyList);


    @Select("select * from kpi_dict where status = '0'")
    List<KpiDict> getDic();


    List<KpiUserFactor> getList(@Param("input") KpiUserFactorSearchDto input);


    List<KpiUserFactorBeforeDto> getUserDept(@Param("input") KpiUserFactorSearchDto searchDto);


    List<KpiPersonnelFactorList> getListByDicType(@Param("dicType") String dicType);


    @Select("select * from sys_user where del_flag = '0'")
    List<SysUser> getUser();

    List<KpiDictItemOutDto> getDictKey();



    List<KpiUserFactorDeptDto> deptValueList(@Param("input") KpiUserFactorDeptSearchDto dto);
}
