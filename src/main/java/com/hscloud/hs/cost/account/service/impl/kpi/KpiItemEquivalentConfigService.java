package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.google.common.collect.ImmutableList;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemEquivalentConfigMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigCopyDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemEquivalentConfigDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiItemQueryDTO;
import com.hscloud.hs.cost.account.model.dto.userAttendance.ExcelImportDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountUnit;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItem;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiItemEquivalentConfig;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemEquivalentConfigVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrListVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemEquivalentConfigService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KpiItemEquivalentConfigService extends ServiceImpl<KpiItemEquivalentConfigMapper, KpiItemEquivalentConfig> implements IKpiItemEquivalentConfigService {

    private final KpiItemService kpiItemService;
    private final KpiItemEquivalentConfigMapper kpiItemEquivalentConfigMapper;
    private final RemoteDictService remoteDictService;
    private final KpiAccountUnitService kpiAccountUnitService;
    private final KpiItemMapper kpiItemMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveOrUpdate(List<KpiItemEquivalentConfigDTO> dtos) {
        if (CollectionUtils.isEmpty(dtos)) {
            throw new BizException("当量配置列表不能为空");
        }

        dtos.forEach(dto -> {
            if (ObjectUtils.isEmpty(dto.getAccountUnitId()) || dto.getAccountUnitId() <= 0) {
                throw new BizException("科室id不能为空");
            }
            if (!StringUtils.hasText(dto.getItemCode())) {
                throw new BizException("核算项code不能为空");
            }
        });

        KpiItemQueryDTO queryDTO = new KpiItemQueryDTO();
        queryDTO.setEquivalentFlag(YesNoEnum.YES.getValue());
        queryDTO.setBusiType("1");
        queryDTO.setCodes(dtos.stream().map(KpiItemEquivalentConfigDTO::getItemCode).distinct().collect(Collectors.joining(",")));
        Map<String, Long> itemMap = kpiItemService.getList(queryDTO).stream().collect(Collectors.toMap(KpiItemVO::getCode, KpiItemVO::getId, (v1, v2) -> v1));

        dtos.forEach(dto -> {
            if (!itemMap.containsKey(dto.getItemCode())) {
                throw new BizException("不存在code为" + dto.getItemCode() + "的核算项数据");
            }
            dto.setItemId(itemMap.get(dto.getItemCode()));
        });

        List<Long> ids = dtos.stream()
                .filter(x -> x.getId() != null && x.getId() > 0)
                .map(KpiItemEquivalentConfigDTO::getId)
                .distinct()
                .collect(Collectors.toList());

        if (CollectionUtils.isEmpty(ids)) {
            List<Long> unitIds = dtos.stream()
                    .filter(x -> x.getAccountUnitId() != null && x.getAccountUnitId() > 0)
                    .map(KpiItemEquivalentConfigDTO::getAccountUnitId).distinct().collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(unitIds)) {
                this.remove(Wrappers.<KpiItemEquivalentConfig>lambdaQuery().in(KpiItemEquivalentConfig::getAccountUnitId, unitIds));
            }

            List<KpiItemEquivalentConfig> list = new ArrayList<>();

            for (KpiItemEquivalentConfigDTO dto : dtos) {
                KpiItemEquivalentConfig kpiItemEquivalentConfig = new KpiItemEquivalentConfig();
                BeanUtils.copyProperties(dto, kpiItemEquivalentConfig);
                list.add(kpiItemEquivalentConfig);
            }

            this.saveBatch(list);
        } else {
            List<KpiItemEquivalentConfig> configList = this.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                    .eq(KpiItemEquivalentConfig::getDelFlag, YesNoEnum.NO.getValue())
                    .in(KpiItemEquivalentConfig::getId, ids));
            configList.forEach(kpiItemEquivalentConfig -> {
                dtos.stream().filter(item -> Objects.equals(item.getId(), kpiItemEquivalentConfig.getId()))
                        .findFirst().ifPresent(dto -> BeanUtils.copyProperties(dto, kpiItemEquivalentConfig));
            });

            this.updateBatchById(configList);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void copy(KpiItemEquivalentConfigCopyDTO dto) {
        List<Long> configIds = dto.getConfigIds();
        if (CollectionUtils.isEmpty(configIds)) {
            throw new BizException("当量配置id列表不能为空");
        }

        List<Long> targetAccountUnitIds = dto.getTargetAccountUnitIds();
        if (CollectionUtils.isEmpty(targetAccountUnitIds)) {
            throw new BizException("目标科室id列表不能为空");
        }

        Map<Long, List<KpiItemEquivalentConfig>> existMap = this.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                        .in(KpiItemEquivalentConfig::getAccountUnitId, targetAccountUnitIds)
                        .eq(KpiItemEquivalentConfig::getDelFlag, YesNoEnum.NO.getValue()))
                .stream().collect(Collectors.groupingBy(KpiItemEquivalentConfig::getAccountUnitId));

        List<KpiItemEquivalentConfig> list = this.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                .in(KpiItemEquivalentConfig::getId, configIds));

        List<KpiItemEquivalentConfig> updateList = new ArrayList<>();
        List<KpiItemEquivalentConfig> saveList = new ArrayList<>();

        if (existMap.isEmpty()) {
            for (Long unitId : targetAccountUnitIds) {
                for (KpiItemEquivalentConfig config : list) {
                    KpiItemEquivalentConfig copyConfig = new KpiItemEquivalentConfig();
                    BeanUtils.copyProperties(config, copyConfig);
                    copyConfig.setAccountUnitId(unitId);
                    copyConfig.setId(null);
                    copyConfig.setCreatedId(null);
                    copyConfig.setCreatedDate(null);
                    copyConfig.setUpdatedId(null);
                    copyConfig.setUpdatedDate(null);
                    saveList.add(copyConfig);
                }
            }
        } else {
            for (KpiItemEquivalentConfig config : list) {
                existMap.forEach((unitId, configs) -> {
                    List<KpiItemEquivalentConfig> exist = configs.stream()
                            .filter(x -> x.getItemCode().equals(config.getItemCode()))
                            .collect(Collectors.toList());
                    if (!CollectionUtils.isEmpty(exist)) {
                        exist.forEach(x -> x.setStdEquivalent(config.getStdEquivalent()));
                        updateList.addAll(exist);
                    } else {
                        KpiItemEquivalentConfig copyConfig = new KpiItemEquivalentConfig();
                        BeanUtils.copyProperties(config, copyConfig);
                        copyConfig.setAccountUnitId(unitId);
                        copyConfig.setId(null);
                        copyConfig.setCreatedId(null);
                        copyConfig.setCreatedDate(null);
                        copyConfig.setUpdatedId(null);
                        copyConfig.setUpdatedDate(null);
                        saveList.add(copyConfig);
                    }
                });
            }
        }

        this.updateBatchById(updateList);
        this.saveBatch(saveList);
    }

    @Override
    public List<KpiItemEquivalentConfigVO> getList(KpiItemEquivalentConfigDTO dto) {
        List<KpiItemEquivalentConfigVO> result = new ArrayList<>();

        LambdaQueryWrapper<KpiItemEquivalentConfig> queryWrapper = Wrappers.<KpiItemEquivalentConfig>lambdaQuery()
                .eq(ObjectUtils.isNotEmpty(dto.getAccountUnitId()), KpiItemEquivalentConfig::getAccountUnitId, dto.getAccountUnitId());

        List<KpiItemEquivalentConfig> list = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return result;
        }

        List<Long> unitIds = list.stream()
                .filter(x -> x.getAccountUnitId() != null && x.getAccountUnitId() > 0)
                .map(KpiItemEquivalentConfig::getAccountUnitId).distinct().collect(Collectors.toList());
        String itemCodes = list.stream().map(KpiItemEquivalentConfig::getItemCode).distinct().collect(Collectors.joining(","));

        KpiItemQueryDTO itemQueryDTO = new KpiItemQueryDTO();
        itemQueryDTO.setCodes(itemCodes);
        itemQueryDTO.setEquivalentFlag(YesNoEnum.YES.getValue());
        Map<String, KpiItemVO> itemVOMap = kpiItemService.getList(itemQueryDTO).stream()
                .collect(Collectors.toMap(KpiItemVO::getCode, Function.identity(), (item1, item2) -> item1));

        Map<Long, String> unitMap = kpiAccountUnitService.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                        .in(!CollectionUtils.isEmpty(unitIds), KpiAccountUnit::getId, unitIds)).stream()
                .collect(Collectors.toMap(KpiAccountUnit::getId, KpiAccountUnit::getName, (item1, item2) -> item1));

        for (KpiItemEquivalentConfig config : list) {
            KpiItemEquivalentConfigVO vo = new KpiItemEquivalentConfigVO();
            BeanUtils.copyProperties(config, vo);

            if (itemVOMap.containsKey(config.getItemCode())) {
                KpiItemVO itemVO = itemVOMap.get(config.getItemCode());

                vo.setItemId(itemVO.getId());
                vo.setItemName(itemVO.getItemName());
                vo.setItemStatus(itemVO.getStatus());
                vo.setProCategoryName(itemVO.getProCategoryName());
                vo.setItemDelFlag(YesNoEnum.NO.getValue());
            } else {
                KpiItem kpiItem = kpiItemMapper.getDeletedItem(config.getItemCode());
                if (kpiItem != null) {
                    vo.setItemId(kpiItem.getId());
                    vo.setItemName(kpiItem.getItemName());
                    vo.setItemStatus(kpiItem.getStatus());
                    vo.setItemDelFlag(YesNoEnum.YES.getValue());

                    List<SysDictItem> proCategoryList = remoteDictService.getDictByType("kpi_pro_category").getData();
                    String proCategoryName = Linq.of(proCategoryList).where(t -> t.getItemValue().equals(kpiItem.getProCategoryCode()))
                            .select(SysDictItem::getLabel).firstOrDefault();
                    vo.setProCategoryName(proCategoryName);
                }
            }

            vo.setAccountUnitName(unitMap.get(config.getAccountUnitId()));

            result.add(vo);
        }

        result = Linq.of(result)
                .orderBy(x -> x.getSeq() == null ? Integer.MAX_VALUE : x.getSeq())
                .toList();

        return result;
    }

    @Override
    public ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, Long accountUnitId) {
        if (accountUnitId == null || accountUnitId == 0) {
            throw new BizException("科室id不能为空");
        }

        int successCount = 0;
        int failCount = 0;
        List<ImportErrListVO> details = new ArrayList<>();

        Map<String, String> proCategoryMap = remoteDictService.getDictByType("kpi_pro_category").getData()
                .stream().collect(Collectors.toMap(SysDictItem::getLabel, SysDictItem::getItemValue, (item1, item2) -> item1));

        Map<String, KpiItem> kpiItemMap = kpiItemService.list(Wrappers.<KpiItem>lambdaQuery()
                        .eq(KpiItem::getEquivalentFlag, YesNoEnum.YES.getValue())
                        .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                        .eq(KpiItem::getDelFlag, YesNoEnum.NO.getValue()))
                .stream().collect(Collectors.toMap(KpiItem::getItemName, Function.identity(), (item1, item2) -> item1));

        List<List<String>> head = getHead();

        if (Objects.equals("1", dto.getOverwriteFlag())) {
            // 1覆盖模式：删除此周期所有数据
            this.remove(Wrappers.<KpiItemEquivalentConfig>lambdaQuery().eq(KpiItemEquivalentConfig::getAccountUnitId, accountUnitId));
        }

        List<KpiItemEquivalentConfig> configList = this.list(Wrappers.<KpiItemEquivalentConfig>lambdaQuery().eq(KpiItemEquivalentConfig::getAccountUnitId, accountUnitId));

        // 导入数据
        for (int i = 1; i < xlsDataArr.length; i++) {
            String[] rowData = xlsDataArr[i];

            List<String> errContentList = new ArrayList<>();
            KpiItemEquivalentConfig equivalentConfig = new KpiItemEquivalentConfig();

            try {
                String proCategoryName = rowData[0];
                String proCategoryCode = "";
                if (StrUtil.isBlank(proCategoryName)) {
                    errContentList.add("项目分类缺失");
                } else {
                    proCategoryCode = proCategoryMap.get(proCategoryName);
                    if (StrUtil.isBlank(proCategoryCode)) {
                        errContentList.add("项目分类不存在");
                    }
                }

                String kpiItemName = rowData[1];
                if (StrUtil.isBlank(kpiItemName)) {
                    errContentList.add("当量项目缺失");
                } else {
                    KpiItem kpiItem = kpiItemMap.get(kpiItemName);
                    if (kpiItem == null) {
                        errContentList.add("当量项目不存在");
                    } else {
                        equivalentConfig.setItemId(kpiItem.getId());
                        equivalentConfig.setItemCode(kpiItem.getCode());
                        if (StrUtil.isNotBlank(proCategoryCode) && !proCategoryCode.equals(kpiItem.getProCategoryCode())) {
                            errContentList.add("项目分类与当量项目不匹配");
                        }
                    }
                }

                String stdEquivalent = rowData[2];
                if (StrUtil.isBlank(stdEquivalent)) {
                    errContentList.add("标化当量缺失");
                } else {
                    try {
                        equivalentConfig.setStdEquivalent(new BigDecimal(stdEquivalent));
                    } catch (Exception e) {
                        errContentList.add("标化当量格式错误");
                    }
                }

                if (CollectionUtils.isEmpty(errContentList)) {
                    if (Objects.equals("2", dto.getOverwriteFlag())) {
                        // 2覆盖模式：判断是否存在
                        KpiItemEquivalentConfig existConfig = configList.stream()
                                .filter(config -> Objects.equals(config.getItemCode(), equivalentConfig.getItemCode()))
                                .findFirst()
                                .orElse(null);

                        if (existConfig != null) {
                            equivalentConfig.setId(existConfig.getId());
                            equivalentConfig.setUpdatedId(SecurityUtils.getUser().getId());
                            this.updateById(equivalentConfig);
                        } else {
                            equivalentConfig.setAccountUnitId(accountUnitId);
                            this.save(equivalentConfig);
                        }
                    } else {
                        // 1覆盖模式：直接保存
                        equivalentConfig.setAccountUnitId(accountUnitId);
                        this.save(equivalentConfig);
                    }
                }
            } catch (Exception e) {
                log.error("导入报错", e);
                String message = e.getMessage();
                errContentList.add(message);
            }

            if (CollUtil.isNotEmpty(errContentList)) {
                failCount++;
                ImportErrListVO build = ImportErrListVO.builder()
                        .lineNum(i + 1)
                        .data(ImmutableList.of(rowData[0], rowData[1], rowData[2]))
                        .contentList(errContentList)
                        .content(StrUtil.join(";", errContentList))
                        .build();
                details.add(build);
                if (Objects.equals("2", dto.getContinueFlag())) {
                    break;
                }
            } else {
                successCount++;
            }
        }

        return ImportErrVo.builder().details(details).successCount(successCount).failCount(failCount).head(head).build();
    }

    public static List<List<String>> getHead() {
        List<List<String>> headList = new ArrayList<>();
        headList.add(ImmutableList.of("行号"));
        headList.add(ImmutableList.of("项目分类"));
        headList.add(ImmutableList.of("当量项目"));
        headList.add(ImmutableList.of("标化当量"));
        headList.add(ImmutableList.of("错误说明"));
        return headList;
    }

    @Override
    public KpiItemEquivalentConfigVO getInfo(Long id) {
        KpiItemEquivalentConfigVO vo = new KpiItemEquivalentConfigVO();

        KpiItemEquivalentConfig config = this.getById(id);
        if (config == null) {
            return vo;
        }

        BeanUtils.copyProperties(config, vo);

        KpiItemVO kpiItem = kpiItemService.getKpiItem(config.getItemId());
        if (kpiItem != null) {
            vo.setItemName(kpiItem.getItemName());
            vo.setProCategoryName(kpiItem.getProCategoryName());
        }

        return vo;
    }

    @Override
    public IPage<KpiItemEquivalentConfigVO> getPage(KpiItemEquivalentConfigDTO dto) {

        IPage<KpiItemEquivalentConfigVO> resultPage = new Page<>();

        IPage<KpiItemEquivalentConfig> page = kpiItemEquivalentConfigMapper.getPage(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        BeanUtils.copyProperties(page, resultPage);

        String itemCodes = page.getRecords().stream().map(KpiItemEquivalentConfig::getItemCode).collect(Collectors.joining(","));
        KpiItemQueryDTO itemQueryDTO = new KpiItemQueryDTO();
        itemQueryDTO.setCodes(itemCodes);
        itemQueryDTO.setEquivalentFlag(YesNoEnum.YES.getValue());
        Map<String, KpiItemVO> itemVOMap = kpiItemService.getList(itemQueryDTO).stream()
                .collect(Collectors.toMap(KpiItemVO::getCode, Function.identity(), (item1, item2) -> item1));

        List<KpiItemEquivalentConfigVO> records = page.getRecords().stream()
                .map(config -> {
                    KpiItemEquivalentConfigVO vo = new KpiItemEquivalentConfigVO();
                    BeanUtils.copyProperties(config, vo);

                    KpiItemVO itemVO = itemVOMap.getOrDefault(config.getItemCode(), new KpiItemVO());
                    vo.setItemName(itemVO.getItemName());
                    vo.setProCategoryName(itemVO.getProCategoryName());

                    return vo;
                })
                .collect(Collectors.toList());

        resultPage.setRecords(records);

        return resultPage;
    }

    @Override
    public void updateInheritFlag(Long id, String inheritFlag) {
        KpiItemEquivalentConfig config = this.getById(id);
        if (config == null) {
            throw new BizException("当量配置不存在");
        }

        this.update(Wrappers.<KpiItemEquivalentConfig>lambdaUpdate()
                .set(KpiItemEquivalentConfig::getInheritFlag, inheritFlag)
                .eq(KpiItemEquivalentConfig::getId, config.getId()));
    }

    @Override
    public void updateSeq(List<KpiItemEquivalentConfigDTO> dtos) {
        dtos.forEach(dto -> {
            if (ObjectUtils.isEmpty(dto.getId()) || dto.getId() <= 0) {
                throw new BizException("id不能为空");
            }

            if (ObjectUtils.isEmpty(dto.getSeq())) {
                throw new BizException("排序号不能为空");
            }
        });

        for (KpiItemEquivalentConfigDTO dto : dtos) {
            this.update(Wrappers.<KpiItemEquivalentConfig>lambdaUpdate()
                    .set(KpiItemEquivalentConfig::getSeq, dto.getSeq())
                    .eq(KpiItemEquivalentConfig::getId, dto.getId()));
        }
    }
}
