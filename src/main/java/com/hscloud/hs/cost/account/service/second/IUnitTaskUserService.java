package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.DmoUserPageDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserAddBatchDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;

import java.util.List;

/**
* 发放单元任务人员 服务接口类
*/
public interface IUnitTaskUserService extends IService<UnitTaskUser> {

    void createInit(UnitTask unitTask);

    /**
     * 获取 任务下的所有人员
     *
     * @param unitTaskId
     * @return
     */
    List<UnitTaskUser> listByTaskId(Long unitTaskId,String empCode);
    List<UnitTaskUser> listByTaskId(Long unitTaskId);

    /**
     * 为unitTask 新增user
     * 会增出所有业务数据
     */
    void createInitBatch(UnitTask unitTask, List<UnitTaskUser> addList);

    /**
     * 批量新增
     * @param unitTaskUserAddBatchDTO
     */
    void addBatch(UnitTaskUserAddBatchDTO unitTaskUserAddBatchDTO);


    /**
     * 获取考勤数据 更新进taskUserList
     * @param cycle
     * @param taskUserList
     * @param accountUnitIdList
     */
    List<UnitTaskUser> getAttendanceData(String cycle, List<UnitTaskUser> taskUserList, List<String> accountUnitIdList);

    void setIfGetAmt(UnitTaskUser unitTaskUser, Attendance attendanceDB);

    /**
     * 挑选人员
     * @param dmoUserPageDTO
     * @return
     */
    IPage<UnitTaskUser> userPage(DmoUserPageDTO dmoUserPageDTO);

    /**
     * 批量修改
     * @param unitTaskUserEditBatchDTO
     */
    void editBatch(UnitTaskUserEditBatchDTO unitTaskUserEditBatchDTO);

    /**
     *
     * @param id
     */
    void delById(Long id);

    /**
     * 获取上一周期的人员数据
     * @param lastUnitTask
     * @param userIds
     * @return
     */
    List<UnitTaskUser> listByTaskUser(UnitTask lastUnitTask, List<String> userIds);

    void delBatchByIds(List<Long> idsList);
}
