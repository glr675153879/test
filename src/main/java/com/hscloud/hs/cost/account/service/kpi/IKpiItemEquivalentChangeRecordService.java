package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemCoefficientDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentChangeDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDistributeDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentChangeRecord;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentChangeRecordVO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 当量调整记录 服务接口类
 */
public interface IKpiItemEquivalentChangeRecordService extends IService<KpiItemEquivalentChangeRecord> {
    List<KpiItemEquivalentChangeRecordVO> getList(KpiItemEquivalentChangeDTO dto);

    void saveRecord(KpiItemEquivalentChangeDTO dto);

    void updateDistribute(KpiItemEquivalentDistributeDTO dto);

    @Transactional(rollbackFor = Exception.class)
    void coefficientBatchSet(KpiItemCoefficientDTO dto);

    List<KpiItemEquivalentChangeRecordVO> listByUnit(KpiItemEquivalentChangeDTO dto);

    void reset(KpiItemEquivalentDTO dto);
}
