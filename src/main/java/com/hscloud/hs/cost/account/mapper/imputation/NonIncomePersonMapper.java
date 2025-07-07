package com.hscloud.hs.cost.account.mapper.imputation;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.NonIncomePerson;
import org.apache.ibatis.annotations.Mapper;

/**
* 不计收入人员 Mapper 接口
*
*/
@Mapper
public interface NonIncomePersonMapper extends BaseMapper<NonIncomePerson> {

}

