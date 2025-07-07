package com.hscloud.hs.cost.account.mapper.second;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
* 二次分配总任务 Mapper 接口
*
*/
@Mapper
public interface SecondTaskMapper extends BaseMapper<SecondTask> {
    List<SecondTask> sumTask(@Param("ids")List<Long> ids);
}

