package com.hscloud.hs.cost.account.mapper.imputation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import org.apache.ibatis.annotations.Mapper;

/**
* 归集变更日志 Mapper 接口
*
*/
@Mapper
public interface ImputationChangeLogMapper extends BaseMapper<ImputationChangeLog> {

}

