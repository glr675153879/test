package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.PersonChange;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDetailsVO;

import java.util.List;
import java.util.Map;

/**
 * @author tianbo
 * @Description：
 * @date 2024/8/6 14:46
 */
public interface IPersonChangeService extends IService<PersonChange> {
    void savePersonChange(List<ImputationDetailsVO> imputationDetails, List<ImputationDetailsVO> imputationDetailsVOs, String imputationCycle, Long accountUnitId);

    List<PersonChange> listByCycle(String cycle);

    Map<String, String> convertToMap(String ids, String names);
}
