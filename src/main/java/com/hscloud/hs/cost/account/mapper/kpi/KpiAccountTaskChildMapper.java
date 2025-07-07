package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTaskChild;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* 核算任务表 Mapper 接口
*
*/
@Mapper
public interface KpiAccountTaskChildMapper extends BaseMapper<KpiAccountTaskChild> {

    void updateLog(@Param("id") Long id, @Param("log") String log,@Param("ero_log") String error_log,
                   @Param("status") int status,@Param("status_name") String status_name);

    @Update("update kpi_account_task_child set status = 0,status_name = null where status <=50")
    void initTask();

    @Select("select period from kpi_account_task_child where id =#{id}")
    Long getPeriod(Long id);

    @Select("select task_child_id from kpi_config where period =#{period} and issued_flag='Y'")
    Long getTaskChildId(Long period);
}

