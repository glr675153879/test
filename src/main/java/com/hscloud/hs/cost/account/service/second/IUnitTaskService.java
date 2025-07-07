package com.hscloud.hs.cost.account.service.second;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.second.GrantUnit;
import com.hscloud.hs.cost.account.model.entity.second.Programme;
import com.hscloud.hs.cost.account.model.entity.second.SecondTask;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;

import java.math.BigDecimal;

/**
* 发放单元任务 服务接口类
*/
public interface IUnitTaskService extends IService<UnitTask> {

    /**
     * 根据 总任务生成 各发放单元任务
     * @param secondTask
     * @param getGrantUnitIds 发放单元id
     */
    void createBySecondTaskId(SecondTask secondTask, String getGrantUnitIds);

    BigDecimal getKsAmt(GrantUnit grantUnit, String cycle);

    /**
     * 编内人员一次分配结果金额
     *@param  grantUnit 发放单元
     *@param  cycle 周期
     *@return  bigDecimal
     */
    BigDecimal getKsAmtNonStaff(GrantUnit grantUnit, String cycle);

    /**
     * 额外分配人员一次分配结果金额
     *@param  grantUnit 发放单元
     *@param  cycle 周期
     *@return  bigDecimal
     */
    BigDecimal getKsAmtExtraUserIds(GrantUnit grantUnit, String cycle);



    UnitTask getLastUnitTask(Long unitTaskId, Long grantUnitId);

    void syncByProgramme(Programme programme);

    void toOnline(Long id);

    void toOffline(Long id);

    void removeByGrantUnitId(SecondTask secondTask, GrantUnit grantUnit);
}
