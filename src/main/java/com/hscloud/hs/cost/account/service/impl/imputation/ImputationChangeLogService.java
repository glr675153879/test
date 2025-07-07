package com.hscloud.hs.cost.account.service.impl.imputation;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationChangeLogExcel;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationChangeLogMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationChangeLog;
import com.hscloud.hs.cost.account.service.imputation.IImputationChangeLogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 归集变更日志 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationChangeLogService extends ServiceImpl<ImputationChangeLogMapper, ImputationChangeLog> implements IImputationChangeLogService {
    private final RemoteUserService remoteUserService;

    @Override
    public IPage<ImputationChangeLog> pageImputationChangeLog(Page<ImputationChangeLog> page, QueryWrapper<ImputationChangeLog> wrapper, Long imputationId) {
        Page<ImputationChangeLog> changeLogPage = page(page, wrapper.eq("imputation_id", imputationId).orderBy(true, false, "create_time"));
        List<ImputationChangeLog> records = changeLogPage.getRecords();
        fillUserNames(records);
        return changeLogPage;
    }

    @Override
    public IPage<ImputationChangeLog> pageImputationChangeLog(Page<ImputationChangeLog> page, QueryWrapper<ImputationChangeLog> wrapper) {
        Page<ImputationChangeLog> changeLogPage = page(page, wrapper.orderBy(true, false, "create_time"));
        List<ImputationChangeLog> records = changeLogPage.getRecords();
        fillUserNames(records);
        return changeLogPage;
    }


    @Override
    public List<ImputationChangeLogExcel> exportChangeLog(Long imputationId, String changeType, String changeModel) {
        LambdaQueryWrapper<ImputationChangeLog> wrapper = Wrappers.<ImputationChangeLog>lambdaQuery().eq(ImputationChangeLog::getImputationId, imputationId);
        if (Objects.nonNull(changeType)) {
            wrapper.like(ImputationChangeLog::getChangeType, changeType);
        }
        if (Objects.nonNull(changeModel)){
            wrapper.like(ImputationChangeLog::getChangeModel, changeModel);
        }
        List<ImputationChangeLog> changeLogs = list(wrapper);
        fillUserNames(changeLogs);
        List<ImputationChangeLogExcel> imputationChangeLogExcels = Optional.ofNullable(changeLogs).map(list -> list.stream().map(item -> {
            ImputationChangeLogExcel imputationChangeLogExcel = new ImputationChangeLogExcel();
            BeanUtils.copyProperties(item, imputationChangeLogExcel);
            return imputationChangeLogExcel;
        }).collect(Collectors.toList())).orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(imputationChangeLogExcels)) {
            imputationChangeLogExcels.add(new ImputationChangeLogExcel());
        }
        return imputationChangeLogExcels;
    }

    @Override
    public List<ImputationChangeLogExcel> exportChangeLog(String changeType, String changeModel,String imputationCode) {
        LambdaQueryWrapper<ImputationChangeLog> wrapper = new LambdaQueryWrapper<>();
        if (Objects.nonNull(changeType)) {
            wrapper.like(ImputationChangeLog::getChangeType, changeType);
        }
        if (Objects.nonNull(changeModel)){
            wrapper.like(ImputationChangeLog::getChangeModel, changeModel);
        }
        if (Objects.nonNull(imputationCode)){
            wrapper.eq(ImputationChangeLog::getImputationCode, imputationCode);
        }
        List<ImputationChangeLog> changeLogs = list(wrapper);
        fillUserNames(changeLogs);
        List<ImputationChangeLogExcel> imputationChangeLogExcels = Optional.ofNullable(changeLogs).map(list -> list.stream().map(item -> {
            ImputationChangeLogExcel imputationChangeLogExcel = new ImputationChangeLogExcel();
            BeanUtils.copyProperties(item, imputationChangeLogExcel);
            return imputationChangeLogExcel;
        }).collect(Collectors.toList())).orElse(Collections.emptyList());

        if (CollectionUtils.isEmpty(imputationChangeLogExcels)) {
            imputationChangeLogExcels.add(new ImputationChangeLogExcel());
        }
        return imputationChangeLogExcels;
    }

    private void fillUserNames(List<ImputationChangeLog> records) {
        if (CollectionUtils.isNotEmpty(records)) {
            //物资收费管理 新增时操作人为中文系统
            List<Long> userIds = records.stream().filter(item -> !Objects.isNull(item.getChangeUserName()) && !"系统".equals(item.getChangeUserName())).map(item -> Long.parseLong(item.getChangeUserName())).collect(Collectors.toList());
            if(CollectionUtils.isEmpty(userIds)) {
                return;
            }
            List<SysUser> userList = remoteUserService.getUserList(userIds).getData();
            Map<Long, String> userMap = userList.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getName, (v1, v2) -> v1));
            records.forEach(record -> {
                String changeUserName = record.getChangeUserName();
                if (StringUtils.isNotBlank(changeUserName)&&!"系统".equals(changeUserName)) {
                    //物资收费管理 新增时操作人为中文系统
                    record.setChangeUserName(userMap.get(Long.parseLong(changeUserName)));
                }

            });
        }
    }
}
