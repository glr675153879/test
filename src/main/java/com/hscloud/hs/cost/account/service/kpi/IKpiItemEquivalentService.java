package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountUnitQueryDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalent;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemCoefficientVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentVO;

import java.util.List;
import java.util.Set;

/**
 * 核算项当量 服务接口类
 */
public interface IKpiItemEquivalentService extends IService<KpiItemEquivalent> {

    void saveData(Long period, Set<String> itemCodes, List<KpiItemEquivalent> records, boolean isAll);

    List<KpiItemEquivalentVO> getList(KpiItemEquivalentDTO dto, boolean isAdmin);

    KpiItemEquivalentVO getParentVO(KpiItemEquivalentDTO dto);

    List<KpiAccountUnitVO> getEquivalentUnitList(KpiAccountUnitQueryDTO dto, Long period);

    List<KpiItemCoefficientVO> getCoefficientList(Long accountUnitId, Long period, Long itemId);

    void lock(Long period);
}
