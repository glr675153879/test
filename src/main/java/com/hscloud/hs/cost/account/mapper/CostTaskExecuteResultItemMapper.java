package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResultItem;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

/**
 * 任务执行结果-核算项
 * 
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Mapper
public interface CostTaskExecuteResultItemMapper extends BaseMapper<CostTaskExecuteResultItem> {

    /**
     * 获取该核算项的总值
     * @param taskId
     * @param indexId
     * @param itemId
     * @return
     */
    BigDecimal getItemCount(@Param("taskId")Long taskId, @Param("indexId")Long indexId, @Param("itemId")Long itemId);
}
