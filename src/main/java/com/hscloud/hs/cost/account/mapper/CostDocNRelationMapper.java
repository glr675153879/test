package com.hscloud.hs.cost.account.mapper;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostDocNRelation;
import com.pig4cloud.pigx.common.data.datascope.PigxBaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * @author banana
 * @create 2023-09-11 14:38
 */
@Mapper
public interface CostDocNRelationMapper extends PigxBaseMapper<CostDocNRelation> {

    IPage<CostAccountUnit> listDocNRelation(Page page, @Param("docName") String docName, @Param("nurseName") String nurseName);

}
