package com.hscloud.hs.cost.account.mapper.kpi;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountTaskListDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountTaskListVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
* 核算任务表(cost_account_task) Mapper 接口
*
*/
@Mapper
public interface KpiAccountTaskMapper extends BaseMapper<KpiAccountTask> {

    IPage<KpiAccountTaskListVO> pageTask(Page page, @Param("input") KpiAccountTaskListDto input);

    @Select("select count(*) from sys_shedlock where now() <DATE_ADD(lock_until, INTERVAL 8 HOUR) and name in ${name}")
    int getShedLog(String name);

    @Select("select index_code,a.period from kpi_account_task a join kpi_account_task_child b on a.id = b.task_id where b.id=#{taskChildId}")
    KpiAccountTask getIndexCode(Long taskChildId);

    @Select("select a.* from kpi_account_task a join kpi_account_task_child b on a.id = b.task_id where b.id=#{taskChildId}")
    KpiAccountTask getTask(Long taskChildId);

    @Update("update kpi_config set issued_date = now(),task_child_id=#{taskChildId}  where period = #{period}")
    void changeConfig(@Param("period") Long period,@Param("taskChildId") Long taskChildId);

    @Update("update kpi_account_task set account_task_name =#{task.accountTaskName} where id =#{task.id}")
    void update2(@Param("task")KpiAccountTask task);
}

