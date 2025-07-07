package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiCalculateConfigDto;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiReportAlloValue;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCalculate;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
* 计算结果 Mapper 接口
*
*/
@Mapper
public interface KpiCalculateMapper extends BaseMapper<KpiCalculate> {

    void insertBatchSomeColumn(@Param("list") List<KpiCalculate> list);

    @Select("SELECT PARTITION_NAME FROM information_schema.partitions WHERE table_name = #{tablename}")
    List<String> findTablePartitionNmae(@Param("tablename") String tablename);

    @Select("select * from sys_user where tenant_id = #{tenant_id} and del_flag = '0'")
    List<SysUser> getUsers(@Param("tenant_id")Long tenant_id);

    @Select("select * from sys_dict_item where dict_type in ('kpi_calculate_grouping','kpi_unit_calc_type') and tenant_id =#{tenant_id}")
    List<SysDictItem> getDicts(@Param("tenant_id")Long tenant_id);

    @Select("select * from sys_dict_item where dict_type in ('kpi_calculate_grouping','kpi_unit_calc_type','user_type') and tenant_id =#{tenant_id}")
    List<SysDictItem> getDicts2(@Param("tenant_id")Long tenant_id);

    List<KpiCalculate> getList(@Param("ew") QueryWrapper<KpiCalculate> ew);

    List<KpiCalculateConfigDto> getList2(@Param("ew") QueryWrapper<KpiCalculateConfigDto> ew);

    List<KpiCalculateConfigDto> getList3(@Param("ew") QueryWrapper<KpiCalculateConfigDto> ew);

    @Update("update kpi_calculate set comp_value = #{ca.compValue} where id =#{ca.id} and period = #{ca.period}")
    void updateCompValue(@Param("ca") KpiCalculate ca);

    List<KpiReportAlloValue> getAlloValue(@Param("r") KpiAllocationRuleCopy r,@Param("period")Long period);
}

