package com.hscloud.hs.cost.account.service.kpi;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormula;
import com.hscloud.hs.cost.account.model.vo.kpi.*;

import java.util.List;

/**
* 指标公式表 服务接口类
*/
public interface IKpiIndexFormulaService extends IService<KpiIndexFormula> {

    Long saveOrupdate(KpiIndexFormulaDto dto);

    List<KpiIndexFormulaVO> getFormulaListByIndexCode(String indexCode,String planGroupCode, Long memberId, String planCategoryCode);

    List<KpiIndexFormulaPlanVO> getPlanList(KpiIndexFormulaPlanListInfoDto dto);

    void planSaveOrUpdate(KpiIndexPlanMemberEditDto dto);

    List<DictDto> planSaveOrUpdateJudge(KpiIndexPlanMemberEditDto dto);

    void del(Long id);

    KpiIndexFormulaInfoVO getCondInfo(String indexCode, Integer formulaGroup);


    String getResult(KpiIndex kpiIndex, String formulaOrigin,List<KpiFormulaParamsDto> list, String period);

    String nocondVerify(KpiNocondFormulaVerifyDto dto);

    String condVerify(KpiCondFormulaVerifyDto dto);

    List<DictDto> childPlanJudge(KpiIndexPlanMemberEditDto dto);

    void jzmember();

    KpiFindObject findObject(Long formulaId,Long planId, Long memberId);

    AllowCopyVo allowCopy(String planCode, String indexCode, Long planObj);


    void copyFor(ForCopyDto dto);
}
