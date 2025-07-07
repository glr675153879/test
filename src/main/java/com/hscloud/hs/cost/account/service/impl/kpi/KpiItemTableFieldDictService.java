package com.hscloud.hs.cost.account.service.impl.kpi;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemTableFieldDictMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableFieldDictService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemTableFieldDictService extends ServiceImpl<KpiItemTableFieldDictMapper, KpiItemTableFieldDict>
        implements IKpiItemTableFieldDictService {

    private final KpiItemTableFieldDictMapper kpiItemTableFieldDictMapper;

    @Override
    public Long saveOrUpdate(KpiItemTableFieldDictDto dto) {
        String dtoDictCode = dto.getDictCode();
        if (StringUtils.isBlank(dtoDictCode)) {
            throw new BizException("字典编码不能为空");
        }

        KpiItemTableFieldDict dict = null == dto.getId() || dto.getId() == 0 ? new KpiItemTableFieldDict() : this.getById(dto.getId());
        String dictCode = dict.getDictCode();
        BeanUtils.copyProperties(dto, dict);

        LambdaQueryWrapper<KpiItemTableFieldDict> queryWrapper = Wrappers.<KpiItemTableFieldDict>lambdaQuery()
                .eq(KpiItemTableFieldDict::getDictCode, dto.getDictCode())
                .eq(KpiItemTableFieldDict::getDelFlag, EnableEnum.ENABLE.getType());

        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("该字典编码已存在");
            }
            this.save(dict);
        } else {
            if (!dtoDictCode.equals(dictCode) && this.count(queryWrapper) > 0) {
                throw new BizException("该字典编码已存在");
            }
            this.updateById(dict);
        }

        return dict.getId();
    }

    @Override
    public IPage<KpiItemTableFieldDict> getPage(KpiItemTableFieldDictDto dto) {
        return kpiItemTableFieldDictMapper.getPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
    }

    @Override
    public KpiItemTableFieldDict getByCode(String code) {
        return this.getOne(Wrappers.<KpiItemTableFieldDict>lambdaQuery()
                .eq(KpiItemTableFieldDict::getDictCode, code));
    }
}
