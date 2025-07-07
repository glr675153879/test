package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexEnableDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiReportConfigListDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigCopy;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 报表多选配置 Mapper 接口
*
*/
@Mapper
public interface KpiReportConfigMapper extends BaseMapper<KpiReportConfig> {

    @Select("select * from sys_dict_item where dict_type in('PMC_kpi_calculate_grouping','kpi_unit_calc_type','user_type')")
    List<SysDictItem> getDicts(@Param("tenant_id")Long tenant_id);

    @Update("update kpi_report_config set status=#{input.status} where id =#{input.id}")
    void updateStatusById(@Param("input") KpiIndexEnableDto input);

    @Select("select * from kpi_report_config ${ew.customSqlSegment}")
    List<KpiReportConfigListDTO> selectList(@Param("ew") QueryWrapper<KpiReportConfig> ew);

    @Select("select * from kpi_report_config_copy ${ew.customSqlSegment}")
    List<KpiReportConfigListDTO> selectList2(@Param("ew") QueryWrapper<KpiReportConfig> ew);

    @Select("select * from kpi_report_config ${ew.customSqlSegment}")
    List<KpiReportConfigCopy> getList(@Param("ew") QueryWrapper<KpiReportConfig> ew);
}

