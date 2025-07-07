package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.report.QueryFieldBySqlDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.report.ReportDb;
import com.hscloud.hs.cost.account.model.vo.report.MetaDataBySqlVo;
import com.hscloud.hs.cost.account.service.report.IReportDbService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 数据集设计表
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/report/reportDb")
@Tag(name = "报表中心-数据集", description = "数据集设计表")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ReportDbController {

    private final IReportDbService reportDbService;

    @SysLog("数据集设计表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "数据集设计表info")
    public R<ReportDb> info(@PathVariable Long id) {
        return R.ok(reportDbService.getById(id));
    }

    @SysLog("数据集设计表page")
    @GetMapping("/page")
    @Operation(summary = "数据集设计表page")
    public R<IPage<ReportDb>> page(PageRequest<ReportDb> pr) {
        return R.ok(reportDbService.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("数据集设计表list")
    @GetMapping("/list")
    @Operation(summary = "数据集设计表list")
    public R<List<ReportDb>> list(PageRequest<ReportDb> pr) {
        return R.ok(reportDbService.list(pr.getWrapper()));
    }

    @SysLog("保存或更新数据集")
    @PostMapping("/save")
    @Operation(summary = "保存或更新数据集")
    public R<Long> save(@RequestBody ReportDb reportDb) {
        return R.ok(reportDbService.createOrEdit(reportDb));
    }

    @SysLog("数据集设计表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "数据集设计表del")
    public R del(@PathVariable Long id) {
        return R.ok(reportDbService.removeById(id));
    }

    @SysLog("数据集设计表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "数据集设计表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(reportDbService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @GetMapping("/loadDbData/{id}")
    @Operation(summary = "加载数据集数据")
    public R<ReportDb> loadDbData(@PathVariable Long id) {
        ReportDb data = reportDbService.loadDbData(id);
        if (Objects.isNull(data)) {
            throw new BizException("找不到数据集");
        }
        return R.ok(data);
    }

    @PostMapping("/queryFieldBySql")
    @Operation(summary = "根据sql查询字段")
    public R<List<MetaDataBySqlVo>> queryFieldBySql(@RequestBody QueryFieldBySqlDto dto) {
        return R.ok(reportDbService.queryFieldBySql(dto));
    }

}