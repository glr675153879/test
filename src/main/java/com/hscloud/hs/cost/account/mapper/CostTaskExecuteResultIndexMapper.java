package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.entity.CostTaskExecuteResultIndex;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * 任务执行结果-核算指标
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-17 16:49:08
 */
@Mapper
public interface CostTaskExecuteResultIndexMapper extends BaseMapper<CostTaskExecuteResultIndex> {

    /**
     * 根据任务id和核算单元id获取核算指标集合
     * @param taskId
     * @param unitId
     * @return
     */
    List<CostTaskExecuteResultIndex>  getIndexIds(@Param("taskId") Long taskId,@Param("unitId") Long unitId);

    /**
     * 获取该指标关联的核算项表中的主键id
     * @param taskId
     * @param unitId
     * @param indexId
     * @param parentId
     * @return
     */
    String getItems(@Param("taskId")Long taskId, @Param("unitId")Long unitId,@Param("indexId") Long indexId,@Param("parentId") Long parentId);

    /**
     * 获取外层指标下的自己指标
     * @param taskId
     * @param unitId
     * @param indexId
     * @return
     */
    List<CostTaskExecuteResultIndex> getIndexListChildren(@Param("taskId")Long taskId, @Param("unitId")Long unitId,@Param("indexId") Long indexId);


    /**
     * 根据任务id、核算单元id、指标id、和父级指标id获取唯一的指标对象
     * @param taskId
     * @param accountUnitId
     * @param bizId
     * @param parentId
     * @return
     */
    CostTaskExecuteResultIndex getResultIndex(@Param("taskId")Long taskId, @Param("unitId")Long accountUnitId,@Param("bizId") Long bizId,@Param("parentId") Long parentId);

    /**
     * 查询一级项的总和数据
     *@param  taskId 任务id
     *@param  unitId 单位id
     *@return  sum
     */
    BigDecimal querySum(@Param("taskId")Long taskId, @Param("unitId")Long unitId,@Param("indexId")Long indexId);

    /**
     *  获取核算指标的总值
     * @param taskId
     * @param indexId
     * @return
     */

    BigDecimal getIndexCount(@Param("taskId")Long taskId, @Param("indexId")Long indexId);
}
