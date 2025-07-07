package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemTableMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTable;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * 核算项基础表 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemTableService extends ServiceImpl<KpiItemTableMapper, KpiItemTable> implements IKpiItemTableService {

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdate(KpiItemTableDto dto) {
        String dtoTableName = dto.getTableName();
        if (StringUtils.isBlank(dtoTableName)) {
            throw new BizException("表名不能为空");
        }

        String[] split = dtoTableName.split("\\.");
        if (split.length != 2) {
            throw new BizException("表名格式不正确");
        }

        String schemaName = split[0];
        String tableName = split[1];
        if (StringUtils.isBlank(schemaName) || StringUtils.isBlank(tableName)) {
            throw new BizException("表名格式不正确");
        }

        KpiItemTable kpiItemTable = null == dto.getId() || dto.getId() == 0 ? new KpiItemTable() : this.getById(dto.getId());
        String name = kpiItemTable.getTableName();
        BeanUtils.copyProperties(dto, kpiItemTable);

        LambdaQueryWrapper<KpiItemTable> queryWrapper = Wrappers.<KpiItemTable>lambdaQuery()
                .eq(KpiItemTable::getTableName, dto.getTableName())
                .eq(KpiItemTable::getDelFlag, EnableEnum.ENABLE.getType());
        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("该基础表已存在");
            }
            this.save(kpiItemTable);
        } else {
            if (!dtoTableName.equals(name) && this.count(queryWrapper) > 0) {
                throw new BizException("该基础表已存在");
            }
            this.updateById(kpiItemTable);
        }

        return kpiItemTable.getId();
    }
}
