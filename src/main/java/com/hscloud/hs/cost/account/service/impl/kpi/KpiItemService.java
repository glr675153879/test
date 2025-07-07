package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.thread.ExecutorBuilder;
import cn.hutool.core.thread.NamedThreadFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestvike.linq.Linq;
import com.google.common.collect.Lists;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.YesNoEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.*;
import com.hscloud.hs.cost.account.constant.enums.report.OperatorEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportItem;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemExtVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemTableFieldVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiItemVO;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportItemService;
import com.hscloud.hs.cost.account.service.kpi.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.hscloud.hs.cost.account.utils.kpi.StringChangeUtil;
import com.hscloud.hs.cost.account.validator.RuleConfig;
import com.hscloud.hs.cost.account.validator.ValidatorHolder;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;
import org.springframework.util.StringUtils;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 核算项服务实现类
 *
 * @author Administrator
 */
@Slf4j
@Service
@RequiredArgsConstructor
//@Transactional(rollbackFor = Exception.class)
public class KpiItemService extends ServiceImpl<KpiItemMapper, KpiItem> implements IKpiItemService {

    private final IKpiItemResultService iKpiItemResultService;
    private final ICostReportItemService costReportItemService;
    private final IKpiCategoryService ikpiCategoryService;
    private final KpiValidatorService kpiValidatorService;
    private final CommCodeService commCodeService;
    private final IKpiConfigService iKpiConfigService;
    private final KpiUserAttendanceMapper kpiUserAttendanceMapper;
    private final KpiItemResultRelationService kpiItemResultRelationService;
    private final KpiAccountUnitMapper kpiAccountUnitMapper;
    private final RedisUtil redisUtil;
    private final KpiConfigMapper kpiConfigMapper;
    private final KpiMemberMapper kpiMemberMapper;
    private final KpiItemSqlMapper kpiItemSqlMapper;
    private final KpiCalculateMapper kpiCalculateMapper;
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final KpiItemTableService kpiItemTableService;
    private final KpiItemTableFieldService kpiItemTableFieldService;
    private final ValidatorHolder validatorHolder;
    private final SqlUtil sqlUtil;
    private final RemoteDictService remoteDictService;
    private final IKpiServiceItemCategoryService kpiServiceItemCategoryService;

    @Override
    public KpiItemVO getKpiItem(Long id) {
        KpiItem item = this.getById(id);
        KpiItemVO itemVO = KpiItemVO.changeToVo(item);
        itemVO.setAccountObject(item.getAccountObject());
        itemVO.setExtTemplate(item.getExtTemplate());
        if (null != item.getReportId() && item.getReportId() > 0) {
            CostReportItem costReportItem = costReportItemService.getById(item.getReportId());
            if (null != costReportItem) {
                itemVO.setReportName(costReportItem.getName());
            }
        }
        Map<String, String> categoryMap = ikpiCategoryService.getCodeAndNameMap(CategoryEnum.ITEM_GROUP.getType(), item.getCategoryCode(), item.getBusiType());
        itemVO.setCategoryName(categoryMap.get(item.getCategoryCode()));

        List<SysDictItem> proCategoryList = remoteDictService.getDictByType("kpi_pro_category").getData();
        String proCategoryName = Linq.of(proCategoryList).where(t -> t.getItemValue().equals(item.getProCategoryCode()))
                .select(SysDictItem::getLabel).firstOrDefault();
        itemVO.setProCategoryName(proCategoryName);

        KpiServiceItemCategory itemCategory = kpiServiceItemCategoryService.getOne(Wrappers.<KpiServiceItemCategory>lambdaQuery()
                .eq(KpiServiceItemCategory::getCode, item.getServiceItemCategoryCode()));
        if (null != itemCategory) {
            itemVO.setServiceItemCategoryName(itemCategory.getName());
        }

        return itemVO;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveOrUpdate(KpiItemDTO dto) {
        KpiItem item = null == dto.getId() || dto.getId() == 0 ? new KpiItem() : this.getById(dto.getId());
        KpiItem item2 = new KpiItem();
        BeanUtils.copyProperties(item, item2);
        String config = item.getConfig();
        BeanUtils.copyProperties(dto, item);

        String dtoTableIds = dto.getTableIds();

        if (StringUtils.hasText(dtoTableIds)) {
            List<Long> tableIds = Arrays.stream(dtoTableIds.split(","))
                    .map(Long::valueOf)
                    .collect(Collectors.toList());

            item.setTableIds(dtoTableIds);

            List<KpiItemCondDto> condList = dto.getCondList();
            if (!CollectionUtils.isEmpty(condList)) {
                Map<Long, String> tableMap = kpiItemTableService.list(Wrappers.<KpiItemTable>lambdaQuery()
                                .in(KpiItemTable::getId, tableIds)
                                .eq(KpiItemTable::getDelFlag, EnableEnum.ENABLE.getType()))
                        .stream().collect(Collectors.toMap(KpiItemTable::getId, KpiItemTable::getTableName));

                // 获取表字段列表
                List<KpiItemTableFieldVO> fieldList = kpiItemTableFieldService.getListByTableIds(tableIds);
                Map<Long, Map<String, String>> fieldMap = fieldList.stream()
                        .collect(Collectors.groupingBy(KpiItemTableFieldVO::getTableId,
                                Collectors.toMap(KpiItemTableFieldVO::getFieldName, KpiItemTableFieldVO::getFieldType)));

                verifyCond(tableMap, fieldMap, condList);

                item.setItemCond(JSON.toJSONString(condList));

                // 处理查询条件格式
                sqlUtil.dealCond(condList);

                // 校验查询条件
                RuleConfig ruleConfig = new RuleConfig();
                ruleConfig.setType("SQL_COND");
                ruleConfig.setContent(JSON.toJSONString(condList));
                ValidatorResultVo vo = validatorHolder.getValidatorByType("SQL_COND").validate(ruleConfig);
                if (StringUtils.hasLength(vo.getErrorMsg())) {
                    throw new BizException(vo.getErrorMsg());
                }

                item.setWhereSql(" and " + sqlUtil.buildWhereCondition(condList));
            } else {
                item.setItemCond(null);
                item.setWhereSql(null);
            }
        } else {
            item.setTableIds(null);
            item.setItemCond(null);
            item.setWhereSql(null);
        }

        if (!StringUtils.hasLength(item.getCode())) {
            item.setCode(commCodeService.commCode(CodePrefixEnum.ITEM));
        }
        if (StringUtils.hasLength(item.getConfig()) || null != item.getReportId()) {
            String period = iKpiConfigService.getLastCycle(true);
            KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(item, period);
            ValidatorResultVo resultVo = kpiValidatorService.itemValidator(validatorDTO, false, null, null);
            if (StringUtils.hasLength(resultVo.getErrorMsg())) {
                throw new BizException(resultVo.getErrorMsg());
            }

            //sql有变化 存档
            if (null != dto.getId() && dto.getId() != 0
                    && !StringUtil.isNullOrEmpty(config)
                    && !StringUtil.isNullOrEmpty(dto.getConfig())
                    && config.hashCode() != dto.getConfig().hashCode()) {
                saveItemSql(item2);
            }
        }

        LambdaQueryWrapper<KpiItem> queryWrapper = Wrappers.<KpiItem>lambdaQuery()
                .eq(KpiItem::getItemName, dto.getItemName())
                .eq(KpiItem::getCategoryCode, dto.getCategoryCode())
                .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType());
        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("已存在同名核算项");
            }
            item.setExtStatus(ItemExtStatusEnum.WAIT_EXT.getStatus());
            this.save(item);
        } else {
            if (!item.getItemName().equals(dto.getItemName()) && this.count(queryWrapper) > 0) {
                throw new BizException("已存在同名核算项");
            }
            this.updateById(item);
        }
        return item.getId();
    }

    public void saveItemSql(KpiItem dto) {
        KpiItemSql kpiItemSql = new KpiItemSql();
        BeanUtils.copyProperties(dto, kpiItemSql);
        kpiItemSql.setId(null);
        kpiItemSql.setUpdatedDate(new Date());
        kpiItemSqlMapper.insert(kpiItemSql);
    }

    @Override
    public void switchStatus(BaseIdStatusDTO dto) {
        // todo 是否存在操作检测
        LambdaUpdateWrapper<KpiItem> updateWrapper = Wrappers.<KpiItem>lambdaUpdate()
                .eq(KpiItem::getId, dto.getId())
                .set(KpiItem::getStatus, dto.getStatus());
        this.update(updateWrapper);
    }

    @Override
    public void deleteItem(Long id) {
        // todo 是否存在删除操作检测
        this.removeById(id);
    }

    @Override
    public IPage<KpiItemVO> getPage(KpiItemQueryDTO dto) {
        List<String> caliberList = new ArrayList<>(12);
        if (StringUtils.hasLength(dto.getCaliber())) {
            caliberList = Arrays.asList(dto.getCaliber().split(","));
        }

        LambdaQueryWrapper<KpiItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(ObjectUtils.isNotEmpty(dto.getStatus()), KpiItem::getStatus, dto.getStatus())
                .eq(ObjectUtils.isNotEmpty(dto.getCategoryCode()), KpiItem::getCategoryCode, dto.getCategoryCode())
                .eq(ObjectUtils.isNotEmpty(dto.getAcqMethod()), KpiItem::getAcqMethod, dto.getAcqMethod())
                .eq(null != dto.getRetainDecimal(), KpiItem::getRetainDecimal, dto.getRetainDecimal())
                .eq(ObjectUtils.isNotEmpty(dto.getCarryRule()), KpiItem::getCarryRule, dto.getCarryRule())
                .in(ObjectUtils.isNotEmpty(dto.getCaliber()), KpiItem::getCaliber, caliberList)
                .eq(ObjectUtils.isNotEmpty(dto.getConditionFlag()), KpiItem::getConditionFlag, dto.getConditionFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getBusiType()), KpiItem::getBusiType, dto.getBusiType())
                .eq(ObjectUtils.isNotEmpty(dto.getBedsFlag()), KpiItem::getBedsFlag, dto.getBedsFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getSecondFlag()), KpiItem::getSecondFlag, dto.getSecondFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getEquivalentFlag()), KpiItem::getEquivalentFlag, dto.getEquivalentFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getAssignFlag()), KpiItem::getAssignFlag, dto.getAssignFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getChangeFlag()), KpiItem::getChangeFlag, dto.getChangeFlag())
                .like(ObjectUtils.isNotEmpty(dto.getItemName()), KpiItem::getItemName, dto.getItemName());
        if (ObjectUtils.isNotEmpty(dto.getExtStatus())) {
            if (dto.getExtStatus().equals(ItemExtStatusEnum.EXT_SUCCESS_ZERO.getStatus())) {
                wrapper.eq(KpiItem::getExtStatus, ItemExtStatusEnum.EXT_SUCCESS.getStatus());
                wrapper.apply("b.num is null ");
                //wrapper.eq(KpiItem::getExtNum, 0);
            } else {
                wrapper.eq(KpiItem::getExtStatus, dto.getExtStatus());
            }
        }
        KpiConfig config = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>().eq("1".equals(dto.getBusiType()) ? "default_flag" : "default_ks_flag", "Y"));

        IPage<KpiItemVO> page = this.getBaseMapper().pageData(new Page<>(dto.getCurrent(), dto.getSize()), wrapper, config.getPeriod());
        List<KpiItemVO> list = page.getRecords();
        Map<String, String> categoryMap = ikpiCategoryService.getCodeAndNameMap(CategoryEnum.ITEM_GROUP.getType(), null, dto.getBusiType());
        List<SysDictItem> proCategoryList = remoteDictService.getDictByType("kpi_pro_category").getData();
        List<KpiServiceItemCategory> itemCategoryList = kpiServiceItemCategoryService.list();

        for (KpiItemVO item : list) {
            item.setCategoryName(categoryMap.get(item.getCategoryCode()));

            String proCategoryName = Linq.of(proCategoryList).where(t -> t.getItemValue().equals(item.getProCategoryCode()))
                    .select(SysDictItem::getLabel).firstOrDefault();
            item.setProCategoryName(proCategoryName);

            String proTypeName = Linq.of(itemCategoryList).where(t -> t.getCode().equals(item.getServiceItemCategoryCode()))
                    .select(KpiServiceItemCategory::getName).firstOrDefault();
            item.setServiceItemCategoryName(proTypeName);
        }
        return page;
    }

    @Override
    public IPage<KpiItemVO> getPageOld(KpiItemQueryDTO dto) {
        List<String> codeList = new ArrayList<>(128);
        if (StringUtils.hasLength(dto.getCodes())) {
            codeList = Arrays.asList(dto.getCodes().split(","));
        }

        LambdaQueryWrapper<KpiItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(ObjectUtils.isNotEmpty(dto.getStatus()), KpiItem::getStatus, dto.getStatus())
                .in(ObjectUtils.isNotEmpty(dto.getCodes()), KpiItem::getCode, codeList);

        IPage<KpiItemVO> page = this.getBaseMapper().pageDataOld(new Page<>(dto.getCurrent(), dto.getSize()), wrapper);

        return page;
    }

    @Override
    public List<KpiItemVO> getList(KpiItemQueryDTO dto) {
        List<String> caliberList = new ArrayList<>(12);
        if (StringUtils.hasLength(dto.getCaliber())) {
            caliberList = Arrays.asList(dto.getCaliber().split(","));
        }
        List<String> codeList = new ArrayList<>(128);
        if (StringUtils.hasLength(dto.getCodes())) {
            codeList = Arrays.asList(dto.getCodes().split(","));
        }
        LambdaQueryWrapper<KpiItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(ObjectUtils.isNotEmpty(dto.getStatus()), KpiItem::getStatus, dto.getStatus())
                .eq(ObjectUtils.isNotEmpty(dto.getCategoryCode()), KpiItem::getCategoryCode, dto.getCategoryCode())
                .eq(ObjectUtils.isNotEmpty(dto.getAcqMethod()), KpiItem::getAcqMethod, dto.getAcqMethod())
                .eq(null != dto.getRetainDecimal(), KpiItem::getRetainDecimal, dto.getRetainDecimal())
                .eq(ObjectUtils.isNotEmpty(dto.getCarryRule()), KpiItem::getCarryRule, dto.getCarryRule())
                .in(ObjectUtils.isNotEmpty(dto.getCaliber()), KpiItem::getCaliber, caliberList)
                .eq(ObjectUtils.isNotEmpty(dto.getConditionFlag()), KpiItem::getConditionFlag, dto.getConditionFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getBusiType()), KpiItem::getBusiType, dto.getBusiType())
                .eq(ObjectUtils.isNotEmpty(dto.getBedsFlag()), KpiItem::getBedsFlag, dto.getBedsFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getSecondFlag()), KpiItem::getSecondFlag, dto.getSecondFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getEquivalentFlag()), KpiItem::getEquivalentFlag, dto.getEquivalentFlag())
                .eq(ObjectUtils.isNotEmpty(dto.getAssignFlag()), KpiItem::getAssignFlag, dto.getAssignFlag())
                .in(ObjectUtils.isNotEmpty(dto.getCodes()), KpiItem::getCode, codeList)
                .like(ObjectUtils.isNotEmpty(dto.getItemName()), KpiItem::getItemName, dto.getItemName());
        if (ObjectUtils.isNotEmpty(dto.getExtStatus())) {
            if (dto.getExtStatus().equals(ItemExtStatusEnum.EXT_SUCCESS_ZERO.getStatus())) {
                wrapper.eq(KpiItem::getExtStatus, ItemExtStatusEnum.EXT_SUCCESS.getStatus());
                wrapper.eq(KpiItem::getExtNum, 0);
            } else {
                wrapper.eq(KpiItem::getExtStatus, dto.getExtStatus());
            }
        }

        List<KpiItem> list = this.list(wrapper);
        List<KpiItemVO> result = new ArrayList<>(512);

        Map<String, String> categoryMap = ikpiCategoryService.getCodeAndNameMap(CategoryEnum.ITEM_GROUP.getType(), null, dto.getBusiType());
        List<SysDictItem> proCategoryList = remoteDictService.getDictByType("kpi_pro_category").getData();
        List<KpiServiceItemCategory> itemCategoryList = kpiServiceItemCategoryService.list();

        for (KpiItem item : list) {
            KpiItemVO itemVO = KpiItemVO.changeToVo(item);
            itemVO.setCategoryName(categoryMap.get(item.getCategoryCode()));

            String proCategoryName = Linq.of(proCategoryList).where(t -> t.getItemValue().equals(item.getProCategoryCode()))
                    .select(SysDictItem::getLabel).firstOrDefault();
            itemVO.setProCategoryName(proCategoryName);

            String proTypeName = Linq.of(itemCategoryList).where(t -> t.getCode().equals(item.getServiceItemCategoryCode()))
                    .select(KpiServiceItemCategory::getName).firstOrDefault();
            itemVO.setServiceItemCategoryName(proTypeName);

            result.add(itemVO);
        }
        return result;
    }

    @Override
    public String getResultList(Long id, String period) {
        KpiItem item = this.getById(id);
        if (null == item || !StringUtils.hasLength(item.getCode())) {
            throw new BizException("未查询到该核算项或code缺失");
        }
        if (null == period) {
            period = iKpiConfigService.getLastCycle(false);
            if (!StringUtils.hasLength(period)) {
                throw new BizException("未查询到当前最新计算周期");
            }
        }
        String result;
        period = period.replaceAll("-", "").substring(0, 6);
        LambdaQueryWrapper<KpiItemResult> wrapper = Wrappers.<KpiItemResult>lambdaQuery()
                .eq(KpiItemResult::getCode, item.getCode())
                .eq(KpiItemResult::getPeriod, Long.valueOf(period))
                .eq(KpiItemResult::getBusiType, item.getBusiType());
        List<KpiItemResult> list = iKpiItemResultService.getBaseMapper().selectList(wrapper);
        if (CollectionUtils.isEmpty(list)) {
            throw new BizException("未查询到最近的核算结果");
        }
        String accountObject = item.getAccountObject();
        if (EnableEnum.DISABLE.getType().equals(item.getChangeFlag())) {
            accountObject += "," + ItemResultEnum.DEPT.getType();
        }
        List<String> fields = Arrays.stream(accountObject.split(",")).collect(Collectors.toList());
        if (!fields.contains("value")) {
            fields.add("value");
        }
        if (!fields.contains("period")) {
            fields.add("period");
        }
        List<LinkedHashMap<String, Object>> map = new ArrayList<>();
        int i = 1;
        try {
            for (KpiItemResult var1 : list) {
                LinkedHashMap<String, Object> row = new LinkedHashMap<>(32);
                row.put("seq", i++);
                for (String field : fields) {
                    boolean found = ItemResultEnum.chargeFieldExist(field);
                    if (found) {
                        field = StringChangeUtil.snakeCaseToCamelCase(field);
                        Field nameField = var1.getClass().getDeclaredField(field);
                        nameField.setAccessible(true);
                        Object value = nameField.get(var1);
                        if (null == value) {
                            value = "";
                        }
                        String name = value.toString();
                        row.put(field, name);
                    }
                }
                map.add(row);
            }
        } catch (Exception e) {
            log.error("数据转换异常：", e);
            throw new BizException("数据转换异常");
        }
        result = kpiValidatorService.changeData(item.getRetainDecimal(), item.getCarryRule(), map, true, item.getBusiType());
        return result;
    }

    @Override
    public void itemCalculate(Long id, KpiItem item, String period, Boolean deleteFlag, Long zhongzhiId,
                              List<KpiUserAttendance> userList, List<KpiItemResultRelation> relationList, List<KpiMember> members, List<KpiAccountUnit> units, String busiType) {
        if (null == item) {
            item = this.getById(id);
            if (null == item || !StringUtils.hasLength(item.getCode())) {
                throw new BizException("未查询到该核算项或code缺失");
            }
        }
        if (null == period) {
            period = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(period)) {
                throw new BizException("未查询到当前最新计算周期");
            }
        }
        part(Long.parseLong(StringChangeUtil.periodChange(period, DatePattern.SIMPLE_MONTH_PATTERN)));
//        String key = CacheConstants.KPI_ITEM_CALCULATE + item.getCode();
//        if (redisUtil.get(key) != null) {
//            return;
//        } else {
//            redisUtil.set(key, 1, 1L, TimeUnit.MINUTES);
//        }

        if (CollectionUtils.isEmpty(relationList)) {
            relationList = kpiItemResultRelationService.getLastMonthRelationList(period, item.getCode());
        }
        if (CollectionUtils.isEmpty(members)) {
            members = kpiMemberMapper.selectList(
                    new QueryWrapper<KpiMember>()
                            .eq("member_type", MemberEnum.ACCOUNT_UNIT_RELATION.getType())
                            .eq("busi_type", busiType));
        }
        if (CollectionUtils.isEmpty(units)) {
            units = kpiAccountUnitMapper.selectList(
                    new QueryWrapper<KpiAccountUnit>()
                            .eq("del_flag", "0")
                            .eq("status", "0")
                            .eq("busi_type", busiType));
        }
        ValidatorResultVo resultVo = new ValidatorResultVo();
        String errorMsg = "", status = ItemExtStatusEnum.WAIT_EXT.getStatus();
        boolean endFlag = false;
        try {
            KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(item, period);
            resultVo = kpiValidatorService.itemValidator(validatorDTO, false, members, units);
        } catch (Exception e) {
            log.error("核算项【{}】校验失败：{}", item.getItemName(), e.getMessage());
            errorMsg = e.getMessage();
            status = ItemExtStatusEnum.EXT_FAIL.getStatus();
            endFlag = true;
        }
        if (!endFlag) {
            if (StringUtils.hasLength(resultVo.getErrorMsg())) {
                log.error("核算项【{}】计算失败：{}", item.getItemName(), resultVo.getErrorMsg());
                errorMsg = resultVo.getErrorMsg();
                status = ItemExtStatusEnum.EXT_FAIL.getStatus();
            } else {
                if (deleteFlag) {
                    List<Long> ids = Collections.singletonList(item.getId());
                    updateExtInfo(ids, item.getBusiType());
                }
                period = period.replace("-", "").substring(0, 6);
                if (CollectionUtils.isEmpty(userList)) {
                    userList = kpiUserAttendanceMapper.selectList(Wrappers.<KpiUserAttendance>lambdaQuery()
                            .eq(KpiUserAttendance::getDelFlag, EnableEnum.ENABLE.getType())
                            .eq(KpiUserAttendance::getPeriod, Long.valueOf(period))
                            .eq(KpiUserAttendance::getBusiType, item.getBusiType())
                    );
                }
                try {
                    Integer extNum = iKpiItemResultService.saveItemResult(item, Long.valueOf(period), resultVo.getResult(), deleteFlag, zhongzhiId, userList, relationList);
                    item.setExtNum(extNum);
                    status = ItemExtStatusEnum.EXT_SUCCESS.getStatus();
                } catch (Exception e) {
                    String error = CommonUtils.getEroLog(e);
                    log.error("核算项【{}】计算失败：{}", item.getItemName(), error);
                    errorMsg = error;
                    status = ItemExtStatusEnum.EXT_FAIL.getStatus();
                }
            }
        }
        item.setErrorInfo(errorMsg);
        item.setExtStatus(status);
        item.setExtDate(new Date());
        this.updateById(item);
    }


    public void part(Long period) {
        String part3 = "p_" + period;
        List<String> kpiItemResultPart2 = kpiCalculateMapper.findTablePartitionNmae("kpi_item_result");
        if (!kpiItemResultPart2.contains(part3)) {
            String sql = "alter table kpi_item_result add partition(partition p_" + period + " values in (" + period + "))";
            MapSqlParameterSource parameters = new MapSqlParameterSource();
            jdbcTemplate.update(sql, parameters);
        }
    }

    @Override
    public List<Future<Boolean>> itemBatchCalculate(List<Long> ids, String busiType, Long period, String equivalentFlag) {
        part(period);
        List<Future<Boolean>> futures = new ArrayList<>();
        if (!StringUtils.hasLength(busiType)) {
            busiType = EnableEnum.DISABLE.getType();
        }
        List<KpiItem> list = this.list(Wrappers.<KpiItem>lambdaQuery()
                .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getBusiType, busiType)
                .eq(KpiItem::getEquivalentFlag, equivalentFlag)
                .in(ObjectUtils.isNotEmpty(ids), KpiItem::getId, ids)
        );
        if (CollectionUtils.isEmpty(list)) {
            //throw new BizException("未查询到需要计算的核算项");
            kpiConfigMapper.updateIndex(busiType, period, "1", new Date());
            return futures;
        }
        if (null == period) {
            String var1 = iKpiConfigService.getLastCycle(true);
            if (!StringUtils.hasLength(var1)) {
                throw new BizException("未查询到计算周期");
            } else {
                period = Long.valueOf(var1.replace("-", "").substring(0, 6));
            }
        }
        KpiConfig one = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>().eq("period", period));
        String status = busiType.equals("1") ? one.getIndexFlag() : one.getIndexFlagKs();
        if (status.equals("0")) {
            throw new BizException("正在批量计算总请稍等");
        }
        String strPeriod = period.toString().substring(0, 4) + "-" + period.toString().substring(4, 6);
        updateExtInfo(ids, busiType);
        LambdaQueryWrapper<KpiItemResult> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(KpiItemResult::getPeriod, period);
        queryWrapper.in(KpiItemResult::getBusiType, busiType);
        if (YesNoEnum.YES.getValue().equals(equivalentFlag)) {
            queryWrapper.in(KpiItemResult::getCode, Linq.of(list).select(KpiItem::getCode).toList());
        }
        iKpiItemResultService.getBaseMapper().delete(queryWrapper);

        List<KpiUserAttendance> userList = kpiUserAttendanceMapper.selectList(Wrappers.<KpiUserAttendance>lambdaQuery()
                .eq(KpiUserAttendance::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(KpiUserAttendance::getPeriod, period)
                .eq(KpiUserAttendance::getBusiType, busiType)
        );
        List<KpiItemResultRelation> relationList = kpiItemResultRelationService.getLastMonthRelationList(strPeriod, null);
        List<String> item_codes = Linq.of(list).select(x -> x.getCode()).toList();
        kpiConfigMapper.updateIndex(busiType, period, "0", null);
        List<List<KpiItem>> listList = Lists.partition(list, 200);
        ThreadPoolExecutor executorService = ExecutorBuilder.create()
                .setCorePoolSize(5)
                .setMaxPoolSize(10)
                .setKeepAliveTime(60L, TimeUnit.SECONDS)
                .setWorkQueue(new LinkedBlockingQueue<>(100))
                .setThreadFactory(new NamedThreadFactory("kpi-item-thread-pool-", false))
                .build();
        /*List<KpiAccountUnit> listAccountUnits = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("busi_type", busiType)
                        .eq("name", "中治室")
        );*/
        List<KpiMember> members = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .eq("member_type", MemberEnum.ACCOUNT_UNIT_RELATION.getType())
                        .eq("busi_type", busiType));
        List<KpiAccountUnit> units = kpiAccountUnitMapper.selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("busi_type", busiType)
        );
        List<KpiAccountUnit> listAccountUnits = Linq.of(units).where(x -> "中治室".equals(x.getName())).toList();
        Long zhizhishiId;
        if (!listAccountUnits.isEmpty()) {
            zhizhishiId = listAccountUnits.get(0).getId();
        } else {
            zhizhishiId = null;
        }

        for (List<KpiItem> itemList : listList) {
            try {
                String finalBusiType1 = busiType;
                Future<Boolean> future = executorService.submit(() -> {
                    StopWatch stopWatch = new StopWatch();
                    stopWatch.start(Thread.currentThread().getName());
                    boolean success = true;
                    try {
                        for (KpiItem item : itemList) {
                            List<KpiItemResultRelation> var1 = null;
                            if (!CollectionUtils.isEmpty(relationList)) {
                                var1 = relationList.stream().filter(relation -> relation.getCode().equals(item.getCode())).collect(Collectors.toList());
                            }
                            try {
                                itemCalculate(null, item, strPeriod, false, zhizhishiId, userList, var1, members, units, finalBusiType1);
                            } catch (Exception e) {
                                log.error("核算项{}计算异常：", item.getItemName(), e);
                                success = false;
                            }
                        }
                    } finally {
                        stopWatch.stop();
                        log.info("线程{}：核算项计算耗时{}s", Thread.currentThread().getName(), stopWatch.getTotalTimeSeconds());
                    }
                    return success;
                });
                futures.add(future);
            } catch (Exception e) {
                log.error("核算项计算异常：", e);
            }
        }
        executorService.shutdown();

        if (YesNoEnum.YES.getValue().equals(equivalentFlag)) {
            return futures;
        }

        String finalBusiType = busiType;
        Long finalPeriod = period;
        String finalBusiType2 = busiType;
        new Thread(() -> {
            try {
                //Thread.sleep(5000L);
                List<KpiItem> list2 = this.list(new LambdaQueryWrapper<KpiItem>()
                        .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                        .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                        .eq(KpiItem::getBusiType, finalBusiType)
                        .eq(KpiItem::getExtStatus, ItemExtStatusEnum.WAIT_EXT.getStatus())
                        .select(KpiItem::getCode));
                //System.out.println("第一次监测"+list2.size());
                while (Linq.of(list2).any(l -> item_codes.contains(l.getCode()))) {
                    list2 = this.list(new LambdaQueryWrapper<KpiItem>()
                            .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                            .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                            .eq(KpiItem::getBusiType, finalBusiType)
                            .eq(KpiItem::getExtStatus, ItemExtStatusEnum.WAIT_EXT.getStatus())
                            .select(KpiItem::getCode));
                    //System.out.println("第一次监测"+list2.size());
                    Thread.sleep(5000L);
                }
                List<KpiItem> ext_fails = this.baseMapper.selectList(
                        new LambdaQueryWrapper<KpiItem>()
                                .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                                .eq(KpiItem::getBusiType, finalBusiType)
                                .eq(KpiItem::getExtStatus, ItemExtStatusEnum.EXT_FAIL.getStatus()));
                for (KpiItem item : ext_fails) {
                    List<KpiItemResultRelation> var1 = null;
                    if (!CollectionUtils.isEmpty(relationList)) {
                        var1 = relationList.stream().filter(relation -> relation.getCode().equals(item.getCode())).collect(Collectors.toList());
                    }
                    itemCalculate(null, item, strPeriod, false, zhizhishiId, userList, var1, members, units, finalBusiType2);
                }
                if (this.count(new LambdaQueryWrapper<KpiItem>()
                        .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                        .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                        .eq(KpiItem::getBusiType, finalBusiType)
                        .eq(KpiItem::getExtStatus, ItemExtStatusEnum.EXT_FAIL.getStatus())) > 0) {
                    kpiConfigMapper.updateIndex(finalBusiType, finalPeriod, "9", null);
                } else {
                    kpiConfigMapper.updateIndex(finalBusiType, finalPeriod, "1", new Date());
                }
            } catch (Exception exception) {
                log.error("核算项批量计算检测异常", exception);
                kpiConfigMapper.updateIndex(finalBusiType, finalPeriod, "9", null);
            }
        }).start();

        return futures;
    }

    @Override
    //@Transactional(rollbackFor = Exception.class, propagation = Propagation.REQUIRES_NEW)
    public void updateExtInfo(List<Long> ids, String busiType) {
        LambdaUpdateWrapper<KpiItem> updateWrapper = Wrappers.<KpiItem>lambdaUpdate()
                .eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                .eq(ObjectUtils.isNotEmpty(busiType), KpiItem::getBusiType, busiType)
                .in(ObjectUtils.isNotEmpty(ids), KpiItem::getId, ids)
                .set(KpiItem::getExtStatus, ItemExtStatusEnum.WAIT_EXT.getStatus())
                .set(KpiItem::getErrorInfo, "")
                .set(KpiItem::getExtDate, null)
                .set(KpiItem::getExtNum, null);
        this.update(updateWrapper);
    }

    @Override
    public KpiItemExtVO getItemExtInfo(KpiItemQueryDTO dto) {
        KpiItemExtVO result = new KpiItemExtVO();
        LambdaQueryWrapper<KpiItem> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(KpiItem::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getStatus, EnableEnum.ENABLE.getType())
                .eq(KpiItem::getBusiType, dto.getBusiType())
                .select(KpiItem::getId,
                        KpiItem::getCode,
                        KpiItem::getExtStatus,
                                KpiItem::getEquivalentFlag);
        List<KpiItem> list = this.baseMapper.selectList(wrapper);
        //List<KpiItemVO> list = getList(dto);
        /*List<KpiItemVO> var1 = list.stream().filter(item -> null != item.getExtStatus()
                && item.getExtStatus().equals(ItemExtStatusEnum.EXT_SUCCESS.getStatus())
                && null != item.getExtNum()
                && item.getExtNum() == 0).collect(Collectors.toList());*/
        KpiConfig config = kpiConfigMapper.selectOne(new QueryWrapper<KpiConfig>()
                .eq("1".equals(dto.getBusiType()) ? "default_flag" : "default_ks_flag", "Y"));

        IPage<KpiItemVO> kpiItemVOIPage = this.getBaseMapper().pageData(new Page(1, 9999L).setSearchCount(false),
                new LambdaQueryWrapper<KpiItem>()
                        .eq(KpiItem::getExtStatus, ItemExtStatusEnum.EXT_SUCCESS.getStatus())
                        .eq(KpiItem::getDelFlag, "0")
                        .eq(KpiItem::getStatus, "0")
                        .eq(KpiItem::getBusiType, dto.getBusiType())
                        .eq(ObjectUtils.isNotEmpty(dto.getEquivalentFlag()), KpiItem::getEquivalentFlag, dto.getEquivalentFlag())
                        .apply("b.num is null "),
                config.getPeriod());
        result.setExtZeroNum(kpiItemVOIPage.getRecords().size());

        list = Linq.of(list).where(item -> null != item.getExtStatus()).toList();

        result.setAllExtNum(Linq.of(list).where(item ->
                !YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())).count());

        result.setExtFailNum(Linq.of(list).where(item ->
                !YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())
                        && item.getExtStatus().equals(ItemExtStatusEnum.EXT_FAIL.getStatus())).count());

        result.setNotExtNum(Linq.of(list).where(item ->
                !YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())
                        && item.getExtStatus().equals(ItemExtStatusEnum.WAIT_EXT.getStatus())).count());

        result.setAllNum(Linq.of(list).where(item ->
                YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())).count());

        result.setExtFailEqNum(Linq.of(list).where(item ->
                YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())
                        && item.getExtStatus().equals(ItemExtStatusEnum.EXT_FAIL.getStatus())).count());

        result.setNotExtEqNum(Linq.of(list).where(item ->
                YesNoEnum.YES.getValue().equals(item.getEquivalentFlag())
                        && item.getExtStatus().equals(ItemExtStatusEnum.WAIT_EXT.getStatus())).count());

        return result;
    }

    @Override
    public void saveCond(KpiItemSaveCondDto dto) {
        List<KpiItemCondDto> condList = dto.getCondList();

        KpiItem item = this.getById(dto.getItemId());
        if (item == null) {
            throw new BizException("核算项不存在");
        }

        if (CollectionUtils.isEmpty(condList)) {
            this.update(Wrappers.<KpiItem>lambdaUpdate()
                    .set(KpiItem::getWhereSql, null)
                    .set(KpiItem::getItemCond, null)
                    .eq(KpiItem::getId, item.getId()));

            return;
        }

        // 获取表字段列表
        String tableIds = item.getTableIds();
        if (!StringUtils.hasLength(tableIds)) {
            throw new BizException("核算项数据表未设置");
        }

        List<Long> tableIdList = Arrays.stream(tableIds.split(","))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        Map<Long, String> tableMap = kpiItemTableService.list(Wrappers.<KpiItemTable>lambdaQuery()
                        .in(KpiItemTable::getId, tableIdList)
                        .eq(KpiItemTable::getDelFlag, EnableEnum.ENABLE.getType()))
                .stream().collect(Collectors.toMap(KpiItemTable::getId, KpiItemTable::getTableName));

        List<KpiItemTableFieldVO> fieldList = kpiItemTableFieldService.getListByTableIds(tableIdList);
        Map<Long, Map<String, String>> tableFieldMap = fieldList.stream()
                .collect(Collectors.groupingBy(KpiItemTableFieldVO::getTableId,
                        Collectors.toMap(KpiItemTableFieldVO::getFieldName, KpiItemTableFieldVO::getFieldType)));

        verifyCond(tableMap, tableFieldMap, condList);

        String itemCond = JSON.toJSONString(condList);

        // 处理查询条件格式
        sqlUtil.dealCond(condList);

        // 校验查询条件
        RuleConfig ruleConfig = new RuleConfig();
        ruleConfig.setType("SQL_COND");
        ruleConfig.setContent(JSON.toJSONString(condList));
        ValidatorResultVo vo = validatorHolder.getValidatorByType("SQL_COND").validate(ruleConfig);
        if (StringUtils.hasLength(vo.getErrorMsg())) {
            throw new BizException(vo.getErrorMsg());
        }

        String whereSql = " and " + sqlUtil.buildWhereCondition(condList);

        if (StringUtils.hasLength(item.getConfig()) || null != item.getReportId()) {
            item.setWhereSql(whereSql);
            item.setItemCond(itemCond);

            String period = iKpiConfigService.getLastCycle(true);
            KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(item, period);
            ValidatorResultVo resultVo = kpiValidatorService.itemValidator(validatorDTO, false, null, null);
            if (StringUtils.hasLength(resultVo.getErrorMsg())) {
                throw new BizException(resultVo.getErrorMsg());
            }
        }

        this.update(Wrappers.<KpiItem>lambdaUpdate()
                .set(KpiItem::getWhereSql, whereSql)
                .set(KpiItem::getItemCond, itemCond)
                .eq(KpiItem::getId, item.getId()));
    }

    @Override
    public String getSql(Long id, String period) {
        KpiItem item = this.getById(id);
        if (null == item) {
            throw new BizException("未查询到对应核算项");
        }

        if (!StringUtils.hasLength(period)) {
            throw new BizException("周期不能为空");
        }

        KpiValidatorDTO dto = KpiValidatorDTO.changeToValidatorDTO(item, period);
        HashMap<String, Object> sqlInputParam = kpiValidatorService.getSqlInputParam(dto, false, dto.getParams());
        String sql = (String) sqlInputParam.get("sql");

        return sql.replace("#{period}", period);
    }

    private void verifyCond(Map<Long, String> tableMap, Map<Long, Map<String, String>> tableFieldMap, List<KpiItemCondDto> list) {
        List<KpiItemCondDto> groupList = list.stream()
                .filter(cond -> "group".equals(cond.getType()))
                .collect(Collectors.toList());

        List<KpiItemCondDto> condDtos = list.stream()
                .filter(cond -> !"group".equals(cond.getType()))
                .collect(Collectors.toList());

        if (!CollectionUtils.isEmpty(condDtos)) {
            List<String> invalidFields;
            invalidFields = condDtos.stream()
                    .filter(condDto -> null == tableFieldMap.get(condDto.getTableId()))
                    .map(KpiItemCondDto::getTableName)
                    .collect(Collectors.toList());
            if (!invalidFields.isEmpty()) {
                throw new BizException("数据表" + String.join(",", invalidFields) + "未关联到核算项");
            }

            // 验证字段是否存在
            invalidFields = condDtos.stream()
                    .filter(condDto -> !tableFieldMap.get(condDto.getTableId()).containsKey(condDto.getFieldName()))
                    .map(KpiItemCondDto::getFieldName)
                    .collect(Collectors.toList());
            if (!invalidFields.isEmpty()) {
                throw new BizException("条件字段" + String.join(",", invalidFields) + "不存在");
            }

            for (KpiItemCondDto condDto : condDtos) {
                List<String> fieldValueList = condDto.getFieldValueList();
                if (!CollectionUtils.isEmpty(fieldValueList)) {
                    String fieldValue = String.join(",", fieldValueList);
                    condDto.setFieldValue(fieldValue);
                }
            }

            // 操作符类型为IN、NOT IN、LIKE、NOT LIKE时，字段值不能为空
            List<String> operatorList = Arrays.asList(OperatorEnum.IN.getOperator(), OperatorEnum.NOT_IN.getOperator(),
                    OperatorEnum.LIKE.getOperator(), OperatorEnum.NOT_LIKE.getOperator());
            invalidFields = condDtos.stream()
                    .filter(cond -> operatorList.contains(cond.getOperator()) && !StringUtils.hasText(cond.getFieldValue()))
                    .map(KpiItemCondDto::getFieldName)
                    .collect(Collectors.toList());
            if (!invalidFields.isEmpty()) {
                throw new BizException("条件字段" + String.join(",", invalidFields) + "字段值不能为空");
            }

            list.stream().filter(cond -> !"group".equals(cond.getType()))
                    .forEach(cond -> {
                        cond.setFieldType(tableFieldMap.get(cond.getTableId()).get(cond.getFieldName()));
                        cond.setTableName(tableMap.get(cond.getTableId()));
                    });
        }

        if (!CollectionUtils.isEmpty(groupList)) {
            for (KpiItemCondDto group : groupList) {
                List<KpiItemCondDto> data = group.getData();
                if (!CollectionUtils.isEmpty(data)) {
                    verifyCond(tableMap, tableFieldMap, data);
                }
            }
        }
    }
}
