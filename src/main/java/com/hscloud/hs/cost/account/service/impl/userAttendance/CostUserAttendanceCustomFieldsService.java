package com.hscloud.hs.cost.account.service.impl.userAttendance;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.userAttendance.CostUserAttendanceCustomFieldsMapper;
import com.hscloud.hs.cost.account.model.dto.userAttendance.CostUserAttendanceCustomFieldsDto;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldsService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 人员考勤自定义字段表 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostUserAttendanceCustomFieldsService extends ServiceImpl<CostUserAttendanceCustomFieldsMapper, CostUserAttendanceCustomFields> implements ICostUserAttendanceCustomFieldsService {

    private static final String BASE62_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";

    /**
     * 每个月8号继承上个月的自定义字段
     */
    @XxlJob("importCostUserAttendanceCustomFieldsJob")
    @Transactional(rollbackFor = Exception.class)
    public void importData() throws IOException {
        // 自定义传参
        String jobParam = XxlJobHelper.getJobParam();
        String dt;
        String lastDt;
        if (StrUtil.isEmpty(jobParam)) {
            Date now = new Date();
            dt = DateUtil.format(DateUtil.offsetMonth(now, -1), "yyyyMM");
            lastDt = DateUtil.format(DateUtil.offsetMonth(now, -2), "yyyyMM");
        } else {
            dt = jobParam;
            DateTime yyyyMM = DateUtil.parse(jobParam, "yyyyMM");
            lastDt = DateUtil.format(DateUtil.offsetMonth(yyyyMM, -1), "yyyyMM");
        }
        // 删除dt对应月份的自定义字段
        remove(Wrappers.<CostUserAttendanceCustomFields>lambdaQuery().eq(CostUserAttendanceCustomFields::getDt, dt));
        // 查询上个月的自定义字段
        List<CostUserAttendanceCustomFields> lastMonthData = listByDt(lastDt);
        List<CostUserAttendanceCustomFields> dtMonthData = lastMonthData.stream().map(e -> {
            CostUserAttendanceCustomFields fields = new CostUserAttendanceCustomFields();
            fields.setDt(dt);
            fields.setName(e.getName());
            fields.setColumnId(e.getColumnId());
            fields.setDataType(e.getDataType());
            fields.setCode(e.getCode());
            fields.setStatus(e.getStatus());
            fields.setSortNum(e.getSortNum());
            fields.setRequireFlag(e.getRequireFlag());
            fields.setFieldType(e.getFieldType());
            fields.setFieldCheck(e.getFieldCheck());
            return fields;
        }).collect(Collectors.toList());
        saveBatch(dtMonthData);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<CostUserAttendanceCustomFields> listByDt(String dt) {
        LambdaQueryWrapper<CostUserAttendanceCustomFields> queryWrapper = Wrappers.<CostUserAttendanceCustomFields>lambdaQuery()
                .eq(CostUserAttendanceCustomFields::getDt, dt)
                .eq(CostUserAttendanceCustomFields::getStatus, "1")
                .orderByAsc(CostUserAttendanceCustomFields::getSortNum, CostUserAttendanceCustomFields::getId);
        return super.list(queryWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(List<Long> ids) {
        List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields = listByIds(ids);
        if (costUserAttendanceCustomFields != null) {
            for (CostUserAttendanceCustomFields costUserAttendanceCustomField : costUserAttendanceCustomFields) {
                if (costUserAttendanceCustomField.getStatus().equals("1")) {
                    costUserAttendanceCustomField.setStatus("0");
                } else {
                    costUserAttendanceCustomField.setStatus("1");
                }
            }
            return updateBatchById(costUserAttendanceCustomFields);
        }
        return false;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean saveUpdate(CostUserAttendanceCustomFieldsDto costUserAttendanceCustomFieldsDto) {
        List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields = costUserAttendanceCustomFieldsDto.getCustomParams();
        String dt = costUserAttendanceCustomFieldsDto.getDt();
        // int sortNum = 1;
        // 生成公式用编码
        for (CostUserAttendanceCustomFields costUserAttendanceCustomField : costUserAttendanceCustomFields) {
            // costUserAttendanceCustomField.setSortNum(sortNum++);
            costUserAttendanceCustomField.setDt(dt);
            if (costUserAttendanceCustomField.getId() != null) {
                continue;
            }
            if (costUserAttendanceCustomField.getColumnId() != null) {
                String columnId = costUserAttendanceCustomField.getColumnId();
                String base62 = "A" + toBase62(columnId);
                costUserAttendanceCustomField.setCode(base62);
            }
        }
        return saveOrUpdateBatch(costUserAttendanceCustomFields);
    }

    @Override
    public List<CostUserAttendanceCustomFields> listGroup() {
        return this.getBaseMapper().listGroup();
    }

    /**
     * long类型则转base62
     * 非long类型直接返回
     *
     * @param numStr
     * @return {@link String }
     */
    private static String toBase62(String numStr) {
        if (!NumberUtil.isLong(numStr)) {
            return numStr;
        }
        long num = NumberUtil.parseLong(numStr);
        if (num < 0) {
            throw new IllegalArgumentException("Number must be non-negative");
        }
        StringBuilder result = new StringBuilder();
        while (num > 0) {
            int index = (int) (num % 62);
            result.insert(0, BASE62_CHARS.charAt(index));
            num /= 62;
        }
        if (result.length() == 0) {
            result.append('0'); // Represent 0 as "0"
        }
        return result.toString();
    }

}
