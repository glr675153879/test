package com.hscloud.hs.cost.account.utils;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author tianbo
 * @version 1.0
 * @date 2024-08-09 11:18
 **/
@Component
public class OperationUtil {

    @Autowired
    private RemoteUserService remoteUserService;

    public <T extends BaseEntity<T>> Map<Long, String> getUserMap(List<T> records) {
        List<Long> userIds = records.stream()
                .flatMap(e -> Stream.concat(
                        Stream.of(e.getCreateBy()),
                        Stream.of(e.getUpdateBy())
                )).filter(StrUtil::isNotBlank).map(Long::valueOf).distinct()
                .collect(Collectors.toList());
        Map<Long, String> userMap = new HashMap<>();
        if (CollectionUtils.isEmpty(userIds)) {
            return userMap;
        }
        List<SysUser> sysUsers = remoteUserService.getUserList(userIds).getData();
        if (CollectionUtils.isNotEmpty(sysUsers)) {
            userMap = sysUsers.stream().collect(Collectors.toMap(SysUser::getUserId, SysUser::getName, (v1, v2) -> v1));
        }

        return userMap;

    }

    public <T extends BaseEntity<T>> String getOperationName(Map<Long, String> userMap, T record) {
        String operationId = StrUtil.isNotBlank(record.getUpdateBy()) ? record.getUpdateBy() : record.getCreateBy();
        return userMap.get(Long.valueOf(operationId));
    }

    public <T extends BaseEntity<T>> LocalDateTime getOperationTime(T record) {
        return Objects.nonNull(record.getUpdateTime()) ? record.getUpdateTime() : record.getCreateTime();
    }
}
