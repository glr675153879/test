package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountTaskListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiDeptMap;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountTaskListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
* 科室映射 Mapper 接口
*
*/
@Mapper
public interface KpiDeptMapMapper extends BaseMapper<KpiDeptMap> {
    IPage<KpiDeptMap> pageList(Page page, @Param("ew")QueryWrapper<KpiDeptMap> ew);
}

