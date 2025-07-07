package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiIndexFormulaPlanListInfoDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountPlanChildCopy;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormula;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiIndexFormulaPlanVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 指标公式表 Mapper 接口
*
*/
@Mapper
public interface KpiIndexFormulaMapper extends BaseMapper<KpiIndexFormula> {


    @Select("SELECT MAX(formula_group) FROM kpi_index_formula WHERE INDEX_CODE = #{code}")
    Integer getZh(String code);


    void updateDelFlag(@Param("ids") List<Long> ids);

    void insertBatchSomeColumn(@Param("list")List<KpiIndexFormula> oldFormulas);
}

