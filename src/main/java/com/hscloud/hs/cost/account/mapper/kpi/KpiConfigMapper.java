package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiReport;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiConfigVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.Date;
import java.util.List;

/**
 * 配置表 Mapper 接口
 */
@Mapper
public interface KpiConfigMapper extends BaseMapper<KpiConfig> {

    @Update("update kpi_config set default_flag='N' where default_flag='Y'")
    void updateDef();

    @Update("update kpi_config set index_flag = '9',index_flag_ks = '9', equivalent_index_flag = '1'")
    void initConfig();

    void updateIndex(@Param("busiType") String busiType, @Param("period") Long period, @Param("status") String status,
                     @Param("nonEquivalentIndexUpdateDate") Date nonEquivalentIndexUpdateDate);

    List<KpiConfig> getList(@Param("ew") QueryWrapper<KpiConfig> ew);

    IPage<KpiConfigVO> page2(Page<KpiConfigVO> page, @Param("ew") LambdaQueryWrapper<KpiConfig> qw);
}

