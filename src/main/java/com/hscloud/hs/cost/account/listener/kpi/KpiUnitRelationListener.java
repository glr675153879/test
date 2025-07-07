package com.hscloud.hs.cost.account.listener.kpi;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSONArray;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiAccountRelationDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.KpiUnitRelationImportDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 */
@Slf4j
public class KpiUnitRelationListener extends AnalysisEventListener<KpiUnitRelationImportDTO> {
    List<KpiUnitRelationImportDTO> list = new ArrayList<>(512);
    public JSONArray errorArray = new JSONArray();

    private final KpiAccountUnitService kpiAccountUnitService;
    private final List<KpiAccountUnit> docAccountList;
    private final List<KpiAccountUnit> nurseAccountList;
    private final String categoryCode;

    public KpiUnitRelationListener(KpiAccountUnitService kpiAccountUnitService, List<KpiAccountUnit> docAccountList, List<KpiAccountUnit> nurseAccountList, String categoryCode) {
        this.kpiAccountUnitService = kpiAccountUnitService;
        this.docAccountList = docAccountList;
        this.nurseAccountList = nurseAccountList;
        this.categoryCode = categoryCode;
    }

    @Override
    public void invoke(KpiUnitRelationImportDTO dto, AnalysisContext analysisContext) {
        list.add(dto);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        if (list.size() == 0) {
            return;
        }
        Map<Long,String> map = new HashMap<>(512);
        for (KpiUnitRelationImportDTO kpiUnitRelationImportDTO : list) {
            String docAccountName = kpiUnitRelationImportDTO.getDocAccountName();
            String nurseAccountName = kpiUnitRelationImportDTO.getNurseAccountName();

            KpiAccountUnit docAccount = docAccountList.stream().filter(item -> item.getName().equals(docAccountName)).findFirst().orElse(null);
            if (docAccount == null) {
                errorArray.add("科室单元不存在：" + docAccountName);
                errorArray.add(kpiUnitRelationImportDTO);
                continue;
            }
            String[] var1 = nurseAccountName.split(",");
            for (String s : var1) {
                KpiAccountUnit nurseAccount = nurseAccountList.stream().filter(item -> item.getName().equals(s)).findFirst().orElse(null);
                String value = map.get(docAccount.getId());
                if (null != nurseAccount) {
                    if (StringUtils.hasLength(value)) {
                        value = value + "," + nurseAccount.getId();
                        map.put(docAccount.getId(), value);
                    } else {
                        map.put(docAccount.getId(), nurseAccount.getId().toString());
                    }
                } else {
                    map.put(docAccount.getId(), "");
                }
            }
        }
        if (map.size() > 0) {
            for (Long key : map.keySet()) {
                KpiAccountRelationDTO dto = new KpiAccountRelationDTO();
                dto.setCategoryCode(categoryCode);
                dto.setDocAccountId(key);
                dto.setNurseAccountId(map.get(key));
                kpiAccountUnitService.saveAccountRelation(dto);
            }
        }
    }
}
