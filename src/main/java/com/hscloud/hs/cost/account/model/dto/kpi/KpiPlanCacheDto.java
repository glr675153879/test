package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.utils.kpi.RealTimeFormulaDependencyChecker;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiPlanCacheDto {

    public List<ObjCategory> objCategories;

    @Data
    public static class ObjCategory{
        private String categoryCode;
        private Long userId;
    }

}
