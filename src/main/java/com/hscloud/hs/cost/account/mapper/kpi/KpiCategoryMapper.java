package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
* 分组表 Mapper 接口
*
*/
@Mapper
public interface KpiCategoryMapper extends BaseMapper<KpiCategory> {
    void insertBatchSomeColumn(@Param("list") List<KpiCategory> userStudyList);

    /*@Select("select category_code from kpi_category where busi_type = '1' and del_flag = '0' and status = '0' " +
            "and category_type='user_group' and category_name like concat('%', #{name}, '%')")
    List<String> getBwCode(@Param("name") String name);*/
}

