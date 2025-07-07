package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.ProgProject;
import com.hscloud.hs.cost.account.model.entity.second.ProgProjectDetail;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;

import java.util.List;

/**
* 科室二次分配明细大项 服务接口类
*/
public interface IProgDetailItemService extends IService<ProgDetailItem> {

    /**
     * 根据 comProjectDetail 为 progProjectDetail 增出item
     * @param progProjectDetail
     * @param comProjectDetail
     */
    void createByDetail(ProgProjectDetail progProjectDetail, ProgProjectDetail comProjectDetail);

    /**
     * 删除itemList 及 下层数据
     * @param progDetailItemList
     */
    void delByItemList(List<ProgDetailItem> progDetailItemList);

    /**
     * 根据 detail 同步 item
     * @param progProjectDetail
     */
    void syncByDetail(ProgProjectDetail progProjectDetail);

    List<ProgDetailItem> listByPidCache(String cycle, Long progDetailId);
}
