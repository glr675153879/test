package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.CostUnitChangeLog;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CostUnitChangeLogExcel;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 16:18
 **/
public interface ICostUnitChangeLogService extends IService<CostUnitChangeLog> {
    List<CostUnitChangeLogExcel> exportCostUnitChangeLog(String type, String userIds, LocalDateTime startTime, LocalDateTime endTime);

    IPage<CostUnitChangeLog> pageCostUnitChangeLog(Page<CostUnitChangeLog> page, QueryWrapper<CostUnitChangeLog> wrapper);
}
