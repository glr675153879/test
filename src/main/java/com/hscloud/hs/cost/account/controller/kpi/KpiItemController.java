package com.hscloud.hs.cost.account.controller.kpi;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountUnitMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiConfigMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiConfig;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemExtVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemService;
import com.pig4cloud.pigx.common.core.exception.BizException;
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
 * 核算项
 *
 * @author Administrator
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiItem")
@Tag(name = "k_item", description = "核算项")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class KpiItemController {

    private final IKpiItemService kpiItemService;
    private final KpiAccountUnitMapper kpiAccountUnitMapper;
    private final KpiConfigMapper kpiConfigMapper;

    @GetMapping("/info/{id}")
    @Operation(summary = "*核算项info")
    public R<KpiItemVO> info(@PathVariable Long id) {
        return R.ok(kpiItemService.getKpiItem(id));
    }

    @GetMapping("/page")
    @Operation(summary = "*核算项page")
    public R<IPage<KpiItemVO>> getPage(@Validated KpiItemQueryDTO dto) {
        return R.ok(kpiItemService.getPage(dto));
    }

    @GetMapping("/page/old")
    @Operation(summary = "*核算项 历史记录")
    public R<IPage<KpiItemVO>> getPageOld(@Validated KpiItemQueryDTO dto) {
        return R.ok(kpiItemService.getPageOld(dto));
    }

    @GetMapping("/list")
    @Operation(summary = "*核算项list")
    public R<List<KpiItemVO>> getList(@Validated KpiItemQueryDTO dto) {
        return R.ok(kpiItemService.getList(dto));
    }

    @SysLog("核算项add")
    @PostMapping("/saveOrUpdate")
    @Operation(summary = "*核算项add")
    public R<Long> saveOrUpdate(@RequestBody KpiItemDTO dto) {
        return R.ok(kpiItemService.saveOrUpdate(dto));
    }

    @SysLog("核算项switch")
    @PostMapping("/switch")
    @Operation(summary = "*核算项状态停/启用")
    public R switchStatus(@RequestBody BaseIdStatusDTO dto) {
        kpiItemService.switchStatus(dto);
        return R.ok();
    }

    @SysLog("核算项del")
    @PostMapping("/del/{id}")
    @Operation(summary = "*核算项del")
    public R deleteItem(@PathVariable Long id) {
        kpiItemService.deleteItem(id);
        return R.ok();
    }

    @GetMapping("/resultList/{id}")
    @Operation(summary = "核算项结果集list")
    public R<String> getResultList(@PathVariable Long id, @RequestParam(value = "period", required = false) String period) {
        return R.ok(kpiItemService.getResultList(id, period));
    }

    @SysLog("核算项重新计算")
    @PostMapping("/calculate")
    @Operation(summary = "*核算项重新计算")
    public R itemCalculate(@RequestBody @Validated KpiItemCalculateDTO dto) {
        String strPeriod = null;
        if (null != dto.getPeriod()) {
            strPeriod = dto.getPeriod().toString().substring(0, 4) + "-" + dto.getPeriod().toString().substring(4, 6);
        }
        KpiConfig one = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>().eq("period", dto.getPeriod()));

        String status = dto.getBusiType().equals("1") ? one.getIndexFlag() : one.getIndexFlagKs();
        if ("0".equals(status)) {
            throw new BizException("正在批量计算请稍等");
        }
        List<KpiAccountUnit> list = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("busi_type", dto.getBusiType())
                        .eq("name", "中治室")
        );
        Long zhizhishiId = null;
        if (list.size() > 0) {
            zhizhishiId = list.get(0).getId();
        }
        kpiItemService.itemCalculate(dto.getId(), null, strPeriod, true, zhizhishiId, null, null, null, null, dto.getBusiType());
        return R.ok();
    }

    @SysLog("核算项全部计算")
    @PostMapping("/calculate/all")
    @Operation(summary = "*核算项全部计算")
    public R itemBatchCalculate(@RequestBody KpiItemBatchCalculateDTO dto) {
        kpiItemService.itemBatchCalculate(null, dto.getBusiType(), dto.getPeriod(), YesNoEnum.NO.getValue());
        return R.ok();
    }

    @GetMapping("/extNum")
    @Operation(summary = "*核算项计算结果数量")
    public R<KpiItemExtVO> getExtNum(@Validated KpiItemQueryDTO dto) {
        return R.ok(kpiItemService.getItemExtInfo(dto));
    }

    @PostMapping("/saveCond")
    @Operation(summary = "*核算项保存条件")
    public R saveCond(@RequestBody @Validated KpiItemSaveCondDto dtos) {
        kpiItemService.saveCond(dtos);
        return R.ok();
    }

    @GetMapping("/getSql")
    @Operation(summary = "*获取核算项执行sql")
    public R getSql(@RequestParam(value = "id") Long id, @RequestParam(value = "period") String period) {
        return R.ok(kpiItemService.getSql(id, period));
    }
}