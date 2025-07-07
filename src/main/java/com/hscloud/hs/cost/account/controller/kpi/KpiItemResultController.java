package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoSaveDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferListDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferSaveDTO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO2;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferListVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemResultService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
* 核算项结果集
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItemResult")
@Tag(name = "k_转科数据保存", description = "转科数据")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemResultController {

    private final IKpiItemResultService kpiItemResultService;

    @GetMapping("/transfer/page")
    @Operation(summary = "转科数据page")
    public R<IPage<KpiTransferListVO>> getTransferPage(@Validated KpiTransferListDTO dto) {
        return R.ok(kpiItemResultService.getTransferPage(dto));
    }

    @GetMapping("/transfer/info")
    @Operation(summary = "转科数据明细")
    public R<IPage<KpiTransferInfoVO>> getTransferInfoPage(@Validated KpiTransferInfoDTO dto) {
        return R.ok(kpiItemResultService.getTransferInfoPage(dto));
    }

    @GetMapping("/transfer/list")
    @Operation(summary = "转科数据list")
    public R<List<KpiTransferInfoVO2>> getTransferList(@Validated KpiTransferListDTO dto) {
        return R.ok(kpiItemResultService.getTransferList(dto));
    }

    @SysLog("转科数据保存")
    @PostMapping("/transfer/save")
    @Operation(summary = "转科数据保存add")
    public R transferSave(@RequestBody KpiTransferSaveDTO dto)  {
        kpiItemResultService.transferSave(dto);
        return R.ok();
    }

    @SysLog("转科数据批量保存")
    @PostMapping("/transfer/saveBatch")
    @Operation(summary = "转科数据批量保存")
    public R transferSaveV2(@RequestBody KpiTransferSaveDTO dto)  {
        kpiItemResultService.transferSaveV2(dto);
        return R.ok();
    }

    @SysLog("转科数据一键保存")
    @PostMapping("/transfer/saveOneTouch")
    @Operation(summary = "转科数据一键保存")
    public R oneTouchSave(@RequestBody KpiTransferInfoSaveDTO dto)  {
        kpiItemResultService.oneTouchSave(dto);
        return R.ok();
    }

    @SysLog("转科数据一键保存2  入参ids")
    @PostMapping("/transfer/saveOneTouch2")
    @Operation(summary = "转科数据一键保存2 入参ids")
    public R oneTouchSave2(@RequestBody KpiTransferInfoSaveDTO dto)  {
        kpiItemResultService.oneTouchSave2(dto);
        return R.ok();
    }
}