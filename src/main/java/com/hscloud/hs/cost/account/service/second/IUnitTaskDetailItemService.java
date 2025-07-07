package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.ProgDetailItemSave1DTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgDetailItemSave2DTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgItemDelDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskDetailItemSaveDTO;
import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* 科室二次分配明细大项值 服务接口类
*/
public interface IUnitTaskDetailItemService extends IService<UnitTaskDetailItem> {

    /**
     * 初始化 （科室二次分配）
     * 带user信息 一个用户一笔
     * userList == null  则查taskuser
     * @param unitTaskId
     * @param unitTaskProjectDetail
     */
    void initUserData(Long unitTaskId, UnitTaskProjectDetail unitTaskProjectDetail,  List<UnitTaskUser> userList);

    /**
     * 查询itemList user角度
     * @param detailId
     * @return
     */
    List<UnitTaskDetailItemVo> userList(Long detailId);
    List<UnitTaskDetailItemVo> userList(Long detailId, String empCode);

    /**
     * 保存items
     * @param unitTaskDetailItemSaveDTO
     */
    void saveItems(UnitTaskDetailItemSaveDTO unitTaskDetailItemSaveDTO);

    /**
     * 保存发放单元 方案item 工作量
     * @param progDetailItemSave2DTO
     */
    void saveProgDetailItem2(ProgDetailItemSave2DTO progDetailItemSave2DTO);

    void updateProgDetailItem2Index(ProgDetailItemSave2DTO progDetailItemSave2DTO);

    /**
     * 保存发放单元 方案item 系数分配
     * @param progDetailItemSave1DTO
     */
    void saveProgDetailItem1(ProgDetailItemSave1DTO progDetailItemSave1DTO);

    /**
     * 删除
     * @param progItemDelDTO
     */
    void deleteById(ProgItemDelDTO progItemDelDTO);

    void delByProjectId(List<Long> projectIds);

    void delByDetailId(List<Long> detailIds);

    void syncByDetail(String cycle, UnitTaskProjectDetail unitTaskProjectDetail);

    void exportErci(Long unitTaskProjectDetailId, HttpServletResponse response);

    ImportResultVo importErciXishu(Long unitTaskProjectDetailId, String[][] xlsDataArr);

    ImportResultVo importErciWork(Long unitTaskProjectDetailId, String[][] xlsDataArr);

    List<UnitTaskDetailItem> listByUnitTask(Long unitTaskId);

    /**
     * 从任务数据 获取 方案数据
     * @param unitTaskId
     * @param unitDetailId
     * @return
     */
    List<ProgDetailItem> getProgItemList(Long unitTaskId, Long unitDetailId);
}
