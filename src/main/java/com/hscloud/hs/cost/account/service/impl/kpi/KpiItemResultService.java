package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CaliberEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.ItemResultEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.RoundingEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.UserFactorCodeEnum;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemResultRelationMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferInfoSaveDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferListDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiTransferSaveDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferInfoVO2;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiTransferListVO;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.service.kpi.KpiUserFactorService;
import com.hscloud.hs.cost.account.utils.kpi.StringChangeUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiItemResultMapper;
import com.hscloud.hs.cost.account.service.kpi.IKpiItemResultService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * 核算项结果集 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiItemResultService extends ServiceImpl<KpiItemResultMapper, KpiItemResult> implements IKpiItemResultService {
    private final KpiItemMapper kpiItemMapper;
    private final KpiItemResultRelationMapper kpiItemResultRelationMapper;
    private final KpiAccountUnitService kpiAccountUnitService;

    private static final ReentrantLock FAIR_LOCK = new ReentrantLock(true);
    private final KpiUserFactorService kpiUserFactorService;

    @Override
    @Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public Integer saveItemResult(KpiItem item, Long period, String result, Boolean deleteFlag, Long zhongzhiId,
                                  List<KpiUserAttendance> userMap,
                                  List<KpiItemResultRelation> relationMap) throws IllegalAccessException {
        Integer extNum = 0;
        if (deleteFlag) {
            LambdaQueryWrapper<KpiItemResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(KpiItemResult::getCode, item.getCode());
            queryWrapper.eq(KpiItemResult::getPeriod, period);
            queryWrapper.eq(KpiItemResult::getBusiType, item.getBusiType());
            this.getBaseMapper().delete(queryWrapper);
        }

        String mateFlag = StringUtils.hasLength(item.getChangeFlag()) ? item.getChangeFlag() : EnableEnum.ENABLE.getType();
        String changeVersion = StringUtils.hasLength(item.getChangeVersion()) ? item.getChangeVersion() : EnableEnum.ENABLE.getType();

        Field[] fields = KpiItemResult.class.getDeclaredFields();
        String accountObject = item.getAccountObject();
        List<String> fieldList = new ArrayList<>(Arrays.asList(accountObject.split(",")));
        fieldList.add(ItemResultEnum.PERIOD.getType());
        fieldList.add(ItemResultEnum.VALUE.getType());
        fieldList = fieldList.stream().distinct().collect(Collectors.toList());
        JSONArray resultList = JSONArray.parseArray(result);
        if (CollectionUtils.isEmpty(resultList)) {
            return extNum;
        }
        List<KpiItemResult> list = new ArrayList<>();

        createItemResultList(resultList, fields, fieldList, period, item, mateFlag, list);
        if (CollectionUtils.isEmpty(list)) {
            return extNum;
        }

        List<Long> userIds = list.stream()
                .filter(t -> null != t.getUserId() && !t.getUserId().equals(0L))
                .map(KpiItemResult::getUserId).collect(Collectors.toList());
        List<Long> kkzzUserIds = null;
        if (!CollectionUtils.isEmpty(userIds)) {
            kkzzUserIds = kpiUserFactorService.list(Wrappers.<KpiUserFactor>lambdaQuery()
                            .eq(KpiUserFactor::getType, UserFactorCodeEnum.OFFICE.getCode())
                            .eq(KpiUserFactor::getDictType, "kkzz")
                            .eq(KpiUserFactor::getItemCode, YesNoEnum.YES.getCode())
                            .in(KpiUserFactor::getUserId, userIds))
                    .stream().map(KpiUserFactor::getUserId).collect(Collectors.toList());
        }

        if (EnableEnum.DISABLE.getType().equals(mateFlag)) {//"1"
            for (KpiItemResult itemResult : list) {
                String flag = mateFlag;

                // 跨科坐诊 需要转科
//                if (!CollectionUtils.isEmpty(kkzzUserIds) && kkzzUserIds.contains(itemResult.getUserId())
//                        && StringUtils.hasText(itemResult.getGhkb())) {
//                    itemResult.setMateFlag(YesNoEnum.YES.getValue());
//                    itemResult.setDeptId(null);
//                    continue;
//                }

                boolean isExit = item.getCaliber().equals(CaliberEnum.PEOPLE.getType()) && null != item.getReportId()
                        && EnableEnum.DISABLE.getType().equals(item.getChangeFlag());
                if (isExit) {
                    if (null == itemResult.getDeptId() || itemResult.getDeptId().equals(0L)) {
                        flag = EnableEnum.ENABLE.getType();//"0"
                    }
                } else {
                    List<KpiUserAttendance> var1 = Linq.of(userMap).where(t -> t.getUserId() != null && t.getUserId().equals(itemResult.getUserId())).toList();
                    if (zhongzhiId != null) {
                        var1 = Linq.of(var1).where(t -> !(zhongzhiId.equals(t.getAccountUnit()) && !"中治室".equals(t.getAttendanceGroup()))).toList();
                    }
                    if (!CollectionUtils.isEmpty(var1)) {
                        if (var1.size() > 1 ||
                                (!CollectionUtils.isEmpty(kkzzUserIds) && kkzzUserIds.contains(itemResult.getUserId()))) {
                            // 旧版转科逻辑，（新版转科逻辑：如果有多笔考勤数据，就直接用抽上来的科室id）
                            if (EnableEnum.ENABLE.getType().equals(changeVersion)) {
                                KpiItemResultRelation relation = Linq.of(relationMap).firstOrDefault(t -> t.getCode().equals(item.getCode()) && t.getBusiCode().equals(itemResult.getBusiCode()));
                                if (relation != null) {
                                    itemResult.setDeptId(relation.getDeptId());
                                } else {
                                    itemResult.setDeptId(null);
                                }
                            }
                        } else {
                            flag = EnableEnum.ENABLE.getType();
                            itemResult.setDeptId(var1.get(0).getAccountUnit());
                        }
                    } else {
                        flag = EnableEnum.ENABLE.getType();
                        itemResult.setDeptId(null);
                    }
                }
                itemResult.setMateFlag(flag);
            }
        } else {
            if (item.getCaliber().equals(CaliberEnum.PEOPLE.getType()) && item.getAcqMethod().equals("2")) {
                for (KpiItemResult itemResult : list) {
                    if (itemResult.getDeptId() != null && itemResult.getDeptId() > 0L) {
                        itemResult.setMateFlag("1");
                    }
                }
            }
        }
        this.getBaseMapper().insertBatchSomeColumn(list);
        extNum = list.size();
        return extNum;
    }

    @Override
    public IPage<KpiTransferListVO> getTransferPage(KpiTransferListDTO dto) {
        IPage<KpiTransferListVO> page = this.getBaseMapper().getTransferList(new Page<>(dto.getCurrent(), dto.getSize()), dto);
        List<KpiTransferListVO> list = page.getRecords();
        if (!CollectionUtils.isEmpty(list)) {
            List<KpiItem> itemList = kpiItemMapper.selectList(Wrappers.<KpiItem>lambdaQuery().eq(KpiItem::getBusiType, dto.getBusiType()));
            Map<String, String> itemMap = itemList.stream().collect(Collectors.toMap(KpiItem::getCode, KpiItem::getItemName));

            for (KpiTransferListVO item : list) {
                String period = StringChangeUtil.periodChange(item.getPeriod(), DatePattern.NORM_MONTH_PATTERN);
                item.setPeriod(period);
                String code = item.getCode();
                String[] codes = code.split(",");
                StringBuilder content = new StringBuilder();
                for (String var1 : codes) {
                    content.append(itemMap.get(var1)).append(",");
                }
                code = content.substring(0, content.length() - 1);
                item.setItemName(code);
            }
        }
        return page;
    }

    @Override
    public IPage<KpiTransferInfoVO> getTransferInfoPage(KpiTransferInfoDTO dto) {
        IPage<KpiTransferInfoVO> vo = new Page<>();
        String period = StringChangeUtil.periodChange(dto.getPeriod(), DatePattern.SIMPLE_MONTH_PATTERN);

        LambdaQueryWrapper<KpiItemResult> queryWrapper = Wrappers.<KpiItemResult>lambdaQuery()
                .eq(KpiItemResult::getUserId, dto.getUserId())
                .eq(KpiItemResult::getPeriod, Long.valueOf(period))
                .eq(KpiItemResult::getBusiType, dto.getBusiType())
                .eq(KpiItemResult::getMateFlag, EnableEnum.DISABLE.getType());
        if (EnableEnum.ENABLE.getType().equals(dto.getStatus())) {
            queryWrapper.isNull(KpiItemResult::getDeptId);
        } else {
            queryWrapper.gt(KpiItemResult::getDeptId, 0L);
        }
        IPage<KpiItemResult> page = this.page(new Page<>(dto.getCurrent(), dto.getSize()), queryWrapper);
        List<KpiItemResult> list = page.getRecords();
        if (!CollectionUtils.isEmpty(list)) {
            List<KpiItem> itemList = kpiItemMapper.selectList(Wrappers.<KpiItem>lambdaQuery().eq(KpiItem::getBusiType, dto.getBusiType()));
            Map<String, String> itemMap = itemList.stream().collect(Collectors.toMap(KpiItem::getCode, KpiItem::getItemName));
            Map<Long, String> accountUnitMap = kpiAccountUnitService.getUnitMap(dto.getBusiType());

            List<KpiTransferInfoVO> var1 = new ArrayList<>(128);
            for (KpiItemResult item : list) {
                KpiTransferInfoVO var2 = KpiTransferInfoVO.changeToVO(item);
                String itemName = itemMap.get(item.getCode());
                var2.setItemName(itemName);
                if (null != item.getDeptId()) {
                    var2.setDeptName(accountUnitMap.get(item.getDeptId()));
                }
                var1.add(var2);
            }
            vo.setRecords(var1);
            vo.setTotal(page.getTotal());
            vo.setCurrent(page.getCurrent());
            vo.setSize(page.getSize());
            vo.setPages(page.getPages());
        }
        return vo;
    }

    @Override
    public List<KpiTransferInfoVO2> getTransferList(KpiTransferListDTO dto) {
        List<KpiTransferInfoVO2> list = this.getBaseMapper().getTransferList2(dto);
        //return Linq.of(list).orderBy(x->x.getUserName()).orderBy(x->x.getSourceDept()).toList();
        return list;
    }

    @Override
    public void transferSave(KpiTransferSaveDTO dto) {
        KpiItemResult itemResult = this.getById(dto.getId());
        if (null == itemResult) {
            throw new BizException("核算项结果不存在");
        }
        itemResult.setDeptId(dto.getDeptId());
        this.updateById(itemResult);
        FAIR_LOCK.lock();
        try {
            kpiItemResultRelationMapper.delete(new LambdaQueryWrapper<KpiItemResultRelation>()
                    .eq(KpiItemResultRelation::getCode, itemResult.getCode())
                    .eq(KpiItemResultRelation::getBusiCode, itemResult.getBusiCode())
                    .eq(KpiItemResultRelation::getPeriod, itemResult.getPeriod()));
            if (dto.getDeptId() != null) {
                KpiItemResultRelation relation = new KpiItemResultRelation();
                relation.setPeriod(itemResult.getPeriod());
                relation.setBusiCode(itemResult.getBusiCode());
                relation.setUserId(itemResult.getUserId());
                relation.setDeptId(itemResult.getDeptId());
                relation.setCode(itemResult.getCode());
                relation.setCreatedDate(new Date());
                kpiItemResultRelationMapper.insert(relation);
            }
        } finally {
            FAIR_LOCK.unlock();
        }
    }

    @Override
    public void transferBatchSave(List<KpiItemResultRelation> relationList) {
        FAIR_LOCK.lock();
        try {
            if (CollectionUtils.isEmpty(relationList)) {
                return;
            }
            List<Long> userIds = relationList.stream().map(KpiItemResultRelation::getUserId).collect(Collectors.toList());
            Long period = relationList.get(0).getPeriod();
            String code = relationList.get(0).getCode();

            LambdaQueryWrapper<KpiItemResultRelation> wrapper = Wrappers.<KpiItemResultRelation>lambdaQuery()
                    .eq(KpiItemResultRelation::getPeriod, period)
                    .eq(KpiItemResultRelation::getCode, code)
                    .in(KpiItemResultRelation::getUserId, userIds);
            kpiItemResultRelationMapper.delete(wrapper);
            kpiItemResultRelationMapper.insertBatchSomeColumn(relationList);
        } finally {
            FAIR_LOCK.unlock();
        }
    }

    /**
     * 获取结果集
     *
     * @param resultList 计算的结果集
     * @param fields     KpiItem表的字段
     * @param fieldList  出参字段
     * @param period     周期
     * @param item       核算项
     * @param mateFlag   是否需要匹配
     * @param list       结果集
     * @throws IllegalAccessException 异常
     */
    public static void createItemResultList(JSONArray resultList, Field[] fields, List<String> fieldList, Long period,
                                            KpiItem item, String mateFlag,
                                            List<KpiItemResult> list) throws IllegalAccessException {
        yk:
        for (Object var1 : resultList) {
            KpiItemResult itemResult = new KpiItemResult();
            JSONObject var2 = JSONObject.parseObject(var1.toString());
            for (Field field : fields) {
                field.setAccessible(true);
                String filedName = field.getName();
                filedName = StringChangeUtil.camelCaseToSnakeCase(filedName);
                //if (fieldList.contains(filedName)) {
                if (ItemResultEnum.PERIOD.getType().equals(filedName)) {
                    field.set(itemResult, period);
                } else {
                    ItemResultEnum itemResultEnum = ItemResultEnum.findEnumByType(filedName);
                    if (itemResultEnum == null) {
                        continue;
                    }
                    switch (itemResultEnum) {
                        case PERIOD:
                        case ZDYS:
                        case KZYS:
                        case EMP:
                        case BRKS:
                        case WARD:
                        case DEPT:
                        case ZDYSKS:
                        case KZYSKS:
                        case BRBQ:
                        case PROJECT_ID:
                        case IMPUTATION_DEPT:
                            field.set(itemResult, var2.getLongValue(filedName));
                            break;
                        case SOURCE_DEPT:
                        case BUSINESS_CODE:
                        case KZYH:
                        case GHKB:
                            field.set(itemResult, var2.getString(filedName));
                            break;
                        case VALUE:
                            BigDecimal var3 = var2.getBigDecimal(filedName);
                            if (null == var3 || var3.compareTo(BigDecimal.ZERO) == 0) {
                                continue yk;
                            }
                            var3 = var3.setScale(item.getRetainDecimal(), RoundingEnum.getCodeByDesc(item.getCarryRule()));
                            field.set(itemResult, var3);
                            break;
                        default:
                            break;
                    }
                }
                //}
            }
            itemResult.setCode(item.getCode());
            itemResult.setCreatedDate(new Date());
            itemResult.setMateFlag(mateFlag);
            itemResult.setTenantId(item.getTenantId());
            itemResult.setBusiType(item.getBusiType());
            list.add(itemResult);
        }
    }

    @Override
    public void transferSaveV2(KpiTransferSaveDTO dto) {
        if (StringUtils.hasLength(dto.getIds())) {
            List<Long> ids = Arrays.stream(dto.getIds().split(",")).map(Long::parseLong).collect(Collectors.toList());
            for (Long id : ids) {
                KpiTransferSaveDTO var1 = new KpiTransferSaveDTO();
                var1.setId(id);
                var1.setDeptId(dto.getDeptId());
                var1.setBusiType(dto.getBusiType());
                transferSave(var1);
            }
        }
    }

    @Override
    public void oneTouchSave(KpiTransferInfoSaveDTO dto) {
        KpiTransferInfoDTO var1 = new KpiTransferInfoDTO();
        BeanUtils.copyProperties(dto, var1);
        var1.setCurrent(1L);
        var1.setSize(9999L);
        IPage<KpiTransferInfoVO> page = getTransferInfoPage(var1);
        List<KpiTransferInfoVO> list = page.getRecords();

        Map<String, Long> unitMap = kpiAccountUnitService.getUnitMapV2(dto.getBusiType());
        for (KpiTransferInfoVO var2 : list) {
            if (StringUtils.hasLength(var2.getSourceDept())) {
                Long deptId = unitMap.get(var2.getSourceDept());
                if (null == deptId) {
                    continue;
                }
                KpiTransferSaveDTO var3 = new KpiTransferSaveDTO();
                var3.setBusiType(dto.getBusiType());
                var3.setDeptId(deptId);
                var3.setIds(var2.getId().toString());
                transferSaveV2(var3);
            }
        }
    }

    @Override
    public void oneTouchSave2(KpiTransferInfoSaveDTO dto) {
        Map<String, Long> unitMap = kpiAccountUnitService.getUnitMapV2(dto.getBusiType());
        for (KpiTransferInfoVO2 var2 : dto.getList()) {
            if (StringUtils.hasLength(var2.getSourceDept())) {
                Long deptId = unitMap.get(var2.getSourceDept());
                if (null == deptId) {
                    continue;
                }
                KpiTransferSaveDTO var3 = new KpiTransferSaveDTO();
                var3.setBusiType(dto.getBusiType());
                var3.setDeptId(deptId);
                var3.setIds(var2.getId().toString());
                transferSaveV2(var3);
            }
        }
    }
}
