package com.hscloud.hs.cost.account.controller.dataReport;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.dataReport.ReportRecordStatusEnum;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportTaskManageDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportRecord;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportRecordService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
* 上报管理
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/dataReport/costReportTaskManage")
@Tag(name = "costReportTask", description = "上报管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class CostReportTaskManageController {

    private final ICostReportRecordService costReportRecordService;


    @SysLog("上报管理-详情page")
    @GetMapping("/pageDetail")
    @Operation(summary = "上报管理-详情page")
    public R<IPage<CostReportRecord>> pageDetail(PageRequest<CostReportRecord> pr) {
        LambdaQueryWrapper<CostReportRecord> qr = pr.getWrapper().lambda();
        qr.orderByDesc(CostReportRecord::getReportTime);
        IPage<CostReportRecord> pageData = costReportRecordService.page(pr.getPage(),qr);
        List<CostReportRecord> filteredRecords = new ArrayList<>(pageData.getRecords());
        pageData.setRecords(filteredRecords);
        return R.ok(pageData);
    }

    @SysLog("上报管理page")
    @GetMapping("/page")
    @Operation(summary = "上报管理page")
    public R <Page<CostReportTaskManageDto>> page(PageRequest<CostReportTaskManageDto> pr) {
        Page<CostReportTaskManageDto> page = new Page<>();
        // 声明出参
        List<CostReportTaskManageDto> resultList = new ArrayList<>();

        // 条件构造器
        LambdaQueryWrapper<CostReportRecord> qr = new LambdaQueryWrapper<>();
        qr.orderByDesc(CostReportRecord::getCalculateCircle);

        if(pr.getQ().containsKey("type")) {
            qr.eq(CostReportRecord::getType, pr.getQ().get("type"));
        }

        // 获取所有相关数据
        List<CostReportRecord> list = costReportRecordService.list(qr);
        // 核算周期列表
        List<String> calculateCircleList = list.stream().map(CostReportRecord::getCalculateCircle).distinct().collect(Collectors.toList());

        for (String calculateCircle : calculateCircleList) {
            CostReportTaskManageDto dto = new CostReportTaskManageDto();
            // 核算周期名称
            dto.setCalculateCircle(calculateCircle);
            // 上报任务总数量
            dto.setTotalCount(list.stream().filter(x -> x.getCalculateCircle().equals(calculateCircle)).count());
            // 未下发数量
            dto.setUnassignedCount(list.stream().filter(x -> ReportRecordStatusEnum.INIT.getVal().equals(x.getStatus())
                    && (x.getCalculateCircle().equals(calculateCircle))).count());
            // 已上报数量
            dto.setReportedCount(list.stream().filter(x -> (ReportRecordStatusEnum.REPORTED.getVal().equals(x.getStatus())
                    ||ReportRecordStatusEnum.APPROVE.getVal().equals(x.getStatus())) && (x.getCalculateCircle().equals(calculateCircle))).count());
            // 已下发未上报数量
            dto.setToReportCount(list.stream().filter(x -> (ReportRecordStatusEnum.UNREPORT.getVal().equals(x.getStatus())
                    ||ReportRecordStatusEnum.EXPIRED.getVal().equals(x.getStatus())
                    ||ReportRecordStatusEnum.REJECT.getVal().equals(x.getStatus())) && (x.getCalculateCircle().equals(calculateCircle))).count());
            // 待审核数量
            dto.setPendingCount(list.stream().filter(x -> (ReportRecordStatusEnum.REPORTED.getVal().equals(x.getStatus())
                    && (x.getCalculateCircle().equals(calculateCircle)))).count());
            // 审核通过数量
            dto.setApproveCount(list.stream().filter(x -> (ReportRecordStatusEnum.APPROVE.getVal().equals(x.getStatus())
                    && (x.getCalculateCircle().equals(calculateCircle)))).count());
            // 审核驳回数量
            dto.setRejectCount(list.stream().filter(x -> (ReportRecordStatusEnum.REJECT.getVal().equals(x.getStatus())
                    && (x.getCalculateCircle().equals(calculateCircle)))).count());
            //如果该核算周期 上报数为0，则不显示
            if((dto.getToReportCount() + dto.getReportedCount()) == 0){
                continue;
            }
            resultList.add(dto);
        }
        page.setCurrent(pr.getCurrent());
        page.setSize(pr.getSize());
//        page.setTotal(pr.getTotal());
        page.setRecords(resultList);

        return R.ok(page);
    }

}