package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResult;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 任务执行结果
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Mapper
public interface CostTaskExecuteResultMapper extends BaseMapper<CostTaskExecuteResult> {

    /**
     * 根据任务id获取当前任务的总值
     * @param taskId
     * @return
     */
    BigDecimal getTotalCostSum(Long taskId);

    /**
     * 根据任务id和单元id获取result对象
     * @param taskId
     * @param accountUnitId
     * @return
     */
    CostTaskExecuteResult getExecuteResult(@Param("taskId") Long taskId, @Param("accountUnitId") Long accountUnitId);
}
