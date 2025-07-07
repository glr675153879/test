package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskCountEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskCount;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskUser;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskCountVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
* 发放单元分配结果按人汇总 服务接口类
*/
public interface IUnitTaskCountService extends IService<UnitTaskCount> {

    void doCount(UnitTask unitTask);

    void exportCount(Long unitTaskId, HttpServletResponse response);

    ImportResultVo importCount(Long unitTaskId, String[][] xlsDataArr);

    List<UnitTaskCountVo> userList(Long unitTaskId);

    void editBatch(UnitTaskCountEditBatchDTO unitTaskCountEditBatchDTO);

    /**
     * 删除人员的count数据
     * @param unitTaskUser
     */
    void removeCountByUser(UnitTaskUser unitTaskUser);

    List<UnitTaskCount> listByTaskId(Long unitTaskId);

    /**
     * 检查 个人的绩效合计是否大于等于0
     * @param unitTaskId
     * @return
     */
    Boolean checkAmt(Long unitTaskId);
}
