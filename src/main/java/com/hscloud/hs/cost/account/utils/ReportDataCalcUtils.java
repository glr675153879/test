package com.hscloud.hs.cost.account.utils;

import cn.hutool.extra.spring.SpringUtil;
import com.hscloud.hs.cost.account.model.entity.report.ReportDb;
import com.hscloud.hs.cost.account.model.entity.report.ReportField;
import com.hscloud.hs.cost.account.service.report.IReportFieldService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/5/11 9:56]
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class ReportDataCalcUtils {

    public List<Map<String, Object>> calcData(List<Map<String, Object>> dbData, ReportDb reportDb) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        SumCalculator sumTask = new SumCalculator(dbData, reportDb.getReportFields());
        List<Map<String, Object>> join = forkJoinPool.submit(sumTask).join();
        return join;
    }
}

class SumCalculator extends RecursiveTask<List<Map<String, Object>>> {
    // 阈值，超过这个值则拆分任务
    private static long THRESHOLD = 10;
    List<Map<String, Object>> dbData;
    List<ReportField> reportFields;

    public SumCalculator(List<Map<String, Object>> dbData, List<ReportField> reportFields) {
        this.dbData = dbData;
        this.reportFields = reportFields;
    }

    @Override
    protected List<Map<String, Object>> compute() {
        int length = dbData.size();
        if (length <= THRESHOLD) {
            return computeSequentially();
        }
        //将dbData平分为两个子列表

        int middle = length >>> 1;
        SumCalculator leftTask = new SumCalculator(dbData.subList(0, middle), reportFields);
        SumCalculator rightTask = new SumCalculator(dbData.subList(middle, length), reportFields);
        // 拆分左边任务
        leftTask.fork();
        // 拆分右边任务
        rightTask.fork();
        // 合并并返回结果
        List<Map<String, Object>> result = new ArrayList<>();
        result.addAll(leftTask.join());
        result.addAll(rightTask.join());
        return result;
    }

    private List<Map<String, Object>> computeSequentially() {
        IReportFieldService reportFieldService = SpringUtil.getBean(IReportFieldService.class);
        return dbData.stream().map(e ->
                reportFieldService.caclFieldFormula(e, reportFields)).collect(Collectors.toList());
    }

}
