package com.hscloud.hs.cost.account.mapper;

import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorCenterQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorCenter;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorCenterVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 监测中心
 * @author  lian
 * @date  2023-09-19 18:18
 *
 */
@Mapper
public interface CostMonitorCenterMapper extends BaseMapper<CostMonitorCenter> {

    /**
     * 查询监测list
     *@param  queryDto 参数
     *@return  list
     */
    List<CostMonitorCenterVo> queryList(CostMonitorCenterQueryDto queryDto);
}
