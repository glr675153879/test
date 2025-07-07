package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemTableFieldMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTable;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableField;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableFieldService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemTableFieldService extends ServiceImpl<KpiItemTableFieldMapper, KpiItemTableField> implements IKpiItemTableFieldService {

    private final KpiItemTableService kpiItemTableService;
    private final KpiItemTableFieldMapper kpiItemTableFieldMapper;
    private final KpiItemTableFieldDictService kpiItemTableFieldDictService;

    @Override
    public List<KpiItemTableFieldVO> getListByTableId(Long tableId) {
        List<KpiItemTableField> list = this.list(Wrappers.<KpiItemTableField>lambdaQuery()
                .eq(KpiItemTableField::getTableId, tableId));

        Map<String, String> dictMap = kpiItemTableFieldDictService.list().stream()
                .collect(Collectors.toMap(KpiItemTableFieldDict::getDictCode, KpiItemTableFieldDict::getDictName));

        KpiItemTable table = kpiItemTableService.getById(tableId);

        return list.stream()
                .map(field -> {
                    KpiItemTableFieldVO vo = new KpiItemTableFieldVO();
                    BeanUtils.copyProperties(field, vo);
                    vo.setDictName(dictMap.get(field.getDictCode()));
                    vo.setTableName(null == table ? "" : table.getTableName());
                    return vo;
                }).collect(Collectors.toList());
    }

    @Override
    public void switchStatus(BaseIdStatusDTO dto) {
        if (null == dto.getId() || dto.getId() == 0) {
            return;
        }

        KpiItemTableField kpiItemTableField = this.getById(dto.getId());
        if (null == kpiItemTableField || dto.getStatus().equals(kpiItemTableField.getStatus())) {
            return;
        }

        LambdaUpdateWrapper<KpiItemTableField> updateWrapper = Wrappers.<KpiItemTableField>lambdaUpdate()
                .eq(KpiItemTableField::getId, dto.getId())
                .set(KpiItemTableField::getStatus, dto.getStatus());
        this.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<KpiItemTableField> saveFields(Long tableId) {
        if (null == tableId || tableId == 0) {
            throw new BizException("表id不正确");
        }

        KpiItemTable table = kpiItemTableService.getById(tableId);
        if (null == table) {
            throw new BizException("表不存在");
        }

        String name = table.getTableName();
        String[] split = name.split("\\.");
        if (split.length != 2) {
            throw new BizException("表名不正确");
        }

        String schemaName = split[0];
        String tableName = split[1];
        if (StringUtils.isBlank(schemaName) || StringUtils.isBlank(tableName)) {
            throw new BizException("表名不正确");
        }

        // 删除原有的字段
        this.remove(Wrappers.<KpiItemTableField>lambdaQuery()
                .eq(KpiItemTableField::getTableId, tableId));

        List<KpiItemTableField> fields = kpiItemTableFieldMapper.getFields(schemaName, tableName);
        fields.forEach(field -> {
            field.setTableId(tableId);
            field.setStatus(EnableEnum.ENABLE.getType());
            field.setTenantId(table.getTenantId());
        });

        this.saveBatch(fields);

        return fields;
    }

    @Override
    public IPage<KpiItemTableFieldVO> getPage(KpiItemTableFieldDto dto) {
        IPage<KpiItemTableField> page = kpiItemTableFieldMapper.getPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);

        Map<String, String> dictMap = kpiItemTableFieldDictService.list().stream()
                .collect(Collectors.toMap(KpiItemTableFieldDict::getDictCode, KpiItemTableFieldDict::getDictName));

        IPage<KpiItemTableFieldVO> result = new Page<>();
        BeanUtils.copyProperties(page, result);

        List<KpiItemTableFieldVO> list = new ArrayList<>();
        page.getRecords().forEach(item -> {
            KpiItemTableFieldVO vo = new KpiItemTableFieldVO();
            BeanUtils.copyProperties(item, vo);
            vo.setDictName(dictMap.get(item.getDictCode()));
            list.add(vo);
        });

        result.setRecords(list);

        return result;
    }

    @Override
    public void updateDictCodeById(Long id, String dictCode) {
        if (!StringUtils.isBlank(dictCode)) {
            KpiItemTableFieldDict dict = kpiItemTableFieldDictService.getByCode(dictCode);
            if (null == dict) {
                throw new BizException("字典编码不存在");
            }
        }

        this.update(Wrappers.<KpiItemTableField>lambdaUpdate()
                .set(KpiItemTableField::getDictCode, dictCode)
                .eq(KpiItemTableField::getId, id));
    }

    @Override
    public Long saveOrUpdate(KpiItemTableFieldDto dto) {
        Long dtoTableId = dto.getTableId();
        String dtoFieldName = dto.getFieldName();
        String dtoFieldType = dto.getFieldType();

        if (null == dtoTableId || dtoTableId == 0) {
            throw new BizException("表id不正确");
        }

        if (StringUtils.isBlank(dtoFieldName)) {
            throw new BizException("字段名不能为空");
        }

        if (StringUtils.isBlank(dtoFieldType)) {
            throw new BizException("字段类型不能为空");
        }

        KpiItemTable table = kpiItemTableService.getById(dtoTableId);
        if (null == table) {
            throw new BizException("表不存在");
        }

        KpiItemTableField field = null == dto.getId() || dto.getId() == 0 ? new KpiItemTableField() : this.getById(dto.getId());
        String fieldName = field.getFieldName();
        Long tableId = field.getTableId();
        BeanUtils.copyProperties(dto, field);

        LambdaQueryWrapper<KpiItemTableField> queryWrapper = Wrappers.<KpiItemTableField>lambdaQuery()
                .eq(KpiItemTableField::getTableId, dtoTableId)
                .eq(KpiItemTableField::getFieldName, dtoFieldName);

        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("字段名已存在");
            }

            this.save(field);
        } else {
            if (!dtoTableId.equals(tableId)) {
                throw new BizException("表id不一致");
            }

            if (!dtoFieldName.equals(fieldName) && this.count(queryWrapper) > 0) {
                throw new BizException("字段名已存在");
            }

            this.updateById(field);
        }

        return field.getId();
    }

    @Override
    public List<KpiItemTableFieldVO> getListByTableIds(List<Long> tableIdList) {
        List<KpiItemTableField> list = this.list(Wrappers.<KpiItemTableField>lambdaQuery()
                .in(KpiItemTableField::getTableId, tableIdList));

        return list.stream()
                .map(field -> {
                    KpiItemTableFieldVO vo = new KpiItemTableFieldVO();
                    BeanUtils.copyProperties(field, vo);
                    return vo;
                }).collect(Collectors.toList());
    }
}
