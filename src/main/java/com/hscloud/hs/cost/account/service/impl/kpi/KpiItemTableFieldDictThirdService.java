package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DateUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemTableFieldDictThirdMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemTableFieldDictThirdDto;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDict;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemTableFieldDictThird;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldDictThirdVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemTableFieldDictThirdService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemTableFieldDictThirdService extends ServiceImpl<KpiItemTableFieldDictThirdMapper, KpiItemTableFieldDictThird>
        implements IKpiItemTableFieldDictThirdService {
    private final KpiItemTableFieldDictThirdMapper kpiItemTableFieldDictThirdMapper;
    private final KpiItemTableFieldDictItemService kpiItemTableFieldDictItemService;
    private final KpiItemTableFieldDictService kpiItemTableFieldDictService;

    @Override
    public Long saveOrUpdate(KpiItemTableFieldDictThirdDto dto) {
        String thirdItemCode = dto.getThirdItemCode();
        String dictCode = dto.getDictCode();
        String itemCode = dto.getItemCode();
        if (StringUtils.isBlank(thirdItemCode) || StringUtils.isBlank(dictCode) || StringUtils.isBlank(itemCode)) {
            throw new BizException("编码不能为空");
        }

        KpiItemTableFieldDict dict = kpiItemTableFieldDictService.getByCode(dictCode);
        if (dict == null) {
            throw new BizException("字典编码不存在");
        }

        KpiItemTableFieldDictItem dictItem = kpiItemTableFieldDictItemService.getByItemCode(itemCode);
        if (dictItem == null) {
            throw new BizException("字典值编码不存在");
        } else if (!dictCode.equals(dictItem.getDictCode())) {
            throw new BizException("字典值编码不属于该字典");
        }

        KpiItemTableFieldDictThird dictThird = null == dto.getId() || dto.getId() == 0 ? new KpiItemTableFieldDictThird() : this.getById(dto.getId());
        String existingThirdItemCode = dictThird.getThirdItemCode();
        BeanUtils.copyProperties(dto, dictThird);

        String startTime = dto.getStartTime();
        String endTime = dto.getEndTime();

        if (StringUtils.isNotBlank(startTime)) {
            dictThird.setStartTime(DateUtil.parse(startTime, "yyyy-MM-dd"));
        } else {
            dictThird.setStartTime(null);
        }

        if (StringUtils.isNotBlank(endTime)) {
            dictThird.setEndTime(DateUtil.parse(endTime, "yyyy-MM-dd"));
        } else {
            dictThird.setEndTime(null);
        }

        LambdaQueryWrapper<KpiItemTableFieldDictThird> queryWrapper = Wrappers.<KpiItemTableFieldDictThird>lambdaQuery()
                .eq(KpiItemTableFieldDictThird::getThirdItemCode, dto.getThirdItemCode())
                .eq(KpiItemTableFieldDictThird::getDictCode, dto.getDictCode())
                .eq(KpiItemTableFieldDictThird::getDelFlag, EnableEnum.ENABLE.getType());

        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("该第三方字典值编码已存在");
            }
            this.save(dictThird);
        } else {
            if (!thirdItemCode.equals(existingThirdItemCode) && this.count(queryWrapper) > 0) {
                throw new BizException("该第三方字典值编码已存在");
            }
            this.updateById(dictThird);
        }

        return dictThird.getId();
    }

    @Override
    public IPage<KpiItemTableFieldDictThirdVO> getPage(KpiItemTableFieldDictThirdDto dto) {
        return kpiItemTableFieldDictThirdMapper.getPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
    }
}
