package com.hscloud.hs.cost.account.mapper.imputation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationDetails;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
* 归集明细 Mapper 接口
*
*/
@Mapper
public interface ImputationDetailsMapper extends BaseMapper<ImputationDetails> {

    void saveOrUpdateBatchImputationDetails(List<ImputationDetails> addOrEditList);
}

