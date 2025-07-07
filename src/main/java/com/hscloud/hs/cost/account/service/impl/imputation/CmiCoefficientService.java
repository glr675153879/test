package com.hscloud.hs.cost.account.service.impl.imputation;

import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.CmiCoefficientMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.CmiCoefficient;
import com.hscloud.hs.cost.account.service.imputation.ICmiCoefficientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * CMI系数 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CmiCoefficientService extends ServiceImpl<CmiCoefficientMapper, CmiCoefficient> implements ICmiCoefficientService {

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateCmi(CmiCoefficient cmiCoefficient) {
        boolean flag = false;
        List<CmiCoefficient> cmiCoefficients = this.list();
        if (ObjectUtils.isNotNull(cmiCoefficient.getId())){
            cmiCoefficients.removeIf(cmi -> Objects.equals(cmiCoefficient.getId(), cmi.getId()));
        }
        if (!Objects.isNull(cmiCoefficient.getMin()) && !Objects.isNull(cmiCoefficient.getMax())){
            if (cmiCoefficient.getMin().compareTo(cmiCoefficient.getMax()) > 0) {
                throw new BizException("小区间数字必须大于等于大区间数字！");
            }
        }
        for (CmiCoefficient cmi : cmiCoefficients) {
            if (hasIntersection(cmiCoefficient, cmi)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            log.error("区间有交集，入参为：{}", cmiCoefficient);
            throw new BizException("区间不能有交集部分！");
        }
        convertNull(cmiCoefficient);
        return this.saveOrUpdate(cmiCoefficient);
    }

    private void convertNull(CmiCoefficient cmiCoefficient) {
        if (Objects.isNull(cmiCoefficient.getMin())){
            cmiCoefficient.setMin("");
            cmiCoefficient.setMinOnOff("");
        }

        if (Objects.isNull(cmiCoefficient.getMax())){
            cmiCoefficient.setMax("");
            cmiCoefficient.setMaxOnOff("");
        }

    }


    private boolean hasIntersection(CmiCoefficient cmiCoefficient1, CmiCoefficient cmiCoefficient2) {
        //最大或最小为空时，设置一个上限或下限
        if (ObjectUtils.isNull(cmiCoefficient2.getMin())) {
            cmiCoefficient2.setMin("-9999");
            cmiCoefficient2.setMinOnOff("1");
        }
        if (ObjectUtils.isNull(cmiCoefficient2.getMax())) {
            cmiCoefficient2.setMax("9999");
            cmiCoefficient2.setMaxOnOff("1");
        }
        boolean result = true;
        if (ObjectUtils.isNull(cmiCoefficient1.getMin())){
            int first = cmiCoefficient1.getMax().compareTo(cmiCoefficient2.getMin());
            if (first < 0 || (first == 0 && ("1".equals(cmiCoefficient1.getMaxOnOff()) || "1".equals(cmiCoefficient2.getMinOnOff())))) {
                result = false;
            }
        } else if (ObjectUtils.isNull(cmiCoefficient1.getMax())){
            int second = cmiCoefficient1.getMin().compareTo(cmiCoefficient2.getMax());
            if (second > 0 || (second == 0 && ("1".equals(cmiCoefficient1.getMinOnOff()) || "1".equals(cmiCoefficient2.getMaxOnOff())))) {
                result = false;
            }
        } else {
            int first = cmiCoefficient1.getMin().compareTo(cmiCoefficient2.getMax());
            if (first > 0 || (first == 0 && ("1".equals(cmiCoefficient1.getMinOnOff()) || "1".equals(cmiCoefficient2.getMaxOnOff())))) {
                result = false;
            }
            int second = cmiCoefficient1.getMax().compareTo(cmiCoefficient2.getMin());
            if (second < 0 || (second == 0 && ("1".equals(cmiCoefficient1.getMaxOnOff()) || "1".equals(cmiCoefficient2.getMinOnOff())))) {
                result = false;
            }
        }



        return result;

    }
}
