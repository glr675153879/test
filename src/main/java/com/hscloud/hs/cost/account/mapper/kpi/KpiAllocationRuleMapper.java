package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAllocationRuleListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRule;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAllocationRuleCopy;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAllocationRuleListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 分摊公式表 Mapper 接口
*
*/
@Mapper
public interface KpiAllocationRuleMapper extends BaseMapper<KpiAllocationRule> {


    IPage<KpiAllocationRuleListVO> getPage(Page<KpiAllocationRule> page, @Param("dto") KpiAllocationRuleListDto dto);



    List<KpiAllocationRuleListVO.Yhgx> getYhgx(@Param("docCode") String docCode, @Param("ids") List<Long> ids);

    @Select("select * from kpi_allocation_rule where id =#{id}")
    KpiAllocationRuleCopy selectById2(@Param("id") Long id);
}

