package com.hscloud.hs.cost.account.utils;

import com.alibaba.fastjson.JSONObject;
import com.hscloud.hs.cost.account.model.dto.dataReport.DataReportUserDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.MeasureDto;
import com.hscloud.hs.cost.account.model.dto.userAttendance.AccountUnitDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostClusterUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.pig4cloud.pigx.admin.api.entity.DeptThird;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.entity.ThirdAccountUnit;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 转化为通用数据格式
 */
@Component
@Slf4j
public class DataProcessUtil {

    public <T> String processList(List<T> list) {
        cn.hutool.json.JSONArray jsonArray = new cn.hutool.json.JSONArray();
        try {
            for (T item : list) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", getId(item));
                jsonObject.put("name", getName(item));
                jsonArray.put(jsonObject);
            }
        } catch (Exception e) {
            // Handle exception
        }
        return jsonArray.toString();
    }

    //按照需求添加目标对象
    private <T> Object getId(T item) {
        if (item instanceof MeasureDto) {
            return ((MeasureDto) item).getId().toString();
        } else if (item instanceof CostClusterUnit) {
            return ((CostClusterUnit) item).getId().toString();
        } else if (item instanceof SysUser) {
            return ((SysUser) item).getUserId().toString();
        } else if (item instanceof DeptThird) {
            return ((DeptThird)item).getId().toString();
        } else if (item instanceof CostAccountUnit) {
            return ((CostAccountUnit)item).getId().toString();
        } else if (item instanceof ThirdAccountUnit) {
            return ((ThirdAccountUnit)item).getId().toString();
        } else if (item instanceof UserVO) {
            return ((UserVO)item).getUserId().toString();
        } else if (item instanceof DataReportUserDto) {
            return ((DataReportUserDto)item).getId();
        } else if (item instanceof AccountUnitDto) {
            return ((AccountUnitDto)item).getId();
        } else if (item instanceof KpiAccountUnit) {
            return ((KpiAccountUnit)item).getId().toString();
        }
        return null;
    }

    private <T> String getName(T item) {
        if (item instanceof MeasureDto) {
            return ((MeasureDto) item).getName();
        } else if (item instanceof CostClusterUnit) {
            return ((CostClusterUnit) item).getName();
        } else if (item instanceof SysUser) {
            return ((SysUser) item).getName();
        } else if (item instanceof DeptThird) {
            return ((DeptThird)item).getName();
        } else if (item instanceof CostAccountUnit) {
            return ((CostAccountUnit)item).getName();
        } else if (item instanceof ThirdAccountUnit) {
            return ((ThirdAccountUnit)item).getThirdUnitName();
        } else if (item instanceof UserVO) {
            return ((UserVO)item).getName();
        } else if (item instanceof DataReportUserDto) {
            return ((DataReportUserDto) item).getName();
        } else if (item instanceof AccountUnitDto) {
            return ((AccountUnitDto) item).getName();
        } else if (item instanceof KpiAccountUnit) {
            return ((KpiAccountUnit)item).getName().toString();
        }
        return null;
    }
}
