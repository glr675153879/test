package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountRelationQueryDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountRelation;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author Administrator
 */
@Mapper
public interface KpiAccountRelationMapper extends BaseMapper<KpiAccountRelation> {

    /**
     * 获取核算单元关系分页列表
     * @param page 分页
     * @param dto 入参
     * @return 分页列表
     */
    IPage<KpiAccountRelationVO> getAccountRelationPageList(Page<KpiAccountRelationVO> page,@Param("dto") KpiAccountRelationQueryDTO dto);
}
