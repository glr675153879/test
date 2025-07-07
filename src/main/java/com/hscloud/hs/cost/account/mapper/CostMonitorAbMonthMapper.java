package com.hscloud.hs.cost.account.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hscloud.hs.cost.account.model.dto.monitorCenter.CostMonitorAbnormalMonQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostMonitorAbMonth;
import com.hscloud.hs.cost.account.model.vo.monitorCenter.CostMonitorAbnormalMonVo;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 监测中心
 * @author  lian
 * @date  2023-09-19 18:19
 *
 */
@Mapper
public interface CostMonitorAbMonthMapper extends BaseMapper<CostMonitorAbMonth> {


    /**
     * 查询异常月份数据记录
     *@param  queryDto 参数
     *@return  list
     */
    List<CostMonitorAbnormalMonVo> queryMonitorAbnormalMonth(CostMonitorAbnormalMonQueryDto queryDto);

}
