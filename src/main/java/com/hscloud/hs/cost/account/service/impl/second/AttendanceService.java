package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.second.AttendanceMapper;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 考勤表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService extends ServiceImpl<AttendanceMapper, Attendance> implements IAttendanceService {


    @Override
    public String getLastCycle(String currentCycle) {
        Attendance attendance = this.baseMapper.selectOne(Wrappers.<Attendance>lambdaQuery()
                .lt(currentCycle != null, Attendance::getCycle, currentCycle)
                .orderByDesc(Attendance::getCycle)
                .last("limit 1"));
        if (attendance != null) {
            return attendance.getCycle();
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public IPage<Attendance> pageMoreDept(PageRequest<Attendance> pr, String cycle) {
        List<Attendance> attendances = list(Wrappers.<Attendance>lambdaQuery()
                .select(Attendance::getUserId)
                .eq(Attendance::getCycle, cycle)
                .groupBy(Attendance::getUserId)
                .having("count(1)>1"));
        log.info("转科人员有:{}", attendances);
        if (CollectionUtils.isEmpty(attendances)) {
            return pr.getPage();
        }
        List<Long> userIds = attendances.stream().filter(Objects::nonNull).map(Attendance::getUserId).distinct().collect(Collectors.toList());
        log.info("转科人员查询，userIds={}", userIds);
        QueryWrapper<Attendance> wrapper = pr.getWrapper().eq("cycle", cycle);
        if (CollectionUtils.isNotEmpty(userIds)) {
            wrapper = wrapper.in("user_id", userIds).orderByDesc("emp_name");
        }

        Page<Attendance> page = page(pr.getPage(), wrapper);
        List<Attendance> records = page.getRecords();
        if (CollectionUtils.isNotEmpty(records)) {
            records.forEach(item ->
            {
                item.setGroupWorkdays(String.format("%.2f", Double.parseDouble(item.getGroupWorkdays())));
                item.setWorkdays(String.format("%.2f", Double.parseDouble(item.getWorkdays())));
                item.setWorkdayszl(String.format("%.2f", Double.parseDouble(item.getWorkdayszl())));
            });
        }
        return page;
    }

    @Override
    public List<Attendance> listByTypeDept(String cycle) {
        return this.baseMapper.listByTypeDept(cycle);
    }

    @Override
    public List<Attendance> listByAccountUnitId(String cycle, Long accountUnitId) {
        return this.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, cycle)
                .eq(accountUnitId != null, Attendance::getAccountUnitId, String.valueOf(accountUnitId)));
    }

    @Override
    public List<Attendance> listByUserId(String cycle, Long userId) {
        return this.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, cycle)
                .eq(Attendance::getUserId, userId));
    }

    @Override
    public List<Attendance> getByCycleUserUnit(String cycle, List<Long> userIds, List<String> accountUnitIds) {
        if (StrUtil.isNotBlank(cycle)) {
            cycle = StrUtil.replace(cycle, "-", "");
        }
        return this.list(Wrappers.<Attendance>lambdaQuery()
                        .eq(Attendance::getCycle, cycle)
                        .in(userIds != null && !userIds.isEmpty(), Attendance::getUserId, userIds)
                        .in(accountUnitIds != null && !accountUnitIds.isEmpty(), Attendance::getAccountUnitId, accountUnitIds)
                //.last("limit 1")
        );
    }

    @Override
    public List<Attendance> listByUserIdsAndCycle(List<Long> userIds, String cycle) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyList();
        }
        return this.list(Wrappers.<Attendance>lambdaQuery()
                .in(Attendance::getUserId, userIds)
                .eq(Attendance::getCycle, cycle));
    }

    @Override
    public List<Attendance> listByCycle(String cycle) {
        return this.list(Wrappers.<Attendance>lambdaQuery().eq(Attendance::getCycle, cycle));
    }
}
