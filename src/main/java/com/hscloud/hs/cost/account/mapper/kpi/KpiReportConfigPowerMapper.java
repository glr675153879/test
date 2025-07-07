package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReportConfigPower;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface KpiReportConfigPowerMapper extends BaseMapper<KpiReportConfigPower> {

    void insertBatchSomeColumn(@Param("list") List<KpiReportConfigPower> list);

    List<Long> getRportIds();

    List<KpiReportConfig> getRportConfigs(@Param("user") PigxUser user);

}
