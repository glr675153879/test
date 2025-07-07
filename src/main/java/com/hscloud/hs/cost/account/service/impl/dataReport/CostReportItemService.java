package com.hscloud.hs.cost.account.service.impl.dataReport;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportItemMapper;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemPageDto;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemService;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportRecordService;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
* 上报项 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportItemService extends ServiceImpl<CostReportItemMapper, CostReportItem> implements ICostReportItemService {

    private final ICostReportTaskService costReportTaskService;

    @Override
    public Boolean activate(CostReportItem costReportItem) {
        CostReportItem newCostReportItem = getById(costReportItem.getId());
        if("1".equals(newCostReportItem.getStatus())){
            newCostReportItem.setStatus("0");
            updateById(newCostReportItem);
        }else if("0".equals(newCostReportItem.getStatus())){
            newCostReportItem.setStatus("1");
            updateById(newCostReportItem);
        }
        return true;
    }

    @Override
    public IPage<CostReportItemPageDto> pageData(Page<CostReportItem> page, QueryWrapper<CostReportItem> wrapper) {
        IPage<CostReportItem> pageData = this.page(page, wrapper);
        List<CostReportItemPageDto> dtoList = pageData.getRecords().stream()
                .map(reportItem -> {
                    CostReportItemPageDto dto = new CostReportItemPageDto();
                    BeanUtils.copyProperties(reportItem, dto);
                    String itemIds = String.valueOf(dto.getId());
                    List<CostReportTask> list = costReportTaskService.list(new LambdaQueryWrapper<CostReportTask>()
                            .like(StringUtils.isNotEmpty(itemIds), CostReportTask::getItemList, itemIds));
                    dto.setIsUsed(CollectionUtil.isNotEmpty(list) ? "1" : "0");
                    return dto;
                })
                .collect(Collectors.toList());
        Page<CostReportItemPageDto> reportItemPage = new Page<>();
        BeanUtils.copyProperties(pageData, reportItemPage, "records");
        reportItemPage.setRecords(dtoList);
        return reportItemPage;
    }

    @Override
    public Boolean isUsed(Long id) {

        // 判断当前核算项是否使用在运行中的任务中
        Boolean ifUsed = false;

        // 获取所有启用中的任务
        List<CostReportTask> costReportTasks = costReportTaskService.list(Wrappers.<CostReportTask>lambdaQuery()
                .eq(CostReportTask::getStatus, "0"));

        // 获取所有的上报项并合并去重
        ifUsed = costReportTasks.stream()
                .map(r -> r.queryItemVoList()).collect(Collectors.toList())
                .stream().flatMap(List::stream).distinct().collect(Collectors.toList())
                .stream().anyMatch(r -> id.equals(r.getId()));

        return ifUsed;
    }

}
