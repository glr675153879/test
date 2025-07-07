package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.DictDto;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndex;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiIndexFormula;
import com.hscloud.hs.cost.account.model.vo.kpi.*;
import com.hscloud.hs.cost.account.service.kpi.IKpiIndexFormulaService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;

/**
* 指标公式表
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiIndexFormula")
@Tag(name = "k_指标公式", description = "指标公式表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiIndexFormulaController {

    private final IKpiIndexFormulaService kpiIndexFormulaService;

    @SysLog("根据指标code获取指标公式list")
    @GetMapping("/list/{indexCode}")
    @Operation(summary = "*根据指标code获取指标公式list")
    public R<List<KpiIndexFormulaVO>> list(@PathVariable String indexCode,String planGroupCode, Long memberId, String planCategoryCode) {
        return R.ok(kpiIndexFormulaService.getFormulaListByIndexCode(indexCode, planGroupCode, memberId, planCategoryCode));
    }

    @SysLog("公式info")
    @GetMapping("/info/{id}")
    @Operation(summary = "*公式info")
    public R<KpiIndexFormula> info(@PathVariable Long id) {
        return R.ok(kpiIndexFormulaService.getById(id));
    }

    @SysLog("条件指标公式info")
    @GetMapping("/cond/info")
    @Operation(summary = "*条件指标info")
    public R<KpiIndexFormulaInfoVO> info(String indexCode, Integer formulaGroup) {
        return R.ok(kpiIndexFormulaService.getCondInfo(indexCode, formulaGroup));
    }

    @SysLog("指标公式表新增修改")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*指标公式表新增修改")
    public R add(@RequestBody KpiIndexFormulaDto dto)  {
        return R.ok(kpiIndexFormulaService.saveOrupdate(dto));
    }

    @SysLog("指标公式删除")
    @PostMapping("/del/{id}")
    @Operation(summary = "*指标公式删除")
    public R del(@PathVariable Long id)  {
        kpiIndexFormulaService.del(id);
        return R.ok();
    }

    /*****************************   公式方案   **********************************/

    @SysLog("非条件指标公式方案列表")
    @GetMapping("/plan/list")
    @Operation(summary = "*非条件指标公式表方案列表")
    public R<List<KpiIndexFormulaPlanVO>> planList(KpiIndexFormulaPlanListInfoDto dto) {
        return R.ok(kpiIndexFormulaService.getPlanList(dto));
    }

    @SysLog("非条件指标公式方案对象保存判断")
    @PostMapping("/plan/saveOrUpdate/judge")
    @Operation(summary = "*非条件指标公式的方案对象保存判断")
    public R<KpiFormulaJudgeVO> planSaveOrUpdateJudge(@RequestBody KpiIndexPlanMemberEditDto dto) {
        KpiFormulaJudgeVO kpiFormulaJudgeVO = new KpiFormulaJudgeVO();
        List<DictDto> list = kpiIndexFormulaService.planSaveOrUpdateJudge(dto);
//        try {
//            List<DictDto> planJudge = kpiIndexFormulaService.childPlanJudge(dto);
//            kpiFormulaJudgeVO.setPlanJudge(planJudge);
//
//        }catch (Exception e){
//
//        }
        kpiFormulaJudgeVO.setIndexJudge(list);
        return R.ok(kpiFormulaJudgeVO);
    }

    @SysLog("非条件指标公式方案对象保存")
    @PostMapping("/plan/saveOrUpdate")
    @Operation(summary = "*非条件指标公式的方案对象保存")
    public R planSaveOrUpdate(@RequestBody KpiIndexPlanMemberEditDto dto) {
        kpiIndexFormulaService.planSaveOrUpdate(dto);
        return R.ok();
    }

    @SysLog("非条件指标公式校验运行")
    @PostMapping("/nocond/verify")
    @Operation(summary = "*非条件指标公式校验运行")
    public R<String> nocondVerify(@RequestBody KpiNocondFormulaVerifyDto dto) {
        return R.ok(kpiIndexFormulaService.nocondVerify(dto));
    }

    @SysLog("条件指标公式校验运行")
    @PostMapping("/cond/verify")
    @Operation(summary = "*条件指标公式校验运行")
    public R<String> condVerify(@RequestBody KpiCondFormulaVerifyDto dto) {
        return R.ok(kpiIndexFormulaService.condVerify(dto));
    }

    @SysLog("矫正指标membercodes汇总")
    @GetMapping("/jzmember")
    public R<String> jzmember() {
        kpiIndexFormulaService.jzmember();
        return R.ok();
    }

    @SysLog("该公式所有适用对象")
    @GetMapping("/find_object")
    @Operation(summary = "*该公式所有适用对象")
    public R<KpiFindObject> findObject(Long formulaId,Long planId, Long memberId) {
        return R.ok(kpiIndexFormulaService.findObject(formulaId,planId, memberId));
    }

    @SysLog("是否允许复制")
    @GetMapping("/allowCopy")
    @Operation(summary = "*是否允许复制")
    public R<AllowCopyVo> allowCopy(String planCode, String indexCode, Long planObj) {
        return R.ok(kpiIndexFormulaService.allowCopy(planCode, indexCode, planObj));
    }

    @SysLog("公式copyFor")
    @PostMapping("/copyFor")
    @Operation(summary = "*公式copyFor")
    public R copyFor(@RequestBody @Validated ForCopyDto dto)  {
        kpiIndexFormulaService.copyFor(dto);
        return R.ok();
    }
}