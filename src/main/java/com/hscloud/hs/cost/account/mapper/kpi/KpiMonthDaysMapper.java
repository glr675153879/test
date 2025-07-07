package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiMonthDays;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 配置表 Mapper 接口
 */
@Mapper
public interface KpiMonthDaysMapper extends BaseMapper<KpiMonthDays> {

}

