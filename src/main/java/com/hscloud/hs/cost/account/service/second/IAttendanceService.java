package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;

import java.util.List;

/**
* 考勤表 服务接口类
*/
public interface IAttendanceService extends IService<Attendance> {

    /**
     * 获取currentCycle的上一个cycle
     * cycle = null时，获取最新的cycle
     * @param currentCycle
     * @return
     */
    String getLastCycle(String currentCycle);

    IPage<Attendance> pageMoreDept(PageRequest<Attendance> pr, String cycle);

    /**
     * 获取 某周期 非独立科室单元的所有人员
     * @param cycle
     * @return
     */
    List<Attendance> listByTypeDept(String cycle);

    /**
     *  获取某周期 某科室单元下的所有人员
     * @param cycle
     * @param accountUnitId
     * @return
     */
    List<Attendance> listByAccountUnitId(String cycle, Long accountUnitId);

    /**
     * 获得 某周期 某人的所有考勤记录
     * @param cycle
     * @param userId
     * @return
     */
    List<Attendance> listByUserId(String cycle,Long userId);

    /**
     * 根据 周期 和 userid 获取考勤数据
     * @param cycle
     * @param userIds
     * @param accountUnitIds
     * @return
     */
    List<Attendance> getByCycleUserUnit(String cycle, List<Long> userIds,List<String> accountUnitIds);

    List<Attendance> listByUserIdsAndCycle(List<Long> userIds, String cycle);

    List<Attendance> listByCycle(String cycle);
}
