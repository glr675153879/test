package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostUnitChangeLogMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.CostUnitChangeLog;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CostUnitChangeLogExcel;
import com.hscloud.hs.cost.account.service.ICostUnitChangeLogService;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 16:19
 **/
@Service
public class CostUnitChangeLogServiceImpl extends ServiceImpl<CostUnitChangeLogMapper, CostUnitChangeLog> implements ICostUnitChangeLogService {

    @Autowired
    private RemoteUserService remoteUserService;

    @Override
    public List<CostUnitChangeLogExcel> exportCostUnitChangeLog(String type, String userIds, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<CostUnitChangeLog> wrapper = Wrappers.<CostUnitChangeLog>lambdaQuery().eq(CostUnitChangeLog::getType, type);
        if (Objects.nonNull(startTime)) {
            wrapper.ge(CostUnitChangeLog::getOperationTime, startTime);
        }
        if (Objects.nonNull(endTime)) {
            wrapper.le(CostUnitChangeLog::getOperationTime, endTime);
        }
        if (StrUtil.isNotBlank(userIds)) {
            wrapper.in(CostUnitChangeLog::getOperatorId, Arrays.asList(userIds.split(",")));
        }

        List<CostUnitChangeLog> costUnitChangeLogs = list(wrapper);
        if (CollectionUtils.isEmpty(costUnitChangeLogs)) {
            return CollUtil.newArrayList(new CostUnitChangeLogExcel());
        }
        fillUserName(costUnitChangeLogs);
        return BeanUtil.copyToList(costUnitChangeLogs, CostUnitChangeLogExcel.class);
    }


    @Override
    public IPage<CostUnitChangeLog> pageCostUnitChangeLog(Page<CostUnitChangeLog> page, QueryWrapper<CostUnitChangeLog> wrapper) {
        Page<CostUnitChangeLog> changeLogPage = page(page, wrapper.orderByDesc("create_time"));
        List<CostUnitChangeLog> records = changeLogPage.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return changeLogPage;
        }
        fillUserName(records);
        return changeLogPage;
    }

    private void fillUserName(List<CostUnitChangeLog> records) {
        List<Long> userIds = records.stream().map(CostUnitChangeLog::getOperatorId).filter(Objects::nonNull).map(Long::valueOf).distinct().collect(Collectors.toList());
        List<SysUser> sysUsers = remoteUserService.getUserList(userIds).getData();
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            Map<Long, String> userMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getName, (v1, v2) -> v1));
            records.forEach(item -> item.setOperatorName(userMap.get(Long.valueOf(item.getOperatorId()))));
        }
    }
}
