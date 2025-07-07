package com.hscloud.hs.cost.account.model.dto.kpi;

import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.utils.kpi.FormulaDependencyChecker;
import com.hscloud.hs.cost.account.utils.kpi.RealTimeFormulaDependencyChecker;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class KpiPlanVerifyDto {

    private String planCategoryCode;

    private String childPlanCode;

    private String childPlanName;

    private String type;

    private Long myMemberId;

    private String indexCaliber;

//    判断循环依赖用
    private RealTimeFormulaDependencyChecker realTimeFormulaDependencyChecker;

    private List<ConfigState> configStates = new ArrayList<>();
    private List<MissChildPlan> missChildPlans = new ArrayList<>();
    private List<MissIndex> missIndices = new ArrayList<>();
    private List<MissFormula> missFormulas = new ArrayList<>();
    private List<Cycledependency> cycledependencies = new ArrayList<>();
    private MissResult missResult;

    /********内存**********/
    private List<KpiIndexFormulaObj> kpiIndexFormulaObjs;
    private List<KpiMember> kpiMembers;
    private List<KpiAccountUnit> kpiAccountUnits;
    private List<CostClusterUnit> costClusterUnits;
    private List<KpiIndexFormula> kpiIndexFormulas;
    private List<KpiIndex> kpiIndices;
    private List<UserCoreVo> sysUsers;

    @Data
    public static class MissResult{
        private List<MissIndex> missIndices;
        private List<MissFormula> missFormulas;
        private List<Cycledependency> cycledependencies;
    }

    @Data
    public static class ConfigState{
        @Schema(description = "0已配置1未配置")
        private Integer state;
        private Long memberId;
        private String indexCode;
        private String planCode;
    }

    @Data
    public static class MissChildPlan{
        private String childPlancode;
        private Long memberId;
        private String memberName;
        private Long formulaId;
        private String indexCode;
        private String indexName;
        private String indexCaliber;
    }
    @Data
    public static class MissIndex{
        private String childPlanCode;
        private String childPlanName;

        private String indexCode;
        private String indexName;
    }
    @Data
    public static class MissFormula{
        private String childPlanCode;
        private String childPlanName;

        private Long planObj;
        private String planObjName;
        private String indexCode;
        private String indexName;

        private String indexCaliber;
    }

    @Data
    public static class Cycledependency{
        private String childPlanCode;
        private String childPlanName;

        private String indexCode;
        private String indexName;

        private String dependencyCode;
        private String dependencyName;
    }

}
