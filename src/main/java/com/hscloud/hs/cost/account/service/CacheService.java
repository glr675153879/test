package com.hscloud.hs.cost.account.service;


import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.ItemDimensionEnum;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class CacheService {


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private SqlUtil sqlUtil;


    public String getItem(String sql, Long itemId, String dimension, Map<String, String> params) {

        String itemKey = String.format(CacheConstants.COST_ITEM_DIMENSION, dimension, params.get("startTime"), params.get("endTime"), itemId);
        if (ItemDimensionEnum.DEPT.getCode().equals(dimension)) {
            String deptCode = params.get("dept_code");
            return getItemValue(itemKey, deptCode, sql, params);
        } else if (ItemDimensionEnum.USER.getCode().equals(dimension)) {
            String userId = params.get("user_id");
            return getItemValue(itemKey, userId, sql, params);
        } else if (ItemDimensionEnum.DEPT_UNIT.getCode().equals(dimension)) {
            String deptUnitCode = params.get("unit_id");
            return getItemValue(itemKey, deptUnitCode, sql, params);
        } else {
            throw new BizException("不支持的sql维度");
        }

    }


    private String getItemValue(String itemKey, String bizId, String sql, Map<String, String> params) {
        Object value = redisUtil.hGet(itemKey, bizId);
        if (value == null) {
            value = sqlUtil.executeSql(sql, params);
            if (value == null) {
                value = "0.0";
            }
            redisUtil.hSet(itemKey, bizId, value);
        }
        return String.valueOf(value);
    }




}
