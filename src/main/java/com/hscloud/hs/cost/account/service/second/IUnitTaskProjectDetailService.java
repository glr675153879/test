package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetail2SaveBatchDTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskProjectDetailSave2DTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskProjectDetailSaveDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* 核算指标值 服务接口类
*/
public interface IUnitTaskProjectDetailService extends IService<UnitTaskProjectDetail> {

    /**
     * 科室二次分配
     * 没有user信息，一个project 一笔
     * 根据发放单元progProject 新增projectDetail
     *
     * @param cycle
     * @param unitTaskProject
     * @param progProject
     */
    void createByProject(String cycle, UnitTaskProject unitTaskProject, ProgProject progProject);

    /**
     * 初始化 （单项绩效 和 平均绩效 ）
     * 带user信息 一个用户一笔
     * userList == null 则查taskuser
     * @param unitTaskId
     * @param unitTaskProject
     * @param progProject
     */
    void initUserData(Long unitTaskId, UnitTaskProject unitTaskProject, ProgProject progProject, List<UnitTaskUser> userList);


    /**
     * user维度list查询
     * @param projectId
     * @return
     */
    List<UnitTaskProjectDetailVo> userList(Long projectId);
    List<UnitTaskProjectDetailVo> userList(Long projectId,String empCode);

    /**
     * 单项 和 平均保存detail值
     * @param unitTaskProjectDetailSaveDTO
     */
    void saveDetail(UnitTaskProjectDetailSaveDTO unitTaskProjectDetailSaveDTO);

    void saveDetail2(UnitTaskProjectDetailSave2DTO unitTaskProjectDetailSave2DTO);

    /**
     * 为 userList 增加 detail数据
     * @param unitTaskProject
     * @param userList
     * @param progProjectDetailList
     */
    void addByProgDetailList(UnitTaskProject unitTaskProject,List<UnitTaskUser> userList,List<ProgProjectDetail> progProjectDetailList);

    /**
     * 保存发放单元方案
     * @param progProjectDetailSaveDTO
     */
    void saveProgDetail(ProgProjectDetailSaveDTO progProjectDetailSaveDTO);

    // void updateProgDetailIndex(ProgProjectDetailSaveDTO progProjectDetailSaveDTO);

    void delByProjectId(List<Long> projectIds);

    /**
     * 根据发放单元project 同步任务数据
     * @param unitTaskProject
     */
    void syncByProject(String cycle, UnitTaskProject unitTaskProject);

    void addProgDetail2(UnitTaskProjectDetail unitTaskProjectDetail);

    void delProgDetail2(Long taskDetailId);

    void addProgDetail2Batch(ProgProjectDetail2SaveBatchDTO progProjectDetail2SaveBatchDTO);

    void exportDanxiang(Long unitTaskProjectId,HttpServletResponse response);

    ImportResultVo importDanxiang(Long unitTaskProjectId, String[][] xlsDataArr);

    List<UnitTaskProjectDetail> listByUnitTask(Long unitTaskId);

    void removeByTaskProjectIds(List<Long> collect);

    /**
     * 从业务数据中 获取 方案数据
     * @param unitTaskId
     * @param unitProjectId
     * @return
     */
    List<ProgProjectDetail> getProgDetailList(Long unitTaskId, Long unitProjectId);
}
