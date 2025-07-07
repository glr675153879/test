package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemTableFieldDictItemMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.BaseIdStatusDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictItemDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableField;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictItem;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableFieldDictItemService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemTableFieldDictItemService extends ServiceImpl<KpiItemTableFieldDictItemMapper, KpiItemTableFieldDictItem>
        implements IKpiItemTableFieldDictItemService {
    private final KpiItemTableFieldDictItemMapper kpiItemTableFieldDictItemMapper;
    private final KpiItemTableFieldDictService kpiItemTableFieldDictService;

    @Override
    public Long saveOrUpdate(KpiItemTableFieldDictItemDto dto) {
        String dtoDictCode = dto.getDictCode();
        String dtoItemCode = dto.getItemCode();
        if (StringUtils.isBlank(dtoDictCode)) {
            throw new BizException("字典编码不能为空");
        }

        if (StringUtils.isBlank(dtoItemCode)) {
            throw new BizException("字典值编码不能为空");
        }

        KpiItemTableFieldDict dict = kpiItemTableFieldDictService.getByCode(dtoDictCode);
        if (dict == null) {
            throw new BizException("字典编码不存在");
        }

        KpiItemTableFieldDictItem dictItem = null == dto.getId() || dto.getId() == 0 ? new KpiItemTableFieldDictItem() : this.getById(dto.getId());
        String itemCode = dictItem.getItemCode();
        BeanUtils.copyProperties(dto, dictItem);

        LambdaQueryWrapper<KpiItemTableFieldDictItem> queryWrapper = Wrappers.<KpiItemTableFieldDictItem>lambdaQuery()
                .eq(KpiItemTableFieldDictItem::getDictCode, dtoDictCode)
                .eq(KpiItemTableFieldDictItem::getItemCode, dtoItemCode)
                .eq(KpiItemTableFieldDictItem::getDelFlag, EnableEnum.ENABLE.getType());

        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("该字典值编码已存在");
            }
            this.save(dictItem);
        } else {
            if (!dtoItemCode.equals(itemCode) && this.count(queryWrapper) > 0) {
                throw new BizException("该字典值编码已存在");
            }

            this.updateById(dictItem);
        }

        return dictItem.getId();
    }

    @Override
    public IPage<KpiItemTableFieldDictItem> getPage(KpiItemTableFieldDictItemDto dto) {
        return kpiItemTableFieldDictItemMapper.getPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
    }

    @Override
    public List<KpiItemTableFieldDictItem> getByDictCode(String dictCode) {
        return this.list(Wrappers.<KpiItemTableFieldDictItem>lambdaQuery()
                .eq(KpiItemTableFieldDictItem::getDictCode, dictCode)
                .eq(KpiItemTableFieldDictItem::getStatus, EnableEnum.ENABLE.getType()));
    }

    @Override
    public KpiItemTableFieldDictItem getByItemCode(String itemCode) {
        return this.getOne(Wrappers.<KpiItemTableFieldDictItem>lambdaQuery()
                .eq(KpiItemTableFieldDictItem::getItemCode, itemCode));
    }

    @Override
    public void switchStatus(BaseIdStatusDTO dto) {
        if (null == dto.getId() || dto.getId() == 0) {
            return;
        }

        KpiItemTableFieldDictItem dictItem = this.getById(dto.getId());
        if (null == dictItem || dto.getStatus().equals(dictItem.getStatus())) {
            return;
        }

        this.update(Wrappers.<KpiItemTableFieldDictItem>lambdaUpdate()
                .eq(KpiItemTableFieldDictItem::getId, dto.getId())
                .set(KpiItemTableFieldDictItem::getStatus, dto.getStatus()));
    }
}
