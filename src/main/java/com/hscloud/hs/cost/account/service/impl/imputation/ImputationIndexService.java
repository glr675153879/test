package com.hscloud.hs.cost.account.service.impl.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.imputation.Imputation;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndexDetails;
import com.hscloud.hs.cost.account.service.imputation.IImputationDeptUnitService;
import com.hscloud.hs.cost.account.service.imputation.IImputationIndexDetailsService;
import com.hscloud.hs.cost.account.service.imputation.IImputationService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationIndexMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ImputationIndex;
import com.hscloud.hs.cost.account.service.imputation.IImputationIndexService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 归集指标 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationIndexService extends ServiceImpl<ImputationIndexMapper, ImputationIndex> implements IImputationIndexService {

    private final IImputationIndexDetailsService imputationIndexDetailsService;
    @Autowired
    @Lazy
    private IImputationDeptUnitService imputationDeptUnitService;
    private final IImputationService imputationService;

    @Override
    public List<ImputationIndex> listByPId(Long imputationId) {
        return this.list(Wrappers.<ImputationIndex>lambdaQuery().eq(ImputationIndex::getImputationId, imputationId));
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean saveOrUpdateImputationIndex(ImputationIndex imputationIndex) {
        imputationService.setImputation(imputationIndex, imputationIndex.getImputationId());
        ImputationIndex one = getOne(Wrappers.<ImputationIndex>lambdaQuery().eq(ImputationIndex::getImputationId, imputationIndex.getImputationId())
                .eq(ImputationIndex::getName, imputationIndex.getName()));
        if (ObjectUtils.isNotNull(imputationIndex.getId())) {
            if (ObjectUtils.isNotNull(one) && !Objects.equals(one.getId(), imputationIndex.getId())) {
                log.error("归集指标编辑，入参为：{}", imputationIndex);
                throw new BizException("此归集下的归集指标已存在，名称不可重复！");
            }
        } else {
            if (ObjectUtils.isNotNull(one)) {
                log.error("归集指标新增，入参为：{}", imputationIndex);
                throw new BizException("此归集下的归集指标已存在，名称不可重复！");
            }
        }


        saveOrUpdate(imputationIndex);

        List<ImputationIndexDetails> indexDetails = Optional.ofNullable(imputationIndex.getIndexDetails()).map(list ->
                list.stream().map(item -> {
                    item.setImputationIndexId(imputationIndex.getId());
                    item.setId(null);
                    return item;
                }).collect(Collectors.toList())
        ).orElse(Collections.emptyList());

        if (indexDetails.isEmpty()) {
            throw new BizException("请选择关联指标！");
        }

        //更新先删除明细
        imputationIndexDetailsService.remove(Wrappers.<ImputationIndexDetails>lambdaQuery()
                .eq(ImputationIndexDetails::getImputationIndexId, imputationIndex.getId()));
        imputationIndexDetailsService.saveBatch(indexDetails);

        if (Objects.equals(imputationIndex.getImputationCode(), ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
            imputationDeptUnitService.setWorkPerson(imputationIndex.getImputationCycle());
        } else {
            imputationDeptUnitService.setIncomePerson(imputationIndex.getImputationCycle());
        }
        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeImputationIndexById(Long id) {
        log.info("删除id为{}的归集指标", id);
        imputationIndexDetailsService.remove(Wrappers.<ImputationIndexDetails>lambdaQuery()
                .eq(ImputationIndexDetails::getImputationIndexId, id));
        ImputationIndex imputationIndex = getById(id);
        if (Objects.nonNull(imputationIndex)) {
            removeById(id);
            if (Objects.equals(imputationIndex.getImputationCode(), ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
                imputationDeptUnitService.setWorkPerson(imputationIndex.getImputationCycle());
            } else {
                imputationDeptUnitService.setIncomePerson(imputationIndex.getImputationCycle());
            }
        }

        return true;
    }

    @Override
    public IPage<ImputationIndex> pageImputationIndex(Page<ImputationIndex> page, QueryWrapper<ImputationIndex> wrapper, Long imputationId) {
        log.info("分页查询归集指标，主档ID入参：{}", imputationId);
        Page<ImputationIndex> imputationIndexPage = page(page, wrapper.eq("imputation_id", imputationId));

        List<ImputationIndex> indexPageRecords = imputationIndexPage.getRecords();
        List<Long> indexIds = Optional.ofNullable(indexPageRecords).map(list ->
                list.stream().map(Entity::getId).collect(Collectors.toList())
        ).orElse(Collections.emptyList());

        if (!indexIds.isEmpty()) {
            List<ImputationIndexDetails> indexDetails = imputationIndexDetailsService.list(Wrappers.<ImputationIndexDetails>lambdaQuery()
                    .in(ImputationIndexDetails::getImputationIndexId, indexIds));

            Map<Long, List<ImputationIndexDetails>> listMap = indexDetails.stream().collect(Collectors.groupingBy(ImputationIndexDetails::getImputationIndexId));

            indexPageRecords.forEach(item -> item.setIndexDetails(listMap.get(item.getId())));

        }
        return imputationIndexPage;
    }
}
