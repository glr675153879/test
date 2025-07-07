package com.hscloud.hs.cost.account.mapper.imputation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import org.apache.ibatis.annotations.Mapper;

/**
* 归集主档 Mapper 接口
*
*/
@Mapper
public interface ImputationMapper extends BaseMapper<Imputation> {

}

