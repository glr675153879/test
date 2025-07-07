package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.hscloud.hs.cost.account.config.BaseConfig;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.mapper.second.AttendanceDeptMapper;
import com.hscloud.hs.cost.account.mapper.second.AttendanceMapper;
import com.hscloud.hs.cost.account.mapper.userAttendance.CostUserAttendanceCustomFieldsMapper;
import com.hscloud.hs.cost.account.mapper.userAttendance.FirstDistributionAccountFormulaParamMapper;
import com.hscloud.hs.cost.account.mapper.userAttendance.FirstDistributionAttendanceFormulaMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.userAttendance.*;
import com.hscloud.hs.cost.account.model.entity.base.Entity;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.entity.second.AttendanceDept;
import com.hscloud.hs.cost.account.model.entity.userAttendance.*;
import com.hscloud.hs.cost.account.model.vo.userAttendance.CustomFieldVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.ValidateJobNumberVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrListVO;
import com.hscloud.hs.cost.account.model.vo.userAttendance.export.ImportErrVo;
import com.hscloud.hs.cost.account.service.impl.userAttendance.CostUserAttendanceService;
import com.hscloud.hs.cost.account.service.impl.userAttendance.FirstDistributionAttendanceFormulaService;
import com.hscloud.hs.cost.account.service.impl.userAttendance.UserAttendanceLogService;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceService;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceCustomFieldsService;
import com.hscloud.hs.cost.account.utils.DataProcessUtil;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.admin.api.constant.MappingGroupAttributeEnum;
import com.pig4cloud.pigx.admin.api.dto.GetUserListCustomDTO;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.entity.mapping.MappingBase;
import com.pig4cloud.pigx.admin.api.feign.RemoteDeptService;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.admin.api.feign.RemoteMappingBaseService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.admin.api.vo.mapping.InnerAllDataVO;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorManager;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.ONE;
import static com.hscloud.hs.cost.account.service.impl.userAttendance.CostUserAttendanceService.getErrorHead;
import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
 * 人员考勤表(cost_user_attendance) 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class KpiUserAttendanceService extends ServiceImpl<KpiUserAttendanceMapper, KpiUserAttendance> implements IKpiUserAttendanceService {

    private static final String UPDATE = "\"%s\"由\"%s\"变更为\"%s\"";
    private final static String medGroup1 = "医生组、医技组、护理组";
    private final static String medGroup2 = "行政组、药剂组";
    private static final String ZHONGZHISHI = "中治室";
    private static final Long FORMULA_ID_SJCQTS = 1L;
    private static final Long FORMULA_ID_YCXJXCQTS = 2L;
    private final static String empList = "叶海、洪善贻、王晖";
    // private final RemoteThirdAccountUnitService remoteThirdAccountUnitService;
    private final long HRThirdId = 1783384115337363458L;
    private final RemoteDeptService remoteDeptService;
    private final AttendanceMapper attendanceMapper;
    private final AttendanceDeptMapper attendanceDeptMapper;
    private final KpiConfigMapper kpiConfigMapper;
    private final KpiMonthDaysMapper kpiMonthDaysMapper;

    private final RemoteMappingBaseService remoteMappingBaseService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private UserAttendanceLogService userAttendanceLogService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private ICostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;
    @Resource
    private RemoteUserService remoteUserService;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private DataProcessUtil dataProcessUtil;
    @Autowired
    private KpiConfigService kpiConfigService;
    @Autowired
    private DmoUtil dmoUtil;
    @Autowired
    private KpiUserAttendanceCustomService kiUserAttendanceCustomService;
    @Autowired
    private KpiUserAttendanceCustomMapper kpiUserAttendanceCustomMapper;
    @Autowired
    private FirstDistributionAttendanceFormulaService firstDistributionAttendanceFormulaService;
    @Autowired
    private RemoteDictService remoteDictService;
    @Autowired
    private KpiCalculateMapper kpiCalculateMapper;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private CostUserAttendanceCustomFieldsMapper costUserAttendanceCustomFieldsMapper;
    @Autowired
    private FirstDistributionAttendanceFormulaMapper firstDistributionAttendanceFormulaMapper;
    @Autowired
    private KpiCoefficientMapper kpiCoefficientMapper;
    @Autowired
    private FirstDistributionAccountFormulaParamMapper firstDistributionAccountFormulaParamMapper;
    @Autowired
    private KpiValueAdjustMapper kpiValueAdjustMapper;
    @Autowired
    private KpiUserCalculationRuleMapper kpiUserCalculationRuleMapper;
    @Autowired
    private KpiHsUserRuleMapper kpiHsUserRuleMapper;
    @Value("ksPurpose:RS_KSHSDY_KPI")
    private String ksPurpose;
    @Value("rsPurpose:RS_KQZHSDY_COST")
    private String kqzPurpose;

    /**
     * 表头
     */
    public static List<List<String>> getHead(List<CostUserAttendanceCustomFields> progDetailList) {
        List<List<String>> total = new ArrayList<>();
        // 固定字段
        total.add(ImmutableList.of("核算单元"));
        total.add(ImmutableList.of("考勤组"));
        total.add(ImmutableList.of("姓名"));
        total.add(ImmutableList.of("工号"));
        total.add(ImmutableList.of("核算组别"));
        total.add(ImmutableList.of("工作性质"));
        total.add(ImmutableList.of("职称"));
        total.add(ImmutableList.of("职务"));
        total.add(ImmutableList.of("岗位"));
        total.add(ImmutableList.of("是否拿奖金"));
        total.add(ImmutableList.of("奖金系数"));
        total.add(ImmutableList.of("不发奖金原因"));
        total.add(ImmutableList.of("当前考勤组所在天数"));
        total.add(ImmutableList.of("出勤天数"));
        total.add(ImmutableList.of("出勤次数"));
        total.add(ImmutableList.of("出勤系数"));
        total.add(ImmutableList.of("在册系数"));
        total.add(ImmutableList.of("一次性绩效出勤天数"));
        total.add(ImmutableList.of("一次性绩效出勤系数"));
        // 自定义字段
        for (CostUserAttendanceCustomFields detail : progDetailList) {
            total.add(ImmutableList.of(detail.getName()));
        }
        return total;
    }

    @NotNull
    private static CostUserAttendanceCustomFields getCostUserAttendanceCustomFields(String period, CostUserAttendanceCustomFields a) {
        CostUserAttendanceCustomFields customFields = new CostUserAttendanceCustomFields();
        customFields.setDt(period);
        customFields.setColumnId(a.getColumnId());
        customFields.setName(a.getName());
        customFields.setDataType(a.getDataType());
        customFields.setCode(a.getCode());
        customFields.setStatus(a.getStatus());
        customFields.setSortNum(a.getSortNum());
        customFields.setRequireFlag(a.getRequireFlag());
        customFields.setFieldType(a.getFieldType());
        customFields.setFieldCheck(a.getFieldCheck());
        customFields.setDelFlag(a.getDelFlag());
        return customFields;
    }

    @NotNull
    private static FirstDistributionAttendanceFormula getFirstDistributionAttendanceFormula(String period, FirstDistributionAttendanceFormula a) {
        FirstDistributionAttendanceFormula customFields = new FirstDistributionAttendanceFormula();
        customFields.setDt(period);
        customFields.setBusiType(a.getBusiType());
        customFields.setPlanId(a.getPlanId());
        customFields.setPlanName(a.getPlanName());
        customFields.setAttendanceFormula(a.getAttendanceFormula());
        customFields.setCarryRule(a.getCarryRule());
        customFields.setUnitName(a.getUnitName());
        customFields.setUnitId(a.getUnitId());
        customFields.setReservedDecimal(a.getReservedDecimal());
        customFields.setFormulaType(a.getFormulaType());
        customFields.setDescription(a.getDescription());
        customFields.setTenantId(a.getTenantId());
        customFields.setDelFlag(a.getDelFlag());
        return customFields;
    }

    public Long insertData(KpiUserAttendance dto) {
        dto.setTenantId(SecurityUtils.getUser().getTenantId());
        int insert = kpiUserAttendanceMapper.insert(dto);
        return (long) dto.getId();
    }

    /**
     * 人员出勤列表
     *
     * @param page
     * @param wrapper
     * @return
     */
    @Override
    public IPage<KpiUserAttendance> pageData(Page<KpiUserAttendance> page, QueryWrapper<KpiUserAttendance> wrapper
            , String busiType, String period, Map<String, Object> q) {
        //检查自定义字段列表
        List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields = costUserAttendanceCustomFieldsMapper
                .selectList(new LambdaQueryWrapper<CostUserAttendanceCustomFields>().eq(CostUserAttendanceCustomFields::getDt, period));
        DateTime yyyyMM = DateUtil.parse(period, "yyyyMM");
        String lastDt = DateUtil.format(DateUtil.offsetMonth(yyyyMM, -1), "yyyyMM");
        if (CollectionUtils.isEmpty(costUserAttendanceCustomFields)) {
            List<CostUserAttendanceCustomFields> last_costUserAttendanceCustomFields = costUserAttendanceCustomFieldsMapper
                    .selectList(new LambdaQueryWrapper<CostUserAttendanceCustomFields>().eq(CostUserAttendanceCustomFields::getDt, lastDt));
            for (CostUserAttendanceCustomFields a : last_costUserAttendanceCustomFields) {
                CostUserAttendanceCustomFields customFields = getCostUserAttendanceCustomFields(period, a);
                costUserAttendanceCustomFieldsMapper.insert(customFields);
            }
        }
        //公式继承
        List<FirstDistributionAttendanceFormula> firstDistributionAttendanceFormulas = firstDistributionAttendanceFormulaMapper
                .selectList(new LambdaQueryWrapper<FirstDistributionAttendanceFormula>().eq(FirstDistributionAttendanceFormula::getDt, period)
                        .eq(FirstDistributionAttendanceFormula::getBusiType, busiType));
        if (CollectionUtils.isEmpty(firstDistributionAttendanceFormulas)) {
            List<FirstDistributionAttendanceFormula> last_firstDistributionAttendanceFormulas = firstDistributionAttendanceFormulaMapper
                    .selectList(new LambdaQueryWrapper<FirstDistributionAttendanceFormula>()
                            .eq(FirstDistributionAttendanceFormula::getDt, lastDt)
                            .eq(FirstDistributionAttendanceFormula::getBusiType, busiType));
            for (FirstDistributionAttendanceFormula a : last_firstDistributionAttendanceFormulas) {
                FirstDistributionAttendanceFormula customFields = getFirstDistributionAttendanceFormula(period, a);
                firstDistributionAttendanceFormulaMapper.insert(customFields);
            }
        }
        List<KpiAccountUnit> unitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                //.eq(KpiAccountUnit::getStatus, 0)
                .eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(busiType), KpiAccountUnit::getBusiType, busiType));
        List<KpiUserAttendance> kpiUserAttendanceList = new ArrayList<>();
        IPage<KpiUserAttendance> pageData = null;
        //未锁定情况下查科室名称 用名称换Id
        List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, period));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if ((busiType.equals("1") && kpiConfig.getUserFlag().equals("N")) || (busiType.equals("2") && kpiConfig.getUserFlagKs().equals("N"))) {
                PageRequest<KpiUserAttendance> pr = new PageRequest<>();
                LambdaQueryWrapper<KpiUserAttendance> qr = wrapper.lambda();
                for (Map.Entry<String, Object> entry : q.entrySet()) {
                    //模糊查询科室单元即核算单元
                    if (entry.getKey().toLowerCase().contains("accountUnitName".toLowerCase())) {
                        //移除原有条件
                        q.remove(entry.getKey());
                        pr.setQ(q);
                        qr = pr.getWrapper().lambda();
                        List<KpiAccountUnit> units = Linq.of(unitList).where(t -> t.getName().contains(entry.getValue().toString())).toList();
                        if (!CollectionUtils.isEmpty(units)) {
                            List<Long> unit_ids = Linq.of(units).select(KpiAccountUnit::getId).toList();
                            qr.in(KpiUserAttendance::getAccountUnit, unit_ids);
                        }
                        else
                        {
                            qr.eq(KpiUserAttendance::getAccountUnit, 0L);
                        }
                    }
                }
                qr.orderByAsc(KpiUserAttendance::getPeriod);
                qr.eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType);
                pageData = page(page, qr);
                kpiUserAttendanceList = pageData.getRecords();
            } else {
                LambdaQueryWrapper<KpiUserAttendance> qr = wrapper.lambda();
                //时间年月倒序
                qr.orderByAsc(KpiUserAttendance::getPeriod);
                qr.eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType);
                pageData = page(page, qr);
                kpiUserAttendanceList = pageData.getRecords();
            }
        }
        if (kpiUserAttendanceList.isEmpty()) {
            return pageData;
        }
        //先全查
        List<KpiMember> userTypeList = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
                .eq(KpiMember::getMemberType, MemberEnum.EMP_TYPE)
                .eq(StringUtil.isNotBlank(busiType), KpiMember::getBusiType, busiType));
        List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
        List<SysDictItem> costTitles = remoteDictService.getDictByType("cost_titles").getData();
        for (KpiUserAttendance kpiUserAttendance : kpiUserAttendanceList) {
            // 自定义字段数值读取
            String customFields = kpiUserAttendance.getCustomFields();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> customFieldVOS = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
                kpiUserAttendance.setCustomFieldList(customFieldVOS);
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            // 科室单元信息id
            Long ks_id = kpiUserAttendance.getAccountUnit();
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
                try {
                    //KpiAccountUnit unitInfo = kpiAccountUnitService.getById(ks_id);
                    KpiAccountUnit unitInfo = Linq.of(unitList).where(x -> x.getId().equals(ks_id)).firstOrDefault();
                    if (unitInfo != null || ks_id == 0) {
                        if (ks_id == 0) {
                            kpiUserAttendance.setAccountUnitName("独立核算人员");
                            accountUnitDto.setName("独立核算人员");
                        }
                        //非锁定取关联 锁定直取不更新赋值
                        if (!kpiUserAttendance.getIsLocked().equals("1")) {
                            if (unitInfo != null) {
                                kpiUserAttendance.setAccountUnitName(unitInfo.getName());
                                accountUnitDto.setName(unitInfo.getName());
                            }
                        }
                        accountUnitDto.setId(String.valueOf(ks_id));
                        String account_groupName = null;
                        if (unitInfo != null) {
                            //核算分组代码
                            String categoryCode = unitInfo.getCategoryCode();
                            //KpiCategory one = kpiCategoryService.getOne(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getCategoryCode, categoryCode));
                            //KpiCategory one = Linq.of(categoryList).where(x -> x.getCategoryCode().equals(categoryCode)).firstOrDefault();
                            List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
                            if (busiType.equals("1")) {
                                pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
                                account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(categoryCode))
                                        .select(SysDictItem::getLabel).firstOrDefault();
                            } else {
                                pmcKpiCalculateGrouping = remoteDictService.getDictByType("dept_cost_account_group").getData();
                                account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(unitInfo.getAccountGroup()))
                                        .select(SysDictItem::getLabel).firstOrDefault();
                            }

                        }
                        if (account_groupName != null) {
                            //非锁定取关联 锁定直取不更新赋值
                            if (!kpiUserAttendance.getIsLocked().equals("1")) {
                                //String actGrp = one.getCategoryName();
                                kpiUserAttendance.setAccountGroup(account_groupName);

                                //人员类型 一人可能多个
                                List<KpiMember> user_type_list = Linq.of(userTypeList).where(t -> t.getHostId().equals(kpiUserAttendance.getUserId())
                                        && t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())).toList();

                                if (!CollectionUtils.isEmpty(user_type_list)) {
                                    StringBuilder builder = new StringBuilder();
                                    for (KpiMember b : user_type_list) {
                                        //设置人员类型
                                        SysDictItem userType = Linq.of(emp_types).
                                                where(x -> x.getItemValue().equals(b.getMemberCode())).firstOrDefault();
                                        builder.append(userType.getLabel()).append(",");
                                    }
                                    if (builder.length() > 0) {
                                        kpiUserAttendance.setUserType(String.valueOf(builder).substring(0, builder.length() - 1));
                                    }
                                    //kpiUserAttendance.setUserType(String.valueOf(builder));
                                } else {
                                    kpiUserAttendance.setUserType("");
                                }
                            }
                        }
                    }
                } catch (Exception ignored) {

                }
            } catch (Exception ignored) {
            }
            String titles = Linq.of(costTitles).where(t -> t.getItemValue().equals(kpiUserAttendance.getTitlesCode()))
                    .select(SysDictItem::getLabel).firstOrDefault();
            if (titles != null && StringUtils.isBlank(kpiUserAttendance.getTitles())) {
                kpiUserAttendance.setTitles(titles);
            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            kpiUserAttendance.setAccountUnits(accountUnits);
            if (busiType.equals("2")
                    && (kpiUserAttendance.getAccountUnit() == null || kpiUserAttendance.getAccountUnit() == 0)) {
                kpiUserAttendance.setAccountUnitName("");
            }
        }
        return pageData;
    }

    /**
     * edit 编辑
     *
     * @param dto
     * @return
     */
    @Override
    public Boolean editWithCustomFields(KpiUserAttendanceEditDto dto) {
        List<KpiUserAttendance> customParams = dto.getCustomParams();
        if (CollUtil.isEmpty(customParams)) {
            throw new BizException("请传入编辑数据");
        }
        KpiUserAttendance newEntity = customParams.get(0);
        // 获取周期月份的自然天数
        DateTime yyyyMM = DateUtil.parse(String.valueOf(newEntity.getPeriod()), "yyyyMM");
        BigDecimal lastDayOfMonth = new BigDecimal(yyyyMM.getLastDayOfMonth());

        KpiUserAttendance oldEntity = getById(newEntity.getId());
//        List<AccountUnitDto> actList = newEntity.getAccountUnits();
//        newEntity.setAccountUnit(dataProcessUtil.processList(actList));

        //没科室默认本人
        if (newEntity.getAccountUnit() == null || newEntity.getAccountUnit() == 0) {
            newEntity.setAccountUnit(0L);
            //newEntity.setAccountUnitName(newEntity.getEmpName());
            newEntity.setAccountUnitName("独立核算单元");
        }

        if (StrUtil.isBlank(newEntity.getReward())) {
            throw new BizException("是否拿奖金必填");
        }

        if (Objects.nonNull(newEntity.getAttendanceGroupDays())) {// 当前考勤组所在天数
            if (NumberUtil.isGreater(newEntity.getAttendanceGroupDays(), lastDayOfMonth) || NumberUtil.isLess(newEntity.getAttendanceGroupDays(), BigDecimal.ZERO)) {
                throw new BizException("当前考勤组所在天数填写有误");
            }
        }

        if (Objects.nonNull(newEntity.getAttendDays())) {// 实际出勤天数
            if (NumberUtil.isGreater(newEntity.getAttendDays(), lastDayOfMonth) || NumberUtil.isLess(newEntity.getAttendDays(), BigDecimal.ZERO)) {
                throw new BizException("出勤天数填写有误");
            }
        }
        // 出勤次数
        if (newEntity.getAttendCount() != null) {
            BigDecimal attendCountNumber = NumberUtil.toBigDecimal(newEntity.getAttendCount());
            if (NumberUtil.isLess(attendCountNumber, BigDecimal.ZERO)) {
                throw new BizException("出勤次数填写有误,不能小于0");
            }
        }

        // 处理自定义字段
        List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(String.valueOf(newEntity.getPeriod()));
        Map<String, CostUserAttendanceCustomFields> collect = customFields.stream()
                .collect(Collectors.toMap(CostUserAttendanceCustomFields::getColumnId, e -> e, (v1, v2) -> v1));
        if (CollUtil.isNotEmpty(newEntity.getCustomFieldList())) {
            for (CustomFieldVO customFieldVO : newEntity.getCustomFieldList()) {
                if (!collect.containsKey(customFieldVO.getId())) {
                    throw new BizException(String.format("[%s][%s]自定义字段不存在", customFieldVO.getName(), customFieldVO.getId()));
                }
                CostUserAttendanceCustomFields customFieldsDB = collect.get(customFieldVO.getId());
                CostUserAttendanceService.checkCustomField(customFieldsDB, customFieldVO.getNum(), lastDayOfMonth);
                customFieldVO.setName(customFieldsDB.getName());
            }
            List<String> customFieldStrList = newEntity.getCustomFieldList().stream().map(JSON::toJSONString).collect(Collectors.toList());
            newEntity.setCustomFields(CollUtil.join(customFieldStrList, ","));
        }
        //更新自定义字段值表
        kpiUserAttendanceCustomMapper.update(null, new LambdaUpdateWrapper<KpiUserAttendanceCustom>()
                .set(KpiUserAttendanceCustom::getDelFlag, "1")
                .eq(KpiUserAttendanceCustom::getUserAttendanceId, newEntity.getId()).eq(KpiUserAttendanceCustom::getPeriod, newEntity.getPeriod())
                .eq(KpiUserAttendanceCustom::getBusiType, newEntity.getBusiType()));
        for (CustomFieldVO customFieldVO : newEntity.getCustomFieldList()) {
            KpiUserAttendanceCustom kpiUserAttendanceCustom = new KpiUserAttendanceCustom();
            kpiUserAttendanceCustom.setPeriod(newEntity.getPeriod());
            kpiUserAttendanceCustom.setUserAttendanceId(newEntity.getId());
            kpiUserAttendanceCustom.setEmpId(newEntity.getEmpId());
            kpiUserAttendanceCustom.setEmpName(newEntity.getEmpName());
            kpiUserAttendanceCustom.setColumnId(Long.valueOf(customFieldVO.getId()));
            kpiUserAttendanceCustom.setName(customFieldVO.getName());
            try {
                kpiUserAttendanceCustom.setValue(new BigDecimal(customFieldVO.getNum()));
            } catch (Exception e) {
                kpiUserAttendanceCustom.setValue(BigDecimal.ZERO);
            }
            kpiUserAttendanceCustom.setBusiType(newEntity.getBusiType());
            kpiUserAttendanceCustom.setTenantId(SecurityUtils.getUser().getTenantId());
            kpiUserAttendanceCustom.setDelFlag("0");
            kpiUserAttendanceCustomMapper.insert(kpiUserAttendanceCustom);
        }
        super.update(Wrappers.<KpiUserAttendance>lambdaUpdate().
                set(KpiUserAttendance::getPeriod, newEntity.getPeriod()).
                set(KpiUserAttendance::getEmpId, newEntity.getEmpId()).
                set(KpiUserAttendance::getEmpName, newEntity.getEmpName()).
                set(KpiUserAttendance::getAccountUnitName, newEntity.getAccountUnitName()).
                set(KpiUserAttendance::getAttendanceGroup, newEntity.getAttendanceGroup()).
                //set(KpiUserAttendance::getUserType, newEntity.getUserType()).
                        set(KpiUserAttendance::getDutiesName, newEntity.getDutiesName()).
                //set(KpiUserAttendance::getAccountGroup, newEntity.getAccountGroup()).
                        set(KpiUserAttendance::getTitles, newEntity.getTitles()).
                set(KpiUserAttendance::getAccountUnit, newEntity.getAccountUnit()).
                set(KpiUserAttendance::getDeptCode, newEntity.getDeptCode()).
                set(KpiUserAttendance::getDeptName, newEntity.getDeptName()).
                set(KpiUserAttendance::getAttendCount, newEntity.getAttendCount()).
                set(KpiUserAttendance::getAttendRate, newEntity.getAttendRate()).
                set(KpiUserAttendance::getRegisteredRate, newEntity.getRegisteredRate()).
                set(KpiUserAttendance::getJobNature, newEntity.getJobNature()).
                set(KpiUserAttendance::getAttendDays, newEntity.getAttendDays()).
                set(KpiUserAttendance::getPost, newEntity.getPost()).
                set(KpiUserAttendance::getReward, newEntity.getReward()).
                set(KpiUserAttendance::getRewardIndex, newEntity.getRewardIndex()).
                set(KpiUserAttendance::getNoRewardReason, newEntity.getNoRewardReason()).
                set(KpiUserAttendance::getAttendanceGroupDays, newEntity.getAttendanceGroupDays()).
                set(KpiUserAttendance::getOneKpiAttendDays, newEntity.getOneKpiAttendDays()).
                set(KpiUserAttendance::getOneKpiAttendRate, newEntity.getOneKpiAttendRate()).
                set(KpiUserAttendance::getTreatRoomDays, newEntity.getTreatRoomDays()).
                set(KpiUserAttendance::getCustomFields, newEntity.getCustomFields()).
                set(KpiUserAttendance::getOriginCustomFields, newEntity.getOriginCustomFields()).
                eq(KpiUserAttendance::getId, newEntity.getId()));
        // TODO:zyj 优化日志
        // 日志
        UserAttendanceLog attendanceLog = new UserAttendanceLog();
        PigxUser user = SecurityUtils.getUser();
        attendanceLog.setDt(String.valueOf(newEntity.getPeriod()));
        attendanceLog.setJobNumber(user.getJobNumber());
        attendanceLog.setOpsById(user.getId());
        attendanceLog.setOpsBy(user.getName());
        attendanceLog.setOpsTime(LocalDateTime.now());
        attendanceLog.setOpsType(ONE);
        attendanceLog.setOpsItem(oldEntity.getEmpName());
        // 逐个比对并记录原值和新值
        List<String> changes = new ArrayList<>();
        Long accountUnit = newEntity.getAccountUnit();
        if (accountUnit != null && !NumberUtil.equals(oldEntity.getAccountUnit(), accountUnit)) {
            // 更新考勤组
            if (accountUnit != 0) {
                KpiAccountUnit kpiAccountUnit = kpiAccountUnitService.getById(accountUnit);

                List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
                if (dto.getBusiType().equals("1")) {
                    pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
                } else {
                    pmcKpiCalculateGrouping = remoteDictService.getDictByType("department_grouping").getData();
                }
                //科室更新核算组别
                String account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode()))
                        .select(SysDictItem::getLabel).firstOrDefault();
                //更新核算组别
                newEntity.setAccountGroup(account_groupName);
                changes.add(String.format("科室单元变更为【%S】", kpiAccountUnit.getName()));
            }

            newEntity.setIsEdited("1");// 标为已编辑
        }
        if (!StrUtil.equals(oldEntity.getAttendanceGroup(), newEntity.getAttendanceGroup())) {
            changes.add(String.format(UPDATE, "考勤组", oldEntity.getAttendanceGroup(), newEntity.getAttendanceGroup()));
        }
        if (!StrUtil.equals(oldEntity.getDutiesName(), newEntity.getDutiesName())) {
            changes.add(String.format(UPDATE, "职务", oldEntity.getDutiesName(), newEntity.getDutiesName()));
        }
        if (!StrUtil.equals(oldEntity.getTitles(), newEntity.getTitles())) {
            changes.add(String.format(UPDATE, "职称", oldEntity.getTitles(), newEntity.getTitles()));
        }
        if (!StrUtil.equals(oldEntity.getDeptCode(), newEntity.getDeptCode())) {
            changes.add(String.format(UPDATE, "科室编码", oldEntity.getDeptCode(), newEntity.getDeptCode()));
        }
        if (!StrUtil.equals(oldEntity.getDeptName(), newEntity.getDeptName())) {
            changes.add(String.format(UPDATE, "科室名称", oldEntity.getDeptName(), newEntity.getDeptName()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendCount(), newEntity.getAttendCount())) {
            changes.add(String.format(UPDATE, "出勤次数", oldEntity.getAttendCount(), newEntity.getAttendCount()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendRate(), newEntity.getAttendRate())) {
            changes.add(String.format(UPDATE, "出勤系数", oldEntity.getAttendRate(), newEntity.getAttendRate()));
        }
        if (!NumberUtil.equals(oldEntity.getRegisteredRate(), newEntity.getRegisteredRate())) {
            changes.add(String.format(UPDATE, "在册系数", oldEntity.getRegisteredRate(), newEntity.getRegisteredRate()));
        }
        if (!StrUtil.equals(oldEntity.getJobNature(), newEntity.getJobNature())) {
            changes.add(String.format(UPDATE, "工作性质", oldEntity.getJobNature(), newEntity.getJobNature()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendDays(), newEntity.getAttendDays())) {
            changes.add(String.format(UPDATE, "实际出勤天数", oldEntity.getAttendDays(), newEntity.getAttendDays()));
        }
        if (!StrUtil.equals(oldEntity.getPost(), newEntity.getPost())) {
            changes.add(String.format(UPDATE, "岗位", oldEntity.getPost(), newEntity.getPost()));
        }
        if (!StrUtil.equals(oldEntity.getReward(), newEntity.getReward())) {
            changes.add(String.format(UPDATE, "是否拿奖金", oldEntity.getReward(), newEntity.getReward()));
        }
        if (!NumberUtil.equals(oldEntity.getRewardIndex(), newEntity.getRewardIndex())) {
            changes.add(String.format(UPDATE, "奖金系数", oldEntity.getRewardIndex(), newEntity.getRewardIndex()));
        }
        if (!StrUtil.equals(oldEntity.getNoRewardReason(), newEntity.getNoRewardReason())) {
            changes.add(String.format(UPDATE, "不拿奖金原因", oldEntity.getNoRewardReason(), newEntity.getNoRewardReason()));
        }
        if (!NumberUtil.equals(oldEntity.getAttendanceGroupDays(), newEntity.getAttendanceGroupDays())) {
            changes.add(String.format(UPDATE, "当前考勤组所在天数", oldEntity.getAttendanceGroupDays(), newEntity.getAttendanceGroupDays()));
        }
        if (!NumberUtil.equals(oldEntity.getOneKpiAttendDays(), newEntity.getOneKpiAttendDays())) {
            changes.add(String.format(UPDATE, "一次性绩效出勤天数", oldEntity.getOneKpiAttendDays(), newEntity.getOneKpiAttendDays()));
        }
        if (!NumberUtil.equals(oldEntity.getOneKpiAttendRate(), newEntity.getOneKpiAttendRate())) {
            changes.add(String.format(UPDATE, "一次性绩效出勤系数", oldEntity.getOneKpiAttendRate(), newEntity.getOneKpiAttendRate()));
        }
        if (!StrUtil.equals(oldEntity.getTreatRoomDays(), newEntity.getTreatRoomDays())) {
            changes.add(String.format(UPDATE, "中治室", oldEntity.getTreatRoomDays(), newEntity.getTreatRoomDays()));
        }
        try {
            if (CollUtil.isNotEmpty(customFields)) {
                // 自定义字段数值读取
                String oldCustomFields = oldEntity.getCustomFields();
                String newCustomFields = newEntity.getCustomFields();
                String oldStr = "[" + oldCustomFields.replaceAll("}(?=,)", "},") + "]";
                String newStr = "[" + newCustomFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> oldCustomFieldVOS = JSON.parseArray(oldStr, CustomFieldVO.class);
                List<CustomFieldVO> newCustomFieldVOS = JSON.parseArray(newStr, CustomFieldVO.class);
                for (CostUserAttendanceCustomFields customField : customFields) {
                    String columnId = customField.getColumnId();
                    CustomFieldVO oldCustomFieldVO = CollUtil.getFirst(CollUtil.filter(oldCustomFieldVOS, e -> e.getId().equals(columnId)));
                    CustomFieldVO newCustomFieldVO = CollUtil.getFirst(CollUtil.filter(newCustomFieldVOS, e -> e.getId().equals(columnId)));
                    String oldValue = Objects.isNull(oldCustomFieldVO) ? null : oldCustomFieldVO.getNum();
                    String newValue = Objects.isNull(newCustomFieldVO) ? null : oldCustomFieldVO.getNum();
                    if (!Objects.equals(oldValue, newValue)) {
                        changes.add(String.format(UPDATE, customField.getName(), oldValue, newValue));
                    }
                }
            }
        } catch (Exception e) {
            log.info("生成自定义字段变更日志失败", e);
        }
        attendanceLog.setDescription(changes.toString());
        userAttendanceLogService.save(attendanceLog);
        return true;
    }

    private Object getCellValue(Cell cell) {
        Object result;
        switch (cell.getCellType()) {
            case STRING:
                result = cell.getStringCellValue();
                break;
            case NUMERIC:
                result = cell.getNumericCellValue();
                break;
            default:
                result = "";
        }
        return result;
    }

    @Override
    public void downloadTemplate(String dt, HttpServletResponse response) {
        try {
            List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(dt);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            EasyExcel.write(response.getOutputStream()).autoCloseStream(true).sheet("模板").head(getHead(customFields)).doWrite(new ArrayList<>());
        } catch (Exception e) {
            log.error("导出报错", e);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public R handleFileUpload(MultipartFile file) {
        // 转化数据
        if (!file.isEmpty()) {
            try (InputStream inputStream = file.getInputStream()) {
                Workbook workbook = new XSSFWorkbook(inputStream);
                Sheet sheet = workbook.getSheetAt(0);

                List<Map<String, Object>> dataList = new ArrayList<>();
                Iterator<Row> rowIterator = sheet.iterator();

                if (rowIterator.hasNext()) {
                    Row headerRow = rowIterator.next();
                    while (rowIterator.hasNext()) {
                        Row row = rowIterator.next();
                        Map<String, Object> data = new HashMap<>();
                        Iterator<Cell> cellIterator = row.iterator();

                        int cellIndex = 0;
                        while (cellIterator.hasNext()) {
                            Cell cell = cellIterator.next();
                            String headerValue = headerRow.getCell(cellIndex).getStringCellValue();
                            data.put(headerValue, getCellValue(cell));
                            cellIndex++;
                        }
                        dataList.add(data);
                    }
                }
                List<KpiUserAttendance> kpiUserAttendanceList = new ArrayList<>();
                for (Map<String, Object> data : dataList) {
                    // process data...
                    CostUserAttendanceDownloadDto costUserAttendanceDownloadDto = new CostUserAttendanceDownloadDto();
                    KpiUserAttendance kpiUserAttendance = new KpiUserAttendance();
                    Field[] fields = CostUserAttendanceDownloadDto.class.getDeclaredFields();
                    for (Field field : fields) {
                        if (field.isAnnotationPresent(Schema.class)) {
                            Schema schema = field.getAnnotation(Schema.class);
                            String description = schema.description();
                            if (data.containsKey(description)) {
                                try {
                                    field.setAccessible(true);
                                    Object value = data.get(description);
                                    PropertyEditor editor = PropertyEditorManager.findEditor(field.getType());
                                    if (editor != null) {
                                        editor.setAsText(value.toString());
                                        field.set(costUserAttendanceDownloadDto, editor.getValue());
                                    } else {
                                        field.set(costUserAttendanceDownloadDto, value);
                                    }
                                } catch (Exception e) {
                                    System.out.println("设置属性值" + description + "失败: " + e.getMessage());
                                }
                            }
                        }
                    }
                    // 自定义字段
                    List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.list();
                    List<CustomFieldVO> valueList = new ArrayList<>();
                    for (CostUserAttendanceCustomFields field : customFields) {
                        if (data.containsKey(field.getName())) {
                            try {
                                Object value = data.get(field.getName());
                                CustomFieldVO customFieldVO = new CustomFieldVO();
                                customFieldVO.setNum(value.toString());
                                customFieldVO.setName(field.getName());
                                Long id = Linq.of(customFields).where(t -> t.getName().equals(field.getName())).select(Entity::getId).firstOrDefault();
                                //Long id = costUserAttendanceCustomFieldsService.getOne(new QueryWrapper<CostUserAttendanceCustomFields>().eq("name", field.getName())).getId();
                                customFieldVO.setId(id.toString());
                                valueList.add(customFieldVO);
                            } catch (Exception e) {
                                System.out.println("设置自定义属性值" + field.getName() + "失败: " + e.getMessage());
                            }
                        }
                    }
                    BeanUtils.copyProperties(costUserAttendanceDownloadDto, kpiUserAttendance);
                    kpiUserAttendance.setAttendanceGroupDays(new BigDecimal(costUserAttendanceDownloadDto.getAttendanceGroupDays()));
                    kpiUserAttendance.setRewardIndex(new BigDecimal(costUserAttendanceDownloadDto.getRewardIndex()));
                    kpiUserAttendance.setRegisteredRate(new BigDecimal(costUserAttendanceDownloadDto.getRegisteredRate()));
                    StringBuilder sb = new StringBuilder();
                    for (CustomFieldVO value : valueList) {
                        sb.append("{ id:").append(value.getId()).append(",name:'").append(value.getName()).append("',num:").append(value.getNum()).append("},");
                    }
                    if (!valueList.isEmpty()) {
                        sb.deleteCharAt(sb.length() - 1);
                    }
                    kpiUserAttendance.setCustomFields(sb.toString());
                    String dt = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
                    kpiUserAttendance.setPeriod(Long.valueOf(dt));
                    kpiUserAttendanceList.add(kpiUserAttendance);
                }
                if (!kpiUserAttendanceList.isEmpty()) {
                    kpiUserAttendanceList.forEach(t -> {
                        t.setTenantId(SecurityUtils.getUser().getTenantId());
                    });
                    List<List<KpiUserAttendance>> partition = ListUtils.partition(kpiUserAttendanceList, 1000);
                    partition.forEach(x -> {
                        kpiUserAttendanceMapper.insertBatchSomeColumn(x);
                    });
                    //partition.forEach(this::saveBatch);
                }
                //saveBatch(kpiUserAttendanceList);
            } catch (IOException e) {
                System.out.println("文件上传失败");
            }
        }
        return R.ok();
    }

    /**
     * 检查excel行数据生成错误报告
     * 并且返回
     *
     * @param rowData
     * @param contentList
     * @param customFields
     * @param kpiAccountUnits
     * @param sysUsers
     * @param pmcKpiCalculateGrouping
     * @return {@link CostUserAttendance }
     */
    private KpiUserAttendance checkAndTransferRowData(Long dt, String[] rowData, List<String> contentList, List<CostUserAttendanceCustomFields> customFields,
                                                      List<KpiAccountUnit> kpiAccountUnits, List<SysUser> sysUsers,
                                                      List<SysDictItem> pmcKpiCalculateGrouping, ExcelImportDTO dto
            , List<KpiMember> memberList, List<SysDictItem> costTitles, List<SysDictItem> emp_types, List<SysDictItem> zw_type, KpiMonthDays config) {
        // 获取周期月份的自然天数
        DateTime yyyyMM = DateUtil.parse(String.valueOf(dt), "yyyyMM");
        BigDecimal lastDayOfMonth = new BigDecimal(yyyyMM.getLastDayOfMonth());
        KpiUserAttendance kpiUserAttendance = new KpiUserAttendance();
        String accountUnitName = rowData[0];// 科室单元
        KpiAccountUnit kpiAccountUnit;
        if (StrUtil.isBlank(accountUnitName)) {
            kpiAccountUnit = null;
            contentList.add("科室单元缺失");
        } else {
            Optional<KpiAccountUnit> any = kpiAccountUnits.stream().filter(e -> Objects.equals(accountUnitName, e.getName())).findAny();
            if (any.isPresent()) {
                kpiAccountUnit = any.get();
                //kpiUserAttendance.setAccountUnit(String.format("[{\"id\":\"%s\",\"name\":\"%s\"}]", costAccountUnit.getId(), costAccountUnit.getName()));
                kpiUserAttendance.setAccountUnit(kpiAccountUnit.getId());
                kpiUserAttendance.setAccountUnitName(kpiAccountUnit.getName());
            } else {
                kpiAccountUnit = null;
                //跟姓名一致 独立核算单元
                if (accountUnitName.equals(rowData[2])) {
                    kpiUserAttendance.setAccountUnit(0L);
                    kpiUserAttendance.setAccountUnitName("独立核算单元");
                } else {
                    contentList.add("科室单元不存在，请先在系统新增");
                }
            }
        }

        String attendanceGroup = rowData[1];// 考勤组
        kpiUserAttendance.setAttendanceGroup(attendanceGroup);
        String empName = rowData[2];// 姓名
        String empId = rowData[3];// 工号
        kpiUserAttendance.setEmpId(empId);
        //UserInfo userInfo = remoteUserService.allInfoByJobNumber(empId).getData();
        SysUser sysUser = Linq.of(sysUsers).where(t -> StringUtils.isNotBlank(t.getJobNumber()) &&
                t.getJobNumber().equals(empId)).firstOrDefault();
//        if (Objects.nonNull(userInfo)) {
//            kpiUserAttendance.setUserId(userInfo.getSysUser().getUserId());
//        }
        if (Objects.nonNull(sysUser)) {
            kpiUserAttendance.setUserId(sysUser.getUserId());
        }
        kpiUserAttendance.setEmpName(empName);
        if (StrUtil.isBlank(empName)) {
            contentList.add("姓名缺失");
        }
        if (StrUtil.isBlank(empId)) {
            contentList.add("工号缺失");
        } else {
            Optional<SysUser> any = sysUsers.stream().filter(e -> Objects.equals(empId, e.getJobNumber())).findAny();
            if (any.isPresent()) {
                SysUser sysUser2 = any.get();
                if (StrUtil.isNotBlank(empName) && !Objects.equals(empName, sysUser2.getName())) {
                    contentList.add("工号与姓名不匹配");
                }
            } else {
                contentList.add("工号不存在");
            }
        }
        String accountGroupStr = rowData[4];// 核算组别
        if (StrUtil.isBlank(accountGroupStr) && !accountUnitName.equals(empName)) {
            contentList.add("核算组别缺失");
        } else {
            if (Objects.nonNull(kpiAccountUnit)) {
//                KpiCategory one = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getLabel().equals(kpiAccountUnit.getCategoryCode())
//                        && t.getCategoryType().equals(CategoryEnum.ACCOUNT_GROUP.getType())).firstOrDefault();
                String account_groupName = "";
                if (dto.getBusiType().equals("1")) {
                    account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                    ).select(SysDictItem::getLabel).firstOrDefault();
                } else {
                    account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                            .select(SysDictItem::getLabel).firstOrDefault();
                }

                String format = String.format("[{\"id\":\"%s\",\"name\":\"%s\"}]", account_groupName, kpiAccountUnit.getCategoryCode());
                if (StrUtil.contains(format, accountGroupStr)) {
                    //填中文 页面搜索关联
                    //kpiUserAttendance.setAccountGroup(kpiAccountUnit.getCategoryCode());
                    kpiUserAttendance.setAccountGroup(account_groupName);
                } else {
                    if (dto.getBusiType().equals("1") && !accountUnitName.equals(empName)) {
                        contentList.add("核算组别与系统维护的不一致");
                    }
                }
            }
        }

        String jobNature = rowData[5];// 工作性质
        kpiUserAttendance.setJobNature(jobNature);
        String titles = rowData[6];// 职称
        String title_code = Linq.of(costTitles).where(t -> t.getLabel().equals(titles))
                .select(SysDictItem::getItemValue).firstOrDefault();
        kpiUserAttendance.setTitles(titles);
        kpiUserAttendance.setTitlesCode(title_code);
        //TODO 添加userType
        //人员类型 一人可能多个
        List<KpiMember> user_type_list = Linq.of(memberList).
                where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                        && t.getBusiType().equals(dto.getBusiType())
                        && sysUser.getUserId().equals(t.getHostId())).toList();
        if (!CollectionUtils.isEmpty(user_type_list)) {
            List<String> user_type_code_list = Linq.of(user_type_list).select(KpiMember::getMemberCode).toList();
            if (!CollectionUtils.isEmpty(user_type_code_list)) {
                kpiUserAttendance.setUserTypeCode(String.join(",", user_type_code_list));
            }
            StringBuilder builder = new StringBuilder();
            for (KpiMember b : user_type_list) {
                //设置人员类型
                SysDictItem userType = Linq.of(emp_types).
                        where(x -> x.getItemValue().equals(b.getMemberCode())).firstOrDefault();
                if (userType != null) {
                    builder.append(userType.getLabel()).append(",");
                }
            }
            if (builder.length() > 0) {
                kpiUserAttendance.setUserType(String.valueOf(builder).substring(0, builder.length() - 1));
            }
            //kpiUserAttendance.setUserType(String.valueOf(builder));
        } else {
            kpiUserAttendance.setUserType("");
        }
        String dutiesName = rowData[7];// 职务
        kpiUserAttendance.setDutiesName(dutiesName);
        List<KpiMember> user_zw_list = Linq.of(memberList).
                where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP_ZW.getType())
                        && t.getBusiType().equals(dto.getBusiType())
                        && sysUser.getUserId().equals(t.getHostId())).toList();
        if (!CollectionUtils.isEmpty(user_zw_list)) {
            KpiMember user_zw = user_zw_list.get(0);
            kpiUserAttendance.setDutiesCode(user_zw.getMemberCode());

            String value = Linq.of(zw_type).where(t -> t.getItemValue().equals(user_zw.getMemberCode())).select(SysDictItem::getLabel).firstOrDefault();
            kpiUserAttendance.setDutiesOrigin(value);
        }
        String post = rowData[8];// 岗位
        kpiUserAttendance.setPost(post);
        String reward = rowData[9];// 是否拿奖金
        if (StrUtil.isBlank(reward)) {
            contentList.add("是否拿奖金缺失");
        } else if (Objects.equals("是", reward)) {
            kpiUserAttendance.setReward("1");
        } else if (Objects.equals("否", reward)) {
            kpiUserAttendance.setReward("0");
        } else {
            contentList.add("是否拿奖金错误");
        }
        String rewardIndex = rowData[10];// 奖金系数
        if (StrUtil.isNotBlank(rewardIndex)) {
            if (!NumberUtil.isNumber(rewardIndex)) {
                contentList.add("奖金系数需要传数值");
            } else {
                kpiUserAttendance.setRewardIndex(NumberUtil.toBigDecimal(rewardIndex));
            }
        }
        String noRewardReason = rowData[11];// 不拿奖金原因
        kpiUserAttendance.setNoRewardReason(noRewardReason);
        String attendanceGroupDays = rowData[12];// 当前考勤组所在天数
        if (StrUtil.isNotBlank(attendanceGroupDays)) {
            if (!NumberUtil.isNumber(attendanceGroupDays)) {
                contentList.add("当前考勤组所在天数需要传数值");
            } else {
                BigDecimal attendanceGroupDaysNumber = NumberUtil.toBigDecimal(attendanceGroupDays);
                if (NumberUtil.isGreater(attendanceGroupDaysNumber, lastDayOfMonth) || NumberUtil.isLess(attendanceGroupDaysNumber, BigDecimal.ZERO)) {
                    contentList.add("当前考勤组所在天数不在月份天数范围内");
                } else {
                    kpiUserAttendance.setAttendanceGroupDays(attendanceGroupDaysNumber);
                }
            }
        } else {
            if (config != null && config.getMonthDays() != null) {
                kpiUserAttendance.setAttendanceGroupDays(NumberUtil.toBigDecimal(config.getMonthDays()));
            } else {
                kpiUserAttendance.setAttendanceGroupDays(NumberUtil.toBigDecimal(lastDayOfMonth));
            }
        }
        String attendDays = rowData[13];// 出勤天数
        if (StrUtil.isNotBlank(attendDays)) {
            if (!NumberUtil.isNumber(attendDays)) {
                contentList.add("出勤天数需要传数值");
            } else {
                BigDecimal attendDaysNumber = NumberUtil.toBigDecimal(attendDays);
                if (NumberUtil.isGreater(attendDaysNumber, lastDayOfMonth) || NumberUtil.isLess(attendDaysNumber, BigDecimal.ZERO)) {
                    contentList.add("出勤天数不在月份天数范围内");
                } else {
                    kpiUserAttendance.setAttendDays(attendDaysNumber);
                }
            }
        }
        String attendCount = rowData[14];// 出勤次数
        if (StrUtil.isNotBlank(attendCount)) {
            if (!NumberUtil.isNumber(attendCount)) {
                contentList.add("出勤次数需要传数值");
            } else {
                BigDecimal attendCountNumber = NumberUtil.toBigDecimal(attendCount);
                if (NumberUtil.isLess(attendCountNumber, BigDecimal.ZERO)) {
                    contentList.add("出勤次数填写有误");
                } else {
                    kpiUserAttendance.setAttendCount(Long.valueOf(attendCount));
                }
            }
        }
        String attendRate = rowData[15];// 出勤系数
        if (StrUtil.isNotBlank(attendRate)) {
            if (!NumberUtil.isNumber(attendRate)) {
                contentList.add("出勤系数需要传数值");
            } else {
                kpiUserAttendance.setAttendRate(NumberUtil.toBigDecimal(attendRate));
            }
        }
        String registeredRate = rowData[16];// 在册系数
        if (StrUtil.isNotBlank(registeredRate)) {
            if (!NumberUtil.isNumber(registeredRate)) {
                contentList.add("在册系数需要传数值");
            } else {
                kpiUserAttendance.setRegisteredRate(NumberUtil.toBigDecimal(registeredRate));
            }
        }
        String oneKpiAttendDays = rowData[17];// 一次性绩效出勤天数
        if (StrUtil.isNotBlank(oneKpiAttendDays)) {
            if (!NumberUtil.isNumber(oneKpiAttendDays)) {
                contentList.add("一次性绩效出勤天数需要传数值");
            } else {
                kpiUserAttendance.setOneKpiAttendDays(NumberUtil.toBigDecimal(oneKpiAttendDays));
            }
        }
        String oneKpiAttendRate = rowData[18];// 一次性绩效出勤系数
        if (StrUtil.isNotBlank(oneKpiAttendRate)) {
            if (!NumberUtil.isNumber(oneKpiAttendRate)) {
                contentList.add("一次性绩效出勤系数需要传数值");
            } else {
                kpiUserAttendance.setOneKpiAttendRate(NumberUtil.toBigDecimal(oneKpiAttendRate));
            }
        }
        int pos = 19;
        if (customFields.size() + 19 != rowData.length) {
            contentList.add("列个数错误，请与模板比对");
        } else {
            List<Map<String, Object>> mapList = new ArrayList<>();
            for (CostUserAttendanceCustomFields customField : customFields) {
                String cellData = rowData[pos++];
                try {
                    CostUserAttendanceService.checkCustomField(customField, cellData, lastDayOfMonth);
                } catch (BizException e) {
                    contentList.add(e.getDefaultMessage());
                } catch (Exception e) {
                    contentList.add(e.getMessage());
                }
                mapList.add(ImmutableMap.of("id", customField.getColumnId(), "name", customField.getName(), "num", cellData));
            }
            List<String> collect = mapList.stream().map(JSON::toJSONString).collect(Collectors.toList());
            kpiUserAttendance.setCustomFields(CollUtil.join(collect, ","));
            // costUserAttendance.setOriginCustomFields(CollUtil.join(collect, "$|"));
        }
        return kpiUserAttendance;
    }

    private KpiUserAttendance checkDbExist(KpiUserAttendance excelData, List<KpiUserAttendance> kpiUserAttendanceDbs) {
        Long accountUnit = excelData.getAccountUnit();
        Optional<KpiUserAttendance> dbDataOptional = kpiUserAttendanceDbs.
                stream().filter(e -> Objects.equals(e.getEmpId(), excelData.getEmpId()) && e.getAccountUnit().equals(accountUnit)).findAny();
        KpiUserAttendance costUserAttendance = dbDataOptional.orElseGet(KpiUserAttendance::new);
        BeanUtil.copyProperties(excelData, costUserAttendance, CopyOptions.create().ignoreNullValue());
        return costUserAttendance;
    }

    private List<KpiUserAttendance> addOldValue(List<KpiUserAttendance> excelData, List<KpiUserAttendance> kpiUserAttendanceDbs) {
        //kpiUserAttendanceDbs与excelData的差集
        List<KpiUserAttendance> differenceB = kpiUserAttendanceDbs.stream().
                filter(b -> excelData.stream().map(KpiUserAttendance::getEmpId).noneMatch(emp_id -> Objects.equals(b.getEmpId(), emp_id))
                        && excelData.stream().map(KpiUserAttendance::getAccountUnit).noneMatch(accountUnit -> Objects.equals(b.getAccountUnit(), accountUnit)))
                .collect(Collectors.toList());
        System.out.println(differenceB);
        return differenceB;

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportErrVo uploadFile(String[][] xlsDataArr, ExcelImportDTO dto, Long dt) {
        KpiMonthDays kpiMonthDays = kpiMonthDaysMapper.selectOne(new LambdaQueryWrapper<KpiMonthDays>().eq(KpiMonthDays::getPeriod, dt));
        List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, dt));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if (dto.getBusiType().equals("1") && kpiConfig.getUserFlag().equals("Y")) {
                throw new BizException("已锁定无法计算");
            }
            if (dto.getBusiType().equals("2") && kpiConfig.getUserFlagKs().equals("Y")) {
                throw new BizException("已锁定无法计算");
            }
        }
        //List<KpiUserAttendance> kpiUserAttendanceList = new ArrayList<>();
        // 此周期的自定义字段
        List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(String.valueOf(dt));
        //启用目录
        List<KpiCategory> categoryList = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>().eq(KpiCategory::getStatus, 0).eq(KpiCategory::getDelFlag, "0"));
        List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                //.eq(KpiAccountUnit::getStatus, 0)
                .eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiAccountUnit::getBusiType, dto.getBusiType()));

        List<SysUser> sysUsers = remoteUserService.userListByCustom(new GetUserListCustomDTO(), SecurityConstants.FROM_IN).getData();
        List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
        if (dto.getBusiType().equals("1")) {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        } else {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("dept_cost_account_group").getData();
        }

        List<KpiMember> memberList = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getMemberType, MemberEnum.EMP_TYPE.getType())
                .or().eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP_ZW.getType()));

        List<List<String>> head = getErrorHead();
        List<ImportErrListVO> details = new ArrayList<>();
        int successCount = 0;
        int failCount = 0;
        // 查出当前周期所有数据
        List<KpiUserAttendance> costUserAttendanceDbs = new ArrayList<>();
        if (Objects.equals("2", dto.getOverwriteFlag())) {
            costUserAttendanceDbs = super.list(Wrappers.<KpiUserAttendance>lambdaQuery()
                    .eq(KpiUserAttendance::getPeriod, dt).eq(StringUtil.isNotBlank(dto.getBusiType()), KpiUserAttendance::getBusiType, dto.getBusiType()));
        }
        List<SysDictItem> zw_type = remoteDictService.getDictByType("cost_duties").getData();
        List<SysDictItem> costTitles = remoteDictService.getDictByType("cost_titles").getData();
        List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
        // 导入数据
        for (int i = 1; i < xlsDataArr.length; i++) {
            String[] rowData = xlsDataArr[i];
            // 错误说明
            List<String> contentList = new ArrayList<>();
            try {
                // 校验并组装行数据
                KpiUserAttendance kpiUserAttendanceExcel =
                        checkAndTransferRowData(dt, rowData, contentList, customFields, kpiAccountUnits
                                , sysUsers, pmcKpiCalculateGrouping, dto, memberList,
                                costTitles, emp_types, zw_type, kpiMonthDays);
                if (CollUtil.isEmpty(contentList)) {
                    KpiUserAttendance kpiUserAttendance = kpiUserAttendanceExcel;
                    // 增量导入：根据工号、科室单元是否已存在决定是更新还是新增
                    if (Objects.equals("2", dto.getOverwriteFlag())) {
                        kpiUserAttendance = checkDbExist(kpiUserAttendanceExcel, costUserAttendanceDbs);
                    }
                    if (kpiUserAttendance.getId() == null) {
                        kpiUserAttendance.setPeriod(dt);
                        if (StringUtil.isNotBlank(dto.getBusiType())) {
                            kpiUserAttendance.setBusiType(dto.getBusiType());
                        }
                        kpiUserAttendance.setDelFlag("0");
                        kpiUserAttendance.setIsEdited("0");
                        kpiUserAttendance.setSourceType("1");
                        kpiUserAttendance.setIsLocked("0");
                        costUserAttendanceDbs.add(kpiUserAttendance);
                    }
                    //super.saveOrUpdate(kpiUserAttendance);
                    // // 每次新增后将excel放入到db中，防止excel本身有重复数据
                    // costUserAttendanceDbs.add(costUserAttendance);
                }
            } catch (Exception e) {
                log.error("导入报错", e);
                String message = e.getMessage();
                contentList.add(message);
            }
            if (CollUtil.isNotEmpty(contentList)) {
                // 生成错误说明
                failCount++;
                ImportErrListVO build = ImportErrListVO.builder().lineNum(i + 1).data(ImmutableList.of(rowData[2], rowData[1], rowData[0])).contentList(contentList).content(StrUtil.join(";",
                        contentList)).build();
                details.add(build);
                // 生成错误说明
                if (Objects.equals("2", dto.getContinueFlag())) {
                    break;
                }
            } else {
                successCount++;
            }
        }
        //删除旧数据
        //if (Objects.equals("1", dto.getOverwriteFlag())) {
        // 1覆盖模式：删除此周期所有数据
        super.remove(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dt)
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiUserAttendance::getBusiType, dto.getBusiType()));
        //}
        if (!costUserAttendanceDbs.isEmpty()) {
            costUserAttendanceDbs.forEach(t -> {
                t.setTenantId(SecurityUtils.getUser().getTenantId());
            });
            List<List<KpiUserAttendance>> partition = ListUtils.partition(costUserAttendanceDbs, 1000);
            partition.forEach(x -> {
                kpiUserAttendanceMapper.insertBatchSomeColumn(x);
            });
        }
        ImportErrVo build = ImportErrVo.builder().details(details).successCount(successCount).failCount(failCount).head(head).build();
        redisUtil.set(CacheConstants.IMPORT_ERROR_COST_USER_ATTENDANCE + dt, JSON.toJSONString(build), 30, TimeUnit.MINUTES);
        return build;
    }

    @Override
    public void exportData(ExcelExportDTO dto, HttpServletResponse response) {
        try {
//            LambdaQueryWrapper<KpiUserAttendance> qrTemp = pr.getWrapper().lambda();
//            qrTemp.orderByAsc(KpiUserAttendance::getPeriod);
            List<KpiUserAttendance> list = list(new LambdaQueryWrapper<KpiUserAttendance>()
                    .eq(KpiUserAttendance::getPeriod, dto.getPeriod())
                    .eq(KpiUserAttendance::getBusiType, dto.getBusiType()));
            //List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt((String.valueOf(pr.getQ().get("period"))));
            List<CostUserAttendanceCustomFields> customFields = costUserAttendanceCustomFieldsService.listByDt(String.valueOf(dto.getPeriod()));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            EasyExcel.write(response.getOutputStream())
                    .autoCloseStream(true)
                    .sheet("模板")
                    .head(CostUserAttendanceService.getHead(customFields))
                    .doWrite(getContent(list, customFields));
        } catch (Exception e) {
            log.error("导出报错", e);
        }
    }

    private List<List<Object>> getContent(List<KpiUserAttendance> userList, List<CostUserAttendanceCustomFields> customFieldsList) throws JsonProcessingException {
        List<List<Object>> totalContent = new ArrayList<>();
        List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.list();
        List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
        if (userList.get(0).getBusiType().equals("1")) {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        } else {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("department_grouping").getData();
        }
        Map<Long, KpiAccountUnit> map = kpiAccountUnits.stream().collect(Collectors.toMap(KpiAccountUnit::getId, a -> a));
        for (KpiUserAttendance kpiUserAttendance : userList) {
            //log.info("time:{}", LocalDateTime.now());
            List<Object> list = new ArrayList<>();

            String accountGroupStr = "";
            // 科室单元
            try {
                Long accountUnitStr = kpiUserAttendance.getAccountUnit();
                KpiAccountUnit kpiAccountUnit = Linq.of(kpiAccountUnits).where(t -> t.getId().equals(accountUnitStr)).firstOrDefault();
                if (kpiAccountUnit != null) {
                    if (StringUtils.isEmpty(kpiAccountUnit.getName())) {
                        list.add("");
                    } else {
                        accountGroupStr = kpiAccountUnit.getCategoryCode();
                        list.add(kpiAccountUnit.getName());
                    }
                }
                if (accountUnitStr == 0) {
                    list.add(kpiUserAttendance.getEmpName());
                }
            } catch (Exception e) {
                list.add("");
            }
            // 考勤组
            list.add(kpiUserAttendance.getAttendanceGroup());
            // 姓名
            list.add(kpiUserAttendance.getEmpName());
            // 工号
            list.add(kpiUserAttendance.getEmpId());
            // 核算组别
            if (StringUtil.isEmpty(accountGroupStr)) {
                list.add("");
            } else {
                String finalAccountGroupStr = accountGroupStr;
                String account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(finalAccountGroupStr))
                        .select(SysDictItem::getLabel).firstOrDefault();
                if (account_groupName != null) {
                    list.add(account_groupName);
                } else {
                    list.add("");
                }
            }
            // 工作性质
            list.add(kpiUserAttendance.getJobNature());
            // 职称
            list.add(kpiUserAttendance.getTitles());
            // 职务
            list.add(kpiUserAttendance.getDutiesName());
            // 岗位
            list.add(kpiUserAttendance.getPost());
            // 是否拿奖金
            list.add(kpiUserAttendance.getReward().equals("1") ? "是" : "否");
            // 奖金系数
            list.add(kpiUserAttendance.getRewardIndex());
            // 不拿奖金原因
            list.add(kpiUserAttendance.getNoRewardReason());
            // 当前考勤组所在天数
            list.add(kpiUserAttendance.getAttendanceGroupDays());
            // 出勤天数
            list.add(kpiUserAttendance.getAttendDays());
            // 出勤次数
            list.add(kpiUserAttendance.getAttendCount() == null ? "0" : kpiUserAttendance.getAttendCount());
            // 出勤系数
            list.add(kpiUserAttendance.getAttendRate());
            // 在册系数
            list.add(kpiUserAttendance.getRegisteredRate());
            // 一次性绩效出勤天数
            list.add(kpiUserAttendance.getOneKpiAttendDays());
            // 一次性出勤系数
            list.add(kpiUserAttendance.getOneKpiAttendRate());

            List<CustomFieldVO> customFieldVOS = getCustomFieldsList(kpiUserAttendance);
            kpiUserAttendance.setCustomFieldList(customFieldVOS);

            for (CostUserAttendanceCustomFields customField : customFieldsList) {
                Object value = null;
                for (CustomFieldVO customFieldVO : customFieldVOS) {
                    if (Objects.equals(customField.getColumnId(), customFieldVO.getId())) {
                        value = customFieldVO.getNum();
                    }
                }
                list.add(value);
            }
            System.out.println("time end" + LocalDateTime.now());
            totalContent.add(list);
        }
        System.out.println("============================end===========================");
        return totalContent;
    }

    private List<CustomFieldVO> getCustomFieldsList(KpiUserAttendance kpiUserAttendance) {
        String customFields = kpiUserAttendance.getCustomFields();
        if(customFields==null)
        {
            return new ArrayList<>();
        }
        String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
        List<CustomFieldVO> customFieldVOS = new ArrayList<>();
        JSONArray jsonArray = JSONArray.parseArray(inputList);
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String name = jsonObject.getString("name");
            String num = jsonObject.getString("num");
            CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
            customFieldVOS.add(customFieldVO);
        }
        return customFieldVOS;
    }

    public IPage<KpiUserAttendanceDto> toMatchList(KpiChangeUseSearchDto searchDto) {
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(searchDto.getBusiType()), KpiAccountUnit::getBusiType, searchDto.getBusiType()));
        // 系统没有给匹配科室单元的数据
        List<KpiUserAttendanceDto> rtnList2 = new ArrayList<>();
        List<KpiUserAttendanceDto> rtnList = new ArrayList<>();
//        LambdaQueryWrapper<KpiUserAttendance> qr = wrapper.lambda();
//        qr.eq(KpiUserAttendance::getIsEdited, "0");
//        qr.isNull(KpiUserAttendance::getAccountUnit).or().eq(KpiUserAttendance::getAccountUnit, "0");
        Page<KpiAccountUserDto> matchPage = new Page<>(searchDto.getCurrent(), searchDto.getSize());
        IPage<KpiUserAttendanceDto> list = kpiUserAttendanceMapper.findMatch(matchPage, searchDto);
        for (KpiUserAttendanceDto item : list.getRecords()) {
            //科室单元id
//            Long accountUnitInfo = item.getAccountUnit();
//            if (accountUnitInfo == null) {
//                List<AccountUnitDto> accountUnits = new ArrayList<>();
//                item.setAccountUnits(accountUnits);
//                rtnList.add(item);
//            }
//            AccountUnitDto accountUnitDto = new AccountUnitDto();
//            try {
//                if (accountUnitInfo != null) {
//                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
//                    accountUnitDto.setName(kpiAccountUnit.getName());
//                    accountUnitDto.setId(String.valueOf(kpiAccountUnit.getId()));
//                }
//            } catch (Exception ignored) {
//            }
//            List<AccountUnitDto> accountUnits = new ArrayList<>();
//            if (ObjectUtil.isEmpty(accountUnitDto)) {
//                accountUnits.add(accountUnitDto);
//                item.setAccountUnits(accountUnits);
//                rtnList.add(item);
//            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            item.setAccountUnits(accountUnits);
        }
        // 继承上月科室
//        for (KpiUserAttendanceDto rItem : rtnList) {
//            Long dt = rItem.getPeriod();
//            YearMonth yearMonth = YearMonth.parse(dt.toString(), DateTimeFormatter.ofPattern("yyyyMM"));
//            YearMonth nextYearMonth = yearMonth.minusMonths(1);
//            String nextDt = nextYearMonth.format(DateTimeFormatter.ofPattern("yyyyMM"));
//            KpiUserAttendance lastMonthData = getOne(new QueryWrapper<KpiUserAttendance>()
//                    .eq("emp_name", rItem.getEmpName())
//                    .eq("period", nextDt)
//                    .eq(StringUtil.isNotBlank(searchDto.getBusiType()), "busi_type", searchDto.getBusiType())
//                    .last("limit 1"));
//            if (rItem.getAccountUnits().isEmpty()) {
//                rtnList2.add(rItem);
//            } else {
//                try {
//                    rItem.setAccountUnit(lastMonthData.getAccountUnit());
//                } catch (Exception ignored) {
//                }
//            }
//        }
//        Pageable pageable = PageRequest.of((int) (searchDto.getCurrent() - 1), (int) searchDto.getSize());
//        org.springframework.data.domain.Page<KpiUserAttendanceDto> pageFromList = PageUtil.createPageFromList(rtnList2, pageable);
        return list;
    }

    public IPage<KpiUserAttendanceDto> manualMatchList(KpiChangeUseSearchDto searchDto) {
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(searchDto.getBusiType()), KpiAccountUnit::getBusiType, searchDto.getBusiType()));
        //上月手动匹配人员
//        List<KpiMember> last_period_manuallist = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
//                .eq(KpiMember::getPeriod, LocalDateTime.now().minusMonths(1))
//                .eq(KpiMember::getMemberType, MemberEnum.USER_DEPT.getType()));
//        Page<KpiUserAttendanceDto> rtnPage = new Page<>();
//        LambdaQueryWrapper<KpiUserAttendance> qr = pr.getWrapper().lambda();
//        qr.eq(KpiUserAttendance::getIsEdited, "1");
//        Page<KpiUserAttendance> originData = page(pr.getPage(), qr);
//        List<KpiUserAttendance> targetList = originData.getRecords();
        Page<KpiAccountUserDto> matchPage = new Page<>(searchDto.getCurrent(), searchDto.getSize());
        IPage<KpiUserAttendanceDto> list = kpiUserAttendanceMapper.findMatch(matchPage, searchDto);
        //List<KpiUserAttendanceDto> rtnList = new ArrayList<>();
        for (KpiUserAttendanceDto item : list.getRecords()) {
            // 1添加本月手动
            // if (item.getIsEdited().equals(ONE)) {
            Long accountUnitInfo = item.getAccountUnit();
            List<AccountUnitDto> accountUnits = new ArrayList<>();

            AccountUnitDto accountUnitDto = new AccountUnitDto();
            if (accountUnitInfo != null && accountUnitInfo == 0L && searchDto.getBusiType().equals("1")) {
                accountUnitDto.setName("独立核算人员");
                accountUnitDto.setId("0");
                item.setAccountUnitName("独立核算人员");
            } else {
                try {
                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
                    accountUnitDto.setName(kpiAccountUnit.getName());
                    accountUnitDto.setId(String.valueOf(kpiAccountUnit.getId()));
                } catch (Exception ignored) {
                }
            }
            accountUnits.add(accountUnitDto);
            item.setAccountUnits(accountUnits);
        }
        return list;


//                KpiUserAttendance lastMonthData = getOne(new QueryWrapper<KpiUserAttendance>()
//                        .eq("emp_name", item.getEmpName())
//                        .eq("period", LocalDateTime.now().minusMonths(1))
//                        .eq("account_unit", item.getAccountUnit())
//                        .last("limit 1"));
//                if (lastMonthData != null) {
//                    BeanUtils.copyProperties(lastMonthData, dto);
//                    Long accountUnitInfo = item.getAccountUnit();
//                    ObjectMapper mapper = new ObjectMapper();
//                    AccountUnitDto accountUnitDto = new AccountUnitDto();
//                    try {
//                        KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
//                        accountUnitDto.setName(kpiAccountUnit.getName());
//                        accountUnitDto.setId(String.valueOf(kpiAccountUnit.getId()));
//                    } catch (Exception ignored) {
//                    }
//                    List<AccountUnitDto> accountUnits = new ArrayList<>();
//                    accountUnits.add(accountUnitDto);
//                    dto.setAccountUnits(accountUnits);
//                    rtnList.add(dto);
//                }
    }

    // 指完全没有人为改动，按照系统匹配规则来的数据显示在这里
    public IPage<KpiUserAttendanceDto> sysMatchList(KpiChangeUseSearchDto searchDto) {
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(searchDto.getBusiType()), KpiAccountUnit::getBusiType, searchDto.getBusiType()));
//        Page<KpiUserAttendanceDto> rtnPage = new Page<>();
//        LambdaQueryWrapper<KpiUserAttendance> qr = pr.getWrapper().lambda();
//        qr.isNotNull(KpiUserAttendance::getAccountUnit);
//        qr.eq(KpiUserAttendance::getIsEdited, "0");
//        Page<KpiUserAttendance> originData = page(pr.getPage(), qr);
//        List<KpiUserAttendance> current = originData.getRecords();
        Page<KpiAccountUserDto> matchPage = new Page<>(searchDto.getCurrent(), searchDto.getSize());
        IPage<KpiUserAttendanceDto> list = kpiUserAttendanceMapper.findMatch(matchPage, searchDto);
        List<KpiUserAttendanceDto> rtnList = new ArrayList<>();
        for (KpiUserAttendanceDto item : list.getRecords()) {
            Long accountUnitInfo = item.getAccountUnit();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            if (accountUnitInfo != null && accountUnitInfo == 0L && searchDto.getBusiType().equals("1")) {
                accountUnitDto.setName("独立核算人员");
                accountUnitDto.setId("0");
                item.setAccountUnitName("独立核算人员");
            } else {
                try {
                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
                    accountUnitDto.setName(kpiAccountUnit.getName());
                    accountUnitDto.setId(String.valueOf(kpiAccountUnit.getId()));
                } catch (Exception ignored) {

                }
            }
            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            item.setAccountUnits(accountUnits);
            //rtnList.add(dto);
        }
//        rtnPage.setRecords(rtnList);
//        rtnPage.setCurrent(originData.getCurrent());
//        rtnPage.setTotal(originData.getTotal());
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public Boolean updateData(KpiUserAttendanceEditDto dto) {
        String dt = dto.getPeriod();
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiAccountUnit::getBusiType, dto.getBusiType()));
        List<KpiMember> members = new ArrayList<>();
        for (KpiUserAttendance kpiUserAttendance : dto.getCustomParams()) {
            kpiUserAttendance.setBusiType(dto.getBusiType());
            KpiUserAttendance oldEntity = getById(kpiUserAttendance.getId());
//            List<AccountUnitDto> actList = kpiUserAttendance.getAccountUnits();
//            kpiUserAttendance.setAccountUnit(dataProcessUtil.processList(actList));
            // 日志
            UserAttendanceLog log = new UserAttendanceLog();
            PigxUser user = SecurityUtils.getUser();
            log.setDt(dt);
            log.setJobNumber(user.getJobNumber());
            log.setOpsById(user.getId());
            log.setOpsBy(user.getName());
            log.setOpsTime(LocalDateTime.now());
            log.setOpsType(ONE);
            log.setOpsItem(oldEntity.getEmpName());
            // 逐个比对并记录原值和新值
            StringBuilder changes = new StringBuilder();
            //List<AccountUnitDto> newAccountUnitInfo = kpiUserAttendance.getAccountUnits();
            if (kpiUserAttendance.getAccountUnit() != null) {
                String newAccountUnitName = kpiUserAttendance.getAccountUnitName();
                // 更新考勤组
                Long newAccountUnitId = kpiUserAttendance.getAccountUnit();
                kpiUserAttendance.setAccountUnit(newAccountUnitId);
                if (!newAccountUnitId.equals(0L)) {
                    //非迁移自己
                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(newAccountUnitId)).firstOrDefault();
                    kpiUserAttendance.setAccountUnitName(kpiAccountUnit.getName());
                    List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
                    if (dto.getBusiType().equals("1")) {
                        pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
                    } else {
                        pmcKpiCalculateGrouping = remoteDictService.getDictByType("dept_cost_account_group").getData();
                    }
                    String account_groupName = "";
                    if (dto.getBusiType().equals("1")) {
                        account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                        ).select(SysDictItem::getLabel).firstOrDefault();
                    } else {
                        account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                .select(SysDictItem::getLabel).firstOrDefault();
                    }
                    //更新核算组别
                    kpiUserAttendance.setAccountGroup(account_groupName);
                    changes.append("科室单元变更为 ").append(newAccountUnitName);
                } else {
                    kpiUserAttendance.setAccountUnitName("独立核算人员");
                }
                kpiUserAttendance.setIsEdited("1");// 标为已编辑
                //TODO 处理member 记录记录
                kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>()
                        .eq(KpiMember::getPeriod, Long.valueOf(dt))
                        .eq(KpiMember::getHostId, oldEntity.getUserId())
                        .eq(KpiMember::getHostCode, oldEntity.getAttendanceGroup())
                        .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType())
                        .eq(KpiMember::getMemberType, MemberEnum.USER_DEPT.getType()));
                KpiMember member = new KpiMember();
                member.setPeriod(Long.valueOf(dt));
                if (StringUtil.isNotBlank(dto.getBusiType())) {
                    member.setBusiType(dto.getBusiType());
                }
                member.setHostId(oldEntity.getUserId());
                member.setMemberId(newAccountUnitId);
                member.setHostCode(oldEntity.getAttendanceGroup());
                member.setMemberType(MemberEnum.USER_DEPT.getType());
                member.setCreatedDate(new Date());
                members.add(member);
            }
            String newUserTypeInfo = kpiUserAttendance.getUserType();
            String oldUserTypeInfo = oldEntity.getUserType();
            if (newUserTypeInfo != null) {
//                JSONObject newUserTypeJson = JSONObject.parseObject(newUserTypeInfo);
//                String newUserType = newUserTypeJson.getString("label");
//                JSONObject oldUserTypeJson = JSONObject.parseObject(oldUserTypeInfo);
//                String oldUserType = ObjectUtil.isNotEmpty(oldUserTypeInfo) ? oldUserTypeJson.getString("label") : "";
//                if (ObjectUtil.isNotEmpty(newUserType)) {
//                    changes.append(String.format(UPDATE, "人员类型", oldUserType, newUserType));
//                }
                if (ObjectUtil.isNotEmpty(newUserTypeInfo)) {
                    changes.append(String.format(UPDATE, "人员类型", oldUserTypeInfo, newUserTypeInfo));
                }
            }
            if (ObjectUtil.isNotEmpty(kpiUserAttendance.getAttendDays())) {
                changes.append(String.format(UPDATE, "出勤次数", oldEntity.getAttendDays(), kpiUserAttendance.getAttendDays()));
            }
            if (ObjectUtil.isNotEmpty(kpiUserAttendance.getRegisteredRate())) {
                changes.append(String.format(UPDATE, "在册系数", oldEntity.getRegisteredRate(), kpiUserAttendance.getRegisteredRate()));
            }
            log.setDescription(changes.toString());


            userAttendanceLogService.save(log);
        }
//        if (!dto.getCustomParams().isEmpty()) {
//            List<List<KpiUserAttendance>> partition = ListUtils.partition(dto.getCustomParams(), 1000);
//            partition.forEach(x -> {
//                kpiUserAttendanceMapper.insertBatchSomeColumn(x);
//            });
//            //partition.forEach(this::saveBatch);
//        }
        if (!members.isEmpty()) {
            members.forEach(t -> {
                t.setTenantId(SecurityUtils.getUser().getTenantId());
            });
            List<List<KpiMember>> partition = ListUtils.partition(members.stream().distinct().collect(Collectors.toList()), 1000);
            partition.forEach(x -> {
                kpiMemberMapper.insertBatchSomeColumn(x);
            });
        }
        return updateBatchById(dto.getCustomParams());
    }

    @Override
    public List<Long> historyList(QueryWrapper<KpiUserAttendance> wrapper) {
        List<Long> rtnList = new ArrayList<>();
        List<KpiUserAttendance> dateList = list();
        if (!CollectionUtils.isEmpty(dateList)) {
            Map<Long, Long> monthlyCounts = dateList.stream().collect(Collectors.groupingBy(KpiUserAttendance::getPeriod, Collectors.counting()));
            monthlyCounts.forEach((month, count) -> rtnList.add(month));
            rtnList.sort(Collections.reverseOrder());
        }
        return rtnList;
    }

   /* @Override
    public Boolean lockData(String dt, String busiType) {
        return lockData(dt, busiType, null);
    }*/

    /**
     * 数据锁定
     *
     * @param dt
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    @SchedulerLock(name = BaseConfig.appCode + "_lockData")
    public Boolean lockData(String dt, String busiType, Long tenantId, boolean empRefresh) {
        if (tenantId == null) {
            tenantId = SecurityUtils.getUser().getTenantId();
        }
        List<KpiConfig> list = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>()
                .eq(KpiConfig::getPeriod, Long.valueOf(dt)));
        List<KpiMember> memberList = kpiMemberService.list();
        List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
        if (busiType.equals("1")) {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        } else {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("dept_cost_account_group").getData();
        }
        List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(busiType), KpiAccountUnit::getBusiType, busiType));
        Map<Long, KpiAccountUnit> kpiUserAttendanceMap = kpiAccountUnits.stream().collect(Collectors.toMap(KpiAccountUnit::getId, a -> a));
        boolean rtn = false; // 是否有数据被锁定
        LambdaQueryWrapper<KpiUserAttendance> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KpiUserAttendance::getPeriod, Long.valueOf(dt)).eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType);
        List<KpiUserAttendance> rtnList = list(wrapper);
        if (!rtnList.isEmpty()) {
            for (KpiUserAttendance item : rtnList) {
                if (item.getIsLocked().equals("1")) {
                    if (busiType.equals("1") && list.get(0).getIssuedFlag().equals("Y") && !empRefresh) {
                        throw new BizException("该月数据已发布，无法解锁");
                    } else {
                        item.setIsLocked("0");
                        if (busiType.equals("1")) {
                            list.get(0).setUserFlag("N");
                            list.get(0).setAttendanceUpdateDate(new Date());
                        }
                        if (busiType.equals("2")) {
                            list.get(0).setUserFlagKs("N");
                            list.get(0).setAttendanceUpdateDateKs(new Date());
                        }
                    }
                } else {
                    rtn = true;
                    item.setIsLocked("1");
                    if (busiType.equals("1")) {
                        list.get(0).setUserFlag("Y");
                        list.get(0).setAttendanceUpdateDate(new Date());
                    }
                    if (busiType.equals("2")) {
                        list.get(0).setUserFlagKs("Y");
                        list.get(0).setAttendanceUpdateDateKs(new Date());
                    }
                }
            }
        } else {
            if (busiType.equals("1")) {
                if (list.get(0).getUserFlag().equals("Y")) {
                    list.get(0).setUserFlag("N");
                    list.get(0).setAttendanceUpdateDate(new Date());
                } else {
                    list.get(0).setUserFlag("Y");
                    list.get(0).setAttendanceUpdateDate(new Date());
                }
            } else if (busiType.equals("2")) {
                if (list.get(0).getUserFlagKs().equals("Y")) {
                    list.get(0).setUserFlagKs("N");
                    list.get(0).setAttendanceUpdateDateKs(new Date());
                } else {
                    list.get(0).setUserFlagKs("Y");
                    list.get(0).setAttendanceUpdateDateKs(new Date());
                }
            }
        }
        //更新总配置表
        kpiConfigService.updateById(list.get(0));
        //刷新归集
        //imputationRefresh.refresh(null);
        // 将该月数据移植到原考勤表(cost_attendance)并删除同核算周期内旧数据
        if (rtn || empRefresh) {
            List<UserIdAndDeptId> userDept = kpiUserAttendanceMapper.getUserDept();
            List<UserCoreVo> sysUsers = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
            List<Attendance> targetList = new ArrayList<>();
            List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
            List<SysDictItem> zw_type = remoteDictService.getDictByType("cost_duties").getData();
            for (KpiUserAttendance item : rtnList) {
                Attendance attendance = new Attendance();
                try {
                    //UserInfo userInfo = remoteUserService.allInfoByJobNumber(item.getEmpId()).getData();
                    UserCoreVo sysUser = Linq.of(sysUsers).where(t -> StringUtils.isNotBlank(t.getJobNumber()) &&
                            t.getJobNumber().equals(item.getEmpId())).firstOrDefault();
                    //SysUser user = userInfo == null ? new SysUser() : userInfo.getSysUser();
                    UserCoreVo user = sysUser == null ? new UserCoreVo() : sysUser;
                    attendance.setUserId(user.getUserId());
                    UserIdAndDeptId userIdAndDeptId = Linq.of(userDept).where(t -> t.getUserId().equals(user.getUserId()))
                            .firstOrDefault();
                    if (userIdAndDeptId.getDeptId() != null) {
                        attendance.setDeptId(userIdAndDeptId.getDeptId());
                        item.setDeptCode(String.valueOf(userIdAndDeptId.getUserId()));
                    }
                    if (StringUtil.isNotBlank(userIdAndDeptId.getName())) {
                        attendance.setDeptName(userIdAndDeptId.getName());
                        item.setDeptName(userIdAndDeptId.getName());
                    }
                    attendance.setEmpCode(item.getEmpId());
                    attendance.setEmpName(user.getName());
                    attendance.setCycle(String.valueOf(item.getPeriod()));
                    if (item.getAccountUnit() != null) {
//                        JSONArray accountUnitJsonArray = JSONObject.parseArray(item.getAccountUnit());
//                        JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
//                        String unitName = firstObject.getString("name");
//                        String unitId = firstObject.getString("id");
                        attendance.setAccountUnitId(String.valueOf(item.getAccountUnit()));
                        KpiAccountUnit kpiAccountUnit = kpiUserAttendanceMap.get(item.getAccountUnit());
                        if (ObjectUtil.isEmpty(kpiAccountUnit)) {
                            attendance.setGroupName("");
                        } else {
                            String grpIngo = kpiAccountUnit.getCategoryCode();
//                            JSONObject grpJson = JSONObject.parseObject(grpIngo);
//                            String grpName = grpJson.getString("label");

                            String account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(grpIngo))
                                    .select(SysDictItem::getLabel).firstOrDefault();
                            attendance.setGroupName(account_groupName);
                            attendance.setAccountUnitName(kpiAccountUnit.getName());
                            attendance.setTitle(item.getTitles());
                        }
                    }
//                    if (StringUtils.isNotBlank(item.getDeptCode())) {
//                        String deptCode = item.getDeptCode();
//                        R<SysDept> sysDeptR = remoteDeptService.getByCode(deptCode, SecurityConstants.FROM_IN);
//                        if (sysDeptR.isOk() && sysDeptR.getData() != null) {
//                            SysDept sysDept = sysDeptR.getData();
//                            attendance.setDeptId(sysDept.getDeptId());
//                            attendance.setDeptName(sysDept.getName());
//                        } else {
//                            log.warn("根据部门编码获取部门信息失败，部门编码：" + deptCode);
//                        }
//                    }
                    attendance.setWorkRate(item.getAttendRate() == null ? null : item.getAttendRate().toString());
                    attendance.setGroupWorkdays(item.getAttendanceGroupDays() == null ? null : item.getAttendanceGroupDays().toString());
                    attendance.setZaiceRate(item.getRegisteredRate() == null ? null : item.getRegisteredRate().toString());
                    attendance.setWorkType(item.getJobNature());
                    attendance.setWorkdayszl(item.getAttendDays() == null ? null : item.getAttendDays().toString());
                    attendance.setIfGetAmt(item.getReward().equals("0") ? "否" : "是");
                    attendance.setCustomFields(item.getCustomFields());
                    attendance.setTenantId(tenantId);
                    attendance.setWorkdays(String.valueOf(item.getAttendanceGroupDays()));
                    targetList.add(attendance);
                } catch (Exception e) {
                    log.error("锁定数据时，用户信息获取失败:" + item.getEmpName(), e);
                }
                //更新考勤
                KpiAccountUnit kpiAccountUnit = kpiUserAttendanceMap.get(item.getAccountUnit());
                if (kpiAccountUnit != null) {
                    String grpIngo = kpiAccountUnit.getCategoryCode();
                    if (StringUtils.isNotBlank(grpIngo)) {
                        String account_groupName = "";
                        if (busiType.equals("1")) {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(grpIngo)
                            ).select(SysDictItem::getLabel).firstOrDefault();
                        } else {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                    .select(SysDictItem::getLabel).firstOrDefault();
                        }
                        item.setAccountGroup(account_groupName);
                    }
                    item.setAccountUnitName(kpiAccountUnit.getName());
                }
                //TODO 添加userType
                //人员类型 一人可能多个
                List<KpiMember> user_type_list = Linq.of(memberList).
                        where(t -> t.getMemberType().equals(MemberEnum.EMP_TYPE.getType())
                                && t.getBusiType().equals(busiType)
                                && item.getUserId().equals(t.getHostId())).toList();
                if (!CollectionUtils.isEmpty(user_type_list)) {
                    List<String> user_type_code_list = Linq.of(user_type_list).select(KpiMember::getMemberCode).toList();
                    if (!CollectionUtils.isEmpty(user_type_code_list)) {
                        item.setUserTypeCode(String.join(",", user_type_code_list));
                    }
                    StringBuilder builder = new StringBuilder();

                    for (KpiMember b : user_type_list) {
                        //设置人员类型
                        SysDictItem userType = Linq.of(emp_types).
                                where(x -> x.getItemValue().equals(b.getMemberCode())).firstOrDefault();
                        if (userType != null) {
                            builder.append(userType.getLabel()).append(",");
                        }
                    }
                    if (builder.length() > 0) {
                        item.setUserType(String.valueOf(builder).substring(0, builder.length() - 1));
                    }
                } else {
                    item.setUserType("");
                }
                List<KpiMember> user_zw_list = Linq.of(memberList).
                        where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP_ZW.getType())
                                && t.getBusiType().equals(busiType)
                                && item.getUserId().equals(t.getHostId())).toList();
                if (!CollectionUtils.isEmpty(user_zw_list)) {
                    KpiMember user_zw = user_zw_list.get(0);
                    item.setDutiesCode(user_zw.getMemberCode());

                    String value = Linq.of(zw_type).where(t -> t.getItemValue().equals(user_zw.getMemberCode())).select(SysDictItem::getLabel).firstOrDefault();
                    item.setDutiesOrigin(value);
                }
                kpiUserAttendanceMapper.updateById(item);
            }
            // 删除同周期的原数据(如果有)

//            attendanceService.remove(new QueryWrapper<Attendance>().eq("cycle", dt));
//            attendanceService.saveBatch(targetList);
            if (busiType.equals("1")) {
                attendanceMapper.delete(new QueryWrapper<Attendance>().eq("cycle", dt));
                if (!targetList.isEmpty()) {
                    for (Attendance t : targetList) {
                        t.setTenantId(tenantId);
                    }
                    List<List<Attendance>> partition = ListUtils.partition(targetList, 1000);
                    partition.forEach(attendanceMapper::insertBatchSomeColumn);
                    //partition.forEach(this::saveBatch);
                }
            }
            if (busiType.equals("2")) {
                attendanceDeptMapper.delete(new QueryWrapper<AttendanceDept>().eq("cycle", dt));
                List<AttendanceDept> targetList_dept = new ArrayList<>();
                if (!targetList.isEmpty()) {
                    for (Attendance t : targetList) {
                        AttendanceDept ad = new AttendanceDept();
                        t.setTenantId(tenantId);
                        BeanUtils.copyProperties(t, ad);
                        targetList_dept.add(ad);
                    }
                    List<List<AttendanceDept>> partition = ListUtils.partition(targetList_dept, 1000);
                    partition.forEach(attendanceDeptMapper::insertBatchSomeColumn);
                    //partition.forEach(this::saveBatch);
                }
            }
            System.out.println("数据移植完成");
            String current = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
            pullCustomFields(dt == null ? current : dt, busiType, tenantId);
        }
        if (!CollectionUtils.isEmpty(rtnList)) {
            LambdaUpdateWrapper<KpiUserAttendance> updateWrapper = new LambdaUpdateWrapper<KpiUserAttendance>()
                    .eq(KpiUserAttendance::getPeriod, Long.valueOf(dt))
                    .eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType)
                    .set(KpiUserAttendance::getIsLocked, rtnList.get(0).getIsLocked());
            kpiUserAttendanceMapper.update(null, updateWrapper);
        }

        //updateBatchById(rtnList)
        return true;
    }

    private void setGrpInfo(KpiUserAttendance item, int daysOfMonth) {
//        JSONObject jsonObject = JSONObject.parseObject(item.getAccountGroup());
//        String labelValue = jsonObject.getString("label");
        String labelValue = item.getAccountGroup();
        if (StringUtil.isBlank(labelValue)) {
            return;
        }
        // 在册系数 逻辑
        if (Objects.equals(item.getRewardIndex(), new BigDecimal(0))) {
            item.setRegisteredRate(new BigDecimal(0));
        } else if (medGroup1.contains(labelValue)) {  // 核算分组如果是医生组、医技组、护理组=当前考勤组所在天数/自然月天数*奖金系数
            item.setRegisteredRate(item.getAttendanceGroupDays().divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64).multiply(item.getRewardIndex()));
        } else if (medGroup2.contains(labelValue)) {  // 核算分组如果是行政组、药剂组=奖金系数
            item.setRegisteredRate(item.getRewardIndex());
        }
    }

    /**
     * 每个月8号获取上月人员考勤数据的任务
     */
    // @XxlJob("importUserAttendanceDataJob")
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void importData(DateDto dto) throws IOException {
        String dt = dto.getPeriod();
        List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, dt));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if (dto.getBusiType().equals("1") && kpiConfig.getUserFlag().equals("Y")) {
                throw new BizException("已锁定无法修改");
            }
            if (dto.getBusiType().equals("2") && kpiConfig.getUserFlagKs().equals("Y")) {
                throw new BizException("已锁定无法修改");
            }
        }
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getStatus, "0").eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiAccountUnit::getBusiType, dto.getBusiType()));
        if ("2".equals(dto.getBusiType())) {
            //剔除虚拟核算单元
            List<Long> virtualKpiAccountUnitIds = kpiAccountUnitService.getBaseMapper().getVirtualIds();
            accountUnitList = Linq.of(accountUnitList).where(x -> !virtualKpiAccountUnitIds.contains(x.getId())).toList();
        }
        List<KpiMember> memberList = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
                .eq(KpiMember::getPeriod, dt)
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType())
                .eq(KpiMember::getMemberType, MemberEnum.USER_DEPT.getType()));
        String lastDt;
        if (StringUtil.isEmpty(dt)) {
            Date now = new Date();
            dt = DateUtil.format(DateUtil.offsetMonth(now, -1), "yyyyMM");
            lastDt = DateUtil.format(DateUtil.offsetMonth(now, -2), "yyyyMM");
        } else {
            DateTime yyyyMM = DateUtil.parse(dt, "yyyyMM");
            lastDt = DateUtil.format(DateUtil.offsetMonth(yyyyMM, -1), "yyyyMM");
        }
        // 查询costUserAttendanceConfigService数据
//        CostUserAttendanceConfig costUserAttendanceConfig = costUserAttendanceConfigService.getByDt(dt);
//        if (Objects.isNull(costUserAttendanceConfig)) {
//            costUserAttendanceConfig = new CostUserAttendanceConfig();
//            costUserAttendanceConfig.setDt(dt);
//            // 默认导入模式
//            costUserAttendanceConfig.setPattern("1");
//            costUserAttendanceConfigService.save(costUserAttendanceConfig);
//        }
//        if (Objects.equals("1", costUserAttendanceConfig.getPattern())) {
//            log.info("导入模式，不走数据中台采集数据");
//            return;
//        }
        int daysOfMonth = DateUtil.parse(dt, "yyyyMM").getLastDayOfMonth();
        // 删除同周期的原数据(如果有)
        remove(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dt).eq(KpiUserAttendance::getBusiType, dto.getBusiType()));
        List<KpiUserAttendance> list = dmoUtil.userAttendanceList2(dt, dto);
        List<KpiUserAttendance> rtnList = new ArrayList<>();// 返回的数据
        List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
        // ThirdAccountUnitQueryDto thirdAccountUnitQueryDto = new ThirdAccountUnitQueryDto();
        // thirdAccountUnitQueryDto.setMatchType("1");
        InnerAllDataVO rsData;
        if (dto.getBusiType().equals("1")) {
            R<InnerAllDataVO> innerAllDataVOR = remoteMappingBaseService.allData(MappingGroupAttributeEnum.KPI.getCode(), "RS_KSHSDY_KPI", SecurityConstants.FROM_IN);
            //log.info("rsData:{}", JSON.toJSONString(innerAllDataVOR));
            //log.info(ksPurpose);
            rsData = innerAllDataVOR.getData();
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        } else {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("dept_cost_account_group").getData();

            R<InnerAllDataVO> innerAllDataVOR = remoteMappingBaseService.allData(MappingGroupAttributeEnum.COST.getCode(), "RS_KQZHSDY_COST", SecurityConstants.FROM_IN);
            //log.info(kqzPurpose);
            //log.info("rsData:{}", JSON.toJSONString(innerAllDataVOR));
            rsData = innerAllDataVOR.getData();
        }
        if (rsData == null) {
            throw new BizException("考勤映射不存在");
        }
        List<KpiAccountUnit> zzs = kpiAccountUnitService.getBaseMapper().selectList(
                new QueryWrapper<KpiAccountUnit>()
                        .eq("del_flag", "0")
                        .eq("status", "0")
                        .eq("busi_type", dto.getBusiType())
                        .eq("name", "中治室")
        );
        Long zhizhishiId;
        if (zzs.size() > 0) {
            zhizhishiId = zzs.get(0).getId();
        } else {
            zhizhishiId = 0L;
        }

        List<KpiUserAttendance> dataLastMonthList = this.list(new QueryWrapper<KpiUserAttendance>()
                .eq("period", lastDt)
                .eq("busi_type", dto.getBusiType()));
        dataLastMonthList = Linq.of(dataLastMonthList)
                .where(r -> !(!ZHONGZHISHI.equals(r.getAttendanceGroup()) && zhizhishiId.equals(r.getAccountUnit()))).toList();
        for (KpiUserAttendance item : list) {
            if (item.getAccountUnit() == null && StringUtil.isNotBlank(item.getDeptName())) {
                KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> item.getDeptName().equals(t.getName())).firstOrDefault();
                if (kpiAccountUnit != null) {
                    item.setAccountUnit(kpiAccountUnit.getId());
                }
            }
            item.setBusiType(dto.getBusiType());
            // 1.尝试继承上月科室单元信息
            KpiUserAttendance dataLastMonth = Linq.of(dataLastMonthList)
                    .firstOrDefault(t -> StringUtil.isNotBlank(t.getAttendanceGroup()) && t.getAttendanceGroup().equals(item.getAttendanceGroup())
                            && t.getUserId() != null && t.getUserId().equals(item.getUserId()));
            if (dataLastMonth != null && dataLastMonth.getAccountUnit() != null) {
                item.setAccountUnit(dataLastMonth.getAccountUnit());
                item.setIsEdited("1");
                Long unitStr = dataLastMonth.getAccountUnit();
//                JSONArray accountUnitJsonArray = JSONObject.parseArray(unitStr);
//                JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
//                String id = firstObject.getString("id");
                //CostAccountUnit costAccountUnitInfo = costAccountUnitService.getById(id);
                KpiAccountUnit kpiAccountUnitInfo = Linq.of(accountUnitList).where(t -> t.getId().equals(unitStr)).firstOrDefault();
                // KpiCategory category = Linq.of(categoryList).where(t -> t.getCategoryCode().equals(kpiAccountUnitInfo.getCategoryCode())).firstOrDefault();
                //item.setAccountGroup(costAccountUnitInfo.getAccountGroupCode());
                //List<SysDictItem> pmcKpiCalculateGrouping = remoteDictService.getDictByType("PMC_kpi_calculate_grouping").getData();
                if (kpiAccountUnitInfo != null) {
                    String account_groupName = "";
                    if (dto.getBusiType().equals("1")) {
                        account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnitInfo.getCategoryCode())
                        ).select(SysDictItem::getLabel).firstOrDefault();
                    } else {
                        account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnitInfo.getAccountGroup()))
                                .select(SysDictItem::getLabel).firstOrDefault();
                    }
                    item.setAccountGroup(account_groupName);
                }
                //setGrpInfo(item, daysOfMonth);
            } else {
                // 2-1.当一个人事科室对应多个核算单元时，第一次配置时，科室单元空着，让用户在“变动人员处理”中确认该人员对应的科室单元，后面默认继承上次的科室单元
                Long accountUnitInfo = item.getAccountUnit();
//                JSONArray accountUnitJsonArray = JSONObject.parseArray(accountUnitInfo);
//                JSONObject firstObject = accountUnitJsonArray.getJSONObject(0);
//                String accountUnit = firstObject.getString("name");
                List<MappingBase> thirdAccountUnits = new ArrayList<>();
                KpiAccountUnit kpiAccountUnit1 = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
//                if (kpiAccountUnit1 != null) {
//                    String accountUnit = kpiAccountUnit1.getName();
//                    thirdAccountUnits = remoteThirdAccountUnitService.getByName(accountUnit).getData();
//                    // 人事系统下，所以只有一个核算单元
//                    if (dto.getBusiType().equals("1")) {
//                        thirdAccountUnits = thirdAccountUnits.stream().filter(x -> x.getThirdId() == HRThirdId && x.getUserSystem().equals("kpi")).collect(Collectors.toList());
//                    } else if (dto.getBusiType().equals("2")) {
//                        thirdAccountUnits = thirdAccountUnits.stream().filter(x -> x.getThirdId() == HRThirdId && x.getUserSystem().equals("cost")).collect(Collectors.toList());
//                    }
//                }
                if (kpiAccountUnit1 == null) {
                    if (item.getAttendanceGroup() != null) {
                        thirdAccountUnits = rsData.autoFilter(null, item.getAttendanceGroup(), "1", "0");
                    }
                }
                String unitIds = "";
                if (!CollectionUtils.isEmpty(thirdAccountUnits)) {
                    unitIds = thirdAccountUnits.get(0).getBaseRelSysIds();
                }
                if (StringUtils.isEmpty(unitIds)) {
                    item.setAccountUnit(null);
                } else if (unitIds.contains(",") || "柔性引进".equals(item.getJobNature()) || "退休返聘".equals(item.getJobNature())) {
                    // 2-2. 对应多条数据, 本月新增了柔性引进和退休返聘人员时，这些人的科室单元空着，让用户在“变动人员处理”中确认该人员对应的科室单元，后面默认继承上次的科室单元
                    item.setAccountUnit(null);
                } else {
                    try {
                        AccountUnitDto accountUnitIdAndNameDto = new AccountUnitDto();
                        String id = thirdAccountUnits.get(0).getBaseRelSysIds();
                        KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().toString().equals(id)).firstOrDefault();
                        //CostAccountUnit costAccountUnit = costAccountUnitService.getById(id);
                        accountUnitIdAndNameDto.setId(id);
                        accountUnitIdAndNameDto.setName(kpiAccountUnit.getName());
                        List<AccountUnitDto> accountUnitIdAndNameDtoList = new ArrayList<>();
                        accountUnitIdAndNameDtoList.add(accountUnitIdAndNameDto);
                        item.setAccountUnit(Long.valueOf(id));
                        item.setAccountUnitName(kpiAccountUnit.getName());
                        // KpiCategory category = Linq.of(categoryList).where(t -> t.getCategoryCode().equals(kpiAccountUnit.getCategoryCode())).firstOrDefault();
                        //List<SysDictItem> pmcKpiCalculateGrouping = remoteDictService.getDictByType("PMC_kpi_calculate_grouping").getData();
                        String account_groupName = "";
                        if (dto.getBusiType().equals("1")) {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                            ).select(SysDictItem::getLabel).firstOrDefault();
                        } else {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                    .select(SysDictItem::getLabel).firstOrDefault();
                        }
                        item.setAccountGroup(account_groupName);
//                        item.setAccountUnit(JSON.toJSONString(accountUnitIdAndNameDtoList));
//                        item.setAccountGroup(costAccountUnit.getAccountGroupCode());
                        //setGrpInfo(item, daysOfMonth);
                    } catch (Exception ignored) {
                    }
                }
            }
            // 3.解析中治室字段 等数据中台返参
            List<CustomFieldVO> customFieldVOS = getCustomFieldsList(item);
            for (CustomFieldVO customFieldVO : customFieldVOS) {
                if (customFieldVO.getName().equals(ZHONGZHISHI)) {
                    BigDecimal day = new BigDecimal(String.valueOf(customFieldVO.getNum()));
                    if (day.compareTo(new BigDecimal(0)) < 1) {
                        continue;
                    }
                    KpiUserAttendance extra = new KpiUserAttendance();
                    BeanUtils.copyProperties(item, extra);
                    List<MappingBase> mappingBases = rsData.getMappingBaseList().stream()
                            .filter(e -> Objects.equals(e.getBaseRelSysNames(), ZHONGZHISHI) && Objects.equals("0", e.getIgnoreStatus())).collect(Collectors.toList());

                    //thirdAccountUnits = thirdAccountUnits.stream().filter(x -> x.getThirdId() == HRThirdId).collect(Collectors.toList());
                    List<AccountUnitDto> accountUnitDtos = new ArrayList<>();
                    AccountUnitDto accountUnitDto = new AccountUnitDto();
                    String id = mappingBases.get(0).getBaseRelSysIds();
                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().toString().equals(id)).firstOrDefault();
                    // CostAccountUnit costAccountUnit = costAccountUnitService.getById(id);
                    accountUnitDto.setId(id);
                    if (kpiAccountUnit != null) {
                        accountUnitDto.setName(kpiAccountUnit.getName());
                    }
                    accountUnitDtos.add(accountUnitDto);
                    String accountUnitString = JSON.toJSONString(accountUnitDtos);
                    //extra.setAccountUnit(accountUnitString);
                    extra.setAccountUnit(Long.valueOf(id));
                    if (kpiAccountUnit != null) {
                        extra.setAccountUnitName(kpiAccountUnit.getName());
                    }
                    extra.setAttendanceGroupDays(day);
                    extra.setSourceType("3");// 用于计算公式识别，跳过计算逻辑
                    extra.setAttendDays(day);
                    extra.setOneKpiAttendDays(day);
//                    CostAccountUnit costAccountUnitInfo = costAccountUnitService.getById(id);
//                    extra.setAccountGroup(costAccountUnitInfo.getAccountGroupCode());
                    //KpiCategory category = Linq.of(categoryList).where(t -> t.getCategoryCode().equals(kpiAccountUnit.getCategoryCode())).firstOrDefault();
                    //List<SysDictItem> pmcKpiCalculateGrouping = remoteDictService.getDictByType("PMC_kpi_calculate_grouping").getData();
                    if (kpiAccountUnit != null) {
                        String account_groupName = "";
                        if (dto.getBusiType().equals("1")) {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                            ).select(SysDictItem::getLabel).firstOrDefault();
                        } else {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                    .select(SysDictItem::getLabel).firstOrDefault();
                        }
                        extra.setAccountGroup(account_groupName);
                    }
                    //setGrpInfo(extra, daysOfMonth);
                    rtnList.add(extra);
                } else {
                    item.setTreatRoomDays(String.valueOf(new BigDecimal(0)));
                }
            }
        }
        rtnList.addAll(list);
        if (!rtnList.isEmpty()) {
            List<SysDictItem> finalPmcKpiCalculateGrouping = pmcKpiCalculateGrouping;
            List<KpiAccountUnit> finalAccountUnitList = accountUnitList;
            rtnList.forEach(t -> {
                if (t.getAccountUnit() == null) {
                    List<KpiMember> memberList1 = Linq.of(memberList).where(x ->
                            x.getHostId().equals(t.getUserId()) &&
                                    x.getHostCode().equals(t.getAttendanceGroup())).toList();
                    if (!CollectionUtils.isEmpty(memberList1)) {
                        t.setAccountUnit(memberList1.get(0).getMemberId());
                        KpiAccountUnit kpiAccountUnit = Linq.of(finalAccountUnitList).where(m -> m.getId().equals(memberList1.get(0).getMemberId())).firstOrDefault();
                        if (kpiAccountUnit != null) {
                            t.setAccountUnitName(kpiAccountUnit.getName());
                            String account_groupName = "";
                            if (dto.getBusiType().equals("1")) {
                                account_groupName = Linq.of(finalPmcKpiCalculateGrouping).where(m -> m.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                                ).select(SysDictItem::getLabel).firstOrDefault();
                            } else {
                                account_groupName = Linq.of(finalPmcKpiCalculateGrouping).where(m -> m.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                        .select(SysDictItem::getLabel).firstOrDefault();
                            }
                            t.setAccountGroup(account_groupName);
                        }
                        t.setIsEdited("1");
                    } else {
                        if (StringUtil.isNotBlank(t.getDeptName())) {
                            KpiAccountUnit kpiAccountUnit = Linq.of(finalAccountUnitList).where(m -> t.getDeptName().equals(m.getName())).firstOrDefault();
                            if (kpiAccountUnit != null) {
                                t.setAccountUnit(kpiAccountUnit.getId());
                                t.setAccountUnitName(kpiAccountUnit.getName());
                                String account_groupName = "";
                                if (dto.getBusiType().equals("1")) {
                                    account_groupName = Linq.of(finalPmcKpiCalculateGrouping).where(m -> m.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                                    ).select(SysDictItem::getLabel).firstOrDefault();
                                } else {
                                    account_groupName = Linq.of(finalPmcKpiCalculateGrouping).where(m -> m.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                            .select(SysDictItem::getLabel).firstOrDefault();
                                }
                                t.setAccountGroup(account_groupName);
                            }
                        }
                    }
                }
                t.setTenantId(SecurityUtils.getUser().getTenantId());
                t.setDelFlag("0");
                t.setIsLocked("0");

                if (t.getIsEdited() == null) {
                    t.setIsEdited("0");
                }
                if (t.getIsEdited().equals("0") && (t.getAccountUnit() == null || t.getAccountUnit().equals(0L))) {
                    t.setAccountUnit(null);
                }
                if (t.getSourceType() == null) {
                    t.setSourceType("2");
                }
            });
            List<List<KpiUserAttendance>> partition = ListUtils.partition(rtnList, 1000);
            partition.forEach(x -> {
                kpiUserAttendanceMapper.insertBatchSomeColumn(x);
            });
            //partition.forEach(this::saveBatch);
        }
        //saveBatch(rtnList);
    }

    @Override
    public R validateData(String dt, String busiType) {
        List<DimMonthEmpIncome> outpatientFeeList = dmoUtil.outpatientFeeList(dt);
        List<KpiUserAttendance> list = list(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dt)
                .eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType));
        // 找出人员id不在list中的数据，封装
        List<String> idList = list.stream().map(KpiUserAttendance::getEmpId).collect(Collectors.toList());
        Map<String, String> map = new HashMap<>();
        for (DimMonthEmpIncome item : outpatientFeeList) {
            if (!idList.contains(item.getEmpCode())) {
                map.put(item.getEmpCode(), item.getEmpName());
            }
        }
        return R.ok(map);
    }

    @Override
    public List<ValidateJobNumberVO> validateJobNumber(String dt, String busiType) {
        List<SysUser> sysUsers = remoteUserService.userListByCustom(new GetUserListCustomDTO(), SecurityConstants.FROM_IN).getData();
        List<String> jobNumberList = sysUsers.stream().map(SysUser::getJobNumber).filter(Objects::nonNull).collect(Collectors.toList());
        List<KpiUserAttendance> costUserAttendances = list(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dt)
                .eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType));
        List<ValidateJobNumberVO> results = new ArrayList<>();
        for (KpiUserAttendance costUserAttendance : costUserAttendances) {
            if (!jobNumberList.contains(costUserAttendance.getEmpId())) {
                ValidateJobNumberVO validateJobNumberVO = new ValidateJobNumberVO();
                validateJobNumberVO.setEmpId(costUserAttendance.getEmpId());
                validateJobNumberVO.setEmpName(costUserAttendance.getEmpName());
                results.add(validateJobNumberVO);
            }
        }
        return results;
    }

    private List<KpiUserAttendance> renderData(List<KpiUserAttendance> costUserAttendanceList, String busiType) {
        String date = costUserAttendanceList.get(0).getPeriod().toString();
        LocalDate localDate = LocalDate.of(Integer.parseInt(date.substring(0, 4)), Integer.parseInt(date.substring(4, 6)), 1);
        int daysOfMonth = localDate.lengthOfMonth();
        List<KpiAccountUnit> accountUnitList = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(busiType), KpiAccountUnit::getBusiType, busiType));
        List<FirstDistributionAttendanceFormula> firstDistributionAttendanceFormulas = firstDistributionAttendanceFormulaMapper
                .selectList(new LambdaQueryWrapper<FirstDistributionAttendanceFormula>()
                        .eq(FirstDistributionAttendanceFormula::getDt, date)
                        .eq(FirstDistributionAttendanceFormula::getBusiType, busiType));
        List<FirstDistributionAccountFormulaParam> firstDistributionAccountFormulaParams = firstDistributionAccountFormulaParamMapper.selectList(null);
        List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields = costUserAttendanceCustomFieldsMapper.selectList(
                new LambdaQueryWrapper<CostUserAttendanceCustomFields>()
                        .eq(CostUserAttendanceCustomFields::getDt, date)
        );
        List<SysDictItem> pmcKpiCalculateGrouping = new ArrayList<>();
        if (busiType.equals("1")) {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        } else {
            pmcKpiCalculateGrouping = remoteDictService.getDictByType("department_grouping").getData();
        }
        //启用目录
        List<KpiCategory> categoryList = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>().
                eq(KpiCategory::getStatus, 0).eq(KpiCategory::getDelFlag, "0"));
        for (KpiUserAttendance kpiUserAttendance : costUserAttendanceList) {
            // 自定义字段数值读取
            String customFields = kpiUserAttendance.getCustomFields();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                List<CustomFieldVO> customFieldVOS = new ArrayList<>();
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
                kpiUserAttendance.setCustomFieldList(customFieldVOS);
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            // 科室单元信息
            Long accountUnitInfo = kpiUserAttendance.getAccountUnit();
            ObjectMapper mapper = new ObjectMapper();
            AccountUnitDto accountUnitDto = new AccountUnitDto();
            try {
//                JsonNode rootNode = mapper.readTree(accountUnitInfo);
//                JsonNode jsonObject = rootNode.get(0); // 获取第一个对象
//                String id = jsonObject.get("id").asText();
//                String name = jsonObject.get("name").asText();
                KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(accountUnitInfo)).firstOrDefault();
//                accountUnitDto.setName(kpiAccountUnit.getName());
//                accountUnitDto.setId(String.valueOf(accountUnitInfo));
                try {
                    //CostAccountUnit unitInfo = costAccountUnitService.getById(id);
                    if (kpiAccountUnit != null) {
                        accountUnitDto.setName(kpiAccountUnit.getName());
                        accountUnitDto.setId(String.valueOf(accountUnitInfo));
                        String actGrp = kpiAccountUnit.getCategoryCode();
                        //KpiCategory category = Linq.of(categoryList).where(t -> t.getCategoryCode().equals(actGrp)).firstOrDefault();
                        String account_groupName = "";
                        if (busiType.equals("1")) {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getCategoryCode())
                            ).select(SysDictItem::getLabel).firstOrDefault();
                        } else {
                            account_groupName = Linq.of(pmcKpiCalculateGrouping).where(t -> t.getItemValue().equals(kpiAccountUnit.getAccountGroup()))
                                    .select(SysDictItem::getLabel).firstOrDefault();
                        }
                        if (account_groupName != null) {
                            kpiUserAttendance.setAccountGroup(account_groupName);
                        }
                    }
                } catch (Exception ignored) {

                }
            } catch (Exception ignored) {
            }
            if (!"3".equals(kpiUserAttendance.getSourceType())) {
                kpiUserAttendance.setAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
                        .calculateAttendDays2(FORMULA_ID_SJCQTS, kpiUserAttendance
                                , accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
                // 一次性绩效出勤天数 - 公式计算
                kpiUserAttendance.setOneKpiAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
                        .calculateAttendDays2(FORMULA_ID_YCXJXCQTS, kpiUserAttendance,
                                accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
            }
//            if ("2".equals(kpiUserAttendance.getSourceType())) {
////            实际出勤天数 - 动态公式计算
//                if (kpiUserAttendance.getAccountUnit() != null) {
//                    KpiAccountUnit kpiAccountUnit = Linq.of(accountUnitList).where(t -> t.getId().equals(kpiUserAttendance.getAccountUnit())).firstOrDefault();
//                    if (kpiAccountUnit != null) {
//                        if (!"中治室".equals(kpiUserAttendance.getAttendanceGroup()) && kpiAccountUnit.getName().contains("中治室") && "1".equals(kpiUserAttendance.getIsEdited())) {
//                            System.out.println("中治室,跳过计算");
//                        } else {
//                            kpiUserAttendance.setAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
//                                    .calculateAttendDays2(FORMULA_ID_SJCQTS, kpiUserAttendance
//                                            , accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
//                            // 一次性绩效出勤天数 - 公式计算
//                            kpiUserAttendance.setOneKpiAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
//                                    .calculateAttendDays2(FORMULA_ID_YCXJXCQTS, kpiUserAttendance
//                                            , accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
//                        }
//                    }
//                } else {
//                    kpiUserAttendance.setAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
//                            .calculateAttendDays2(FORMULA_ID_SJCQTS, kpiUserAttendance
//                                    , accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
//                    // 一次性绩效出勤天数 - 公式计算
//                    kpiUserAttendance.setOneKpiAttendDays(new BigDecimal(firstDistributionAttendanceFormulaService
//                            .calculateAttendDays2(FORMULA_ID_YCXJXCQTS, kpiUserAttendance,
//                                    accountUnitList, firstDistributionAttendanceFormulas, firstDistributionAccountFormulaParams, costUserAttendanceCustomFields)));
//                }
//            }
            // 出勤系数
            if (kpiUserAttendance.getReward().equals("1")) {
                BigDecimal attendDays = kpiUserAttendance.getAttendDays();
                BigDecimal attendRate = attendDays.divide(BigDecimal.valueOf(daysOfMonth), 6, RoundingMode.HALF_UP);
                kpiUserAttendance.setAttendRate(attendRate);
            } else {
                kpiUserAttendance.setAttendRate(new BigDecimal(0));
            }
            // 在册系数 逻辑
            if (customFields.contains("中治室")) {
                kpiUserAttendance.setRegisteredRate(kpiUserAttendance.getAttendRate());
            } else if (Objects.equals(kpiUserAttendance.getRewardIndex(), new BigDecimal(0))) {
                kpiUserAttendance.setRegisteredRate(new BigDecimal(0));
            } else {
                try {
                    kpiUserAttendance.setRegisteredRate(kpiUserAttendance.getAttendanceGroupDays()
                            .divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64));
//                    String labelValue = kpiUserAttendance.getAccountGroup();
//                    if ((kpiUserAttendance.getAccountUnit() != null && kpiUserAttendance.getAccountUnit() == 0L)
//                            || medGroup1.contains(labelValue)) {  // 核算分组如果是医生组、医技组、护理组=当前考勤组所在天数/自然月天数
//                        kpiUserAttendance.setRegisteredRate(kpiUserAttendance.getAttendanceGroupDays()
//                                .divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64));
//                    } else if (medGroup2.contains(labelValue)) {  // 核算分组如果是行政组、药剂组=当前考勤组所在天数/自然月天数*奖金系数
//                        kpiUserAttendance.setRegisteredRate(kpiUserAttendance.getAttendanceGroupDays()
//                                .divide(new BigDecimal(daysOfMonth), MathContext.DECIMAL64).multiply(kpiUserAttendance.getRewardIndex()));
//                    }
                } catch (Exception ignored) {
                }
            }
            // 一次性绩效系数
            BigDecimal oneKpiAttendDays = kpiUserAttendance.getOneKpiAttendDays();
            BigDecimal oneKpiAttendRate = oneKpiAttendDays.divide(BigDecimal.valueOf(daysOfMonth), 6, RoundingMode.HALF_UP);
            kpiUserAttendance.setOneKpiAttendRate(oneKpiAttendRate);

            // 出勤次数 ：需要算出勤次数的人员为叶海、洪善贻、王晖+工作性质是柔性引进的人员，计算规则：天数/0.5（半天算一次），其他人都展示0
            if (kpiUserAttendance.getAttendCount() == null || kpiUserAttendance.getAttendCount() == 0) {
                if (empList.contains(kpiUserAttendance.getEmpName()) && kpiUserAttendance.getJobNature().equals("柔性引进")) {
                    BigDecimal attendDays = kpiUserAttendance.getAttendDays();
                    BigDecimal attendTimes = attendDays.divide(new BigDecimal("0.5"), 6, RoundingMode.HALF_UP);
                    kpiUserAttendance.setAttendCount(Long.valueOf(attendTimes.toString()));
                } else {
                    kpiUserAttendance.setAttendCount(0L);
                }
            }

            List<AccountUnitDto> accountUnits = new ArrayList<>();
            accountUnits.add(accountUnitDto);
            kpiUserAttendance.setAccountUnits(accountUnits);
        }
        return costUserAttendanceList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void calculateData(String dt, String busiType) {
        List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, dt));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if (busiType.equals("1") && kpiConfig.getUserFlag().equals("Y")) {
                throw new BizException("已锁定无法计算");
            }
            if (busiType.equals("2") && kpiConfig.getUserFlagKs().equals("Y")) {
                throw new BizException("已锁定无法计算");
            }
        }
        System.out.println("开始计算...");
        // 查询costUserAttendanceConfigService数据
        List<KpiUserAttendance> costUserAttendanceList = list(new LambdaQueryWrapper<KpiUserAttendance>()
                .eq(KpiUserAttendance::getPeriod, dt).eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType));
        //.eq(KpiUserAttendance::getSourceType, "2"));

//        if (Objects.equals("1", costUserAttendanceList.get(0).getSourceType())) {
//            log.info("导入模式，不用计算");
//            return;
//        }
//        LambdaQueryWrapper<KpiUserAttendance> qr = new LambdaQueryWrapper<>();
//        qr.eq(KpiUserAttendance::getPeriod, dt);
//        List<KpiUserAttendance> costUserAttendanceList = list(qr);
        if (costUserAttendanceList.isEmpty()) {
            return;
        }
        List<KpiUserAttendance> rtnList = renderData(costUserAttendanceList, busiType);
        updateBatchById(rtnList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void pullCustomFields(String dt, String busiType, Long tenantId) {
        // 推送前清空数据
        kiUserAttendanceCustomService.remove(Wrappers.<KpiUserAttendanceCustom>lambdaQuery()
                .eq(KpiUserAttendanceCustom::getPeriod, dt).eq(KpiUserAttendanceCustom::getBusiType, busiType));
        List<KpiUserAttendance> list = list(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dt)
                .eq(StringUtil.isNotBlank(busiType), KpiUserAttendance::getBusiType, busiType));
        List<KpiUserAttendanceCustom> rtnList = new ArrayList<>();
        for (KpiUserAttendance item : list) {
            KpiUserAttendanceCustom customFieldData = new KpiUserAttendanceCustom();

            customFieldData.setPeriod(Long.valueOf(dt));
            customFieldData.setEmpName(item.getEmpName());
            customFieldData.setEmpId(item.getEmpId());
            customFieldData.setUserAttendanceId(item.getId());

            // 自定义字段数值读取
            String customFields = item.getCustomFields();
            List<CustomFieldVO> customFieldVOS = new ArrayList<>();
            try {
                String inputList = "[" + customFields.replaceAll("}(?=,)", "},") + "]";
                JSONArray jsonArray = JSONArray.parseArray(inputList);
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String name = jsonObject.getString("name");
                    String num = jsonObject.getString("num");
                    CustomFieldVO customFieldVO = new CustomFieldVO(id, name, num);
                    customFieldVOS.add(customFieldVO);
                }
            } catch (Exception e) {
                log.error("自定义字段解析异常", e);
            }
            for (CustomFieldVO customField : customFieldVOS) {
                KpiUserAttendanceCustom dataEntry = new KpiUserAttendanceCustom();
                BeanUtils.copyProperties(customFieldData, dataEntry);
                dataEntry.setDelFlag("0");
                dataEntry.setBusiType(busiType);
                dataEntry.setName(customField.getName());
                dataEntry.setColumnId(StringUtils.isNotEmpty(customField.getId()) ? Long.parseLong(customField.getId()) : 0);
                dataEntry.setValue(BigDecimal.valueOf(StringUtils.isNotEmpty(customField.getNum()) ? Double.parseDouble(customField.getNum()) : 0));
                rtnList.add(dataEntry);
            }
        }
        if (!rtnList.isEmpty()) {
            rtnList.forEach(t -> {
                t.setTenantId(tenantId);
            });
            List<List<KpiUserAttendanceCustom>> partition = ListUtils.partition(rtnList, 1000);
            partition.forEach(x -> {
                kpiUserAttendanceCustomMapper.insertBatchSomeColumn(x);
            });
            //partition.forEach(this::saveBatch);
        }
        //kiUserAttendanceCustomService.saveOrUpdateBatch(rtnList);
    }

    public void AddUser(KpiAccountUserAddDto dto) {
        KpiUserAttendance kpiUserAttendance = kpiUserAttendanceMapper.selectOne(new LambdaQueryWrapper<KpiUserAttendance>()
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiUserAttendance::getBusiType, dto.getBusiType())
                .eq(KpiUserAttendance::getUserId, dto.getUserId()).orderByDesc(KpiUserAttendance::getPeriod)
                .last("limit 1"));
        List<KpiMember> list = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType())
                .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType()).or().eq(KpiMember::getMemberType, MemberEnum.EMP_TYPE.getType()));
        KpiMember member = new KpiMember();
        member.setHostCode(dto.getCategoryCode());
        if (StringUtil.isNotBlank(dto.getBusiType())) {
            member.setBusiType(dto.getBusiType());
        }
        member.setMemberId(dto.getUserId());
        member.setMemberType(MemberEnum.ROLE_EMP.getType());
        member.setCreatedDate(new Date());
        member.setPeriod(0L);
        KpiMember kpiMember = Linq.of(list).where(t -> t.getMemberType().equals(MemberEnum.ROLE_EMP.getType())
                && dto.getCategoryCode().equals(t.getHostCode()) && dto.getUserId().equals(t.getMemberId())).firstOrDefault();
        if (kpiMember == null) {
            kpiMemberService.save(member);
        }
        if (!StringUtil.isEmpty(dto.getUserTypeCode()) || !StringUtil.isEmpty(dto.getGjks()) || !StringUtil.isEmpty(dto.getZw())) {
            KpiHsUserEditDto input = new KpiHsUserEditDto();
            input.setCategoryCode(dto.getCategoryCode());
            input.setUserId(dto.getUserId());
            if (!StringUtil.isEmpty(dto.getUserTypeCode())) {
                input.setUserTypeCode(dto.getUserTypeCode());

            }
            if (!StringUtil.isEmpty(dto.getGjks())) {
                input.setGjks(dto.getGjks());
                //editUser(input,"0");
            }
            if (!StringUtil.isEmpty(dto.getZw())) {
                input.setZw(dto.getZw());
                //editUser(input,"0");
            }
            editUser(input, "0");
        }
//        //给考勤表更新usertype 人员类型 一人可能多个
//        List<KpiMember> user_type_list = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
//                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType())
//                .eq(KpiMember::getMemberType, MemberEnum.EMP_TYPE.getType()).eq(KpiMember::getHostId, dto.getUserId()));
//        List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
//        if (!CollectionUtils.isEmpty(user_type_list)) {
//            StringBuilder builder = new StringBuilder();
//            for (KpiMember b : user_type_list) {
//                //设置人员类型
//                SysDictItem userType = Linq.of(emp_types).
//                        where(x -> x.getItemValue().equals(b.getMemberCode())).firstOrDefault();
//                builder.append(userType.getLabel()).append(",");
//            }
//            if (StringUtils.isNotEmpty(builder)) {
//                if (kpiUserAttendance != null) {
//                    kpiUserAttendance.setUserType(String.valueOf(builder).substring(0, builder.length() - 1));
//                    updateById(kpiUserAttendance);
//                }
//            }
//        }
    }

    /**
     * 编辑核算人员 只能改人员类型
     * type 区分1编辑 0新增
     *
     * @param dto
     */
    public void editUser(KpiHsUserEditDto dto, String type) {
        if (type.equals("1")) {
            kpiMemberService.remove(new QueryWrapper<KpiMember>()
                    .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                    .eq("member_type", MemberEnum.EMP_TYPE.getType())
                    .eq("host_id", dto.getUserId()));
        }
        KpiMember user_type = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
                .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                .eq("member_type", MemberEnum.EMP_TYPE.getType())
                .eq("host_id", dto.getUserId()));
        if (StringUtil.isNotBlank(dto.getUserTypeCode())) {
            if (user_type != null) {
                user_type.setMemberCode(dto.getUserTypeCode());
                kpiMemberService.updateById(user_type);
            } else {
                KpiMember member2 = new KpiMember();
                member2.setMemberCode(dto.getUserTypeCode());
                member2.setHostId(dto.getUserId());
                if (StringUtil.isNotBlank(dto.getBusiType())) {
                    member2.setBusiType(dto.getBusiType());
                }
                member2.setMemberType(MemberEnum.EMP_TYPE.getType());
                member2.setCreatedDate(new Date());
                member2.setPeriod(0L);
                kpiMemberService.save(member2);
            }
//            //给考勤表更新usertype 人员类型 一人可能多个
//            List<KpiMember> user_type_list = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
//                    .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType())
//                    .eq(KpiMember::getMemberType, MemberEnum.EMP_TYPE.getType()).eq(KpiMember::getHostId, dto.getUserId()));
//            KpiUserAttendance kpiUserAttendance = kpiUserAttendanceMapper.selectOne(new LambdaQueryWrapper<KpiUserAttendance>()
//                    .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiUserAttendance::getBusiType, dto.getBusiType())
//                    .eq(KpiUserAttendance::getUserId, dto.getUserId()).orderByDesc(KpiUserAttendance::getPeriod)
//                    .last("limit 1"));
//            List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
//            if (!CollectionUtils.isEmpty(user_type_list)) {
//                StringBuilder builder = new StringBuilder();
//                for (KpiMember b : user_type_list) {
//                    //设置人员类型
//                    SysDictItem userType = Linq.of(emp_types).
//                            where(x -> x.getItemValue().equals(b.getMemberCode())).firstOrDefault();
//                    builder.append(userType.getLabel()).append(",");
//                }
//                if (StringUtils.isNotEmpty(builder)) {
//                    if (kpiUserAttendance != null) {
//                        kpiUserAttendance.setUserType(String.valueOf(builder).substring(0, builder.length() - 1));
//                        updateById(kpiUserAttendance);
//                    }
//                }
//            }
        }
        if (type.equals("1")) {
            kpiMemberService.remove(new QueryWrapper<KpiMember>()
                    .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                    .eq("member_type", MemberEnum.ROLE_EMP_GROUP.getType())
                    .eq("host_id", dto.getUserId())
                    .eq("host_code", dto.getCategoryCode()));
        }
        KpiMember user_account = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
                .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                .eq("member_type", MemberEnum.ROLE_EMP_GROUP.getType())
                .eq("host_id", dto.getUserId())
                .eq("host_code", dto.getCategoryCode()));
        if (StringUtil.isNotBlank(dto.getGjks())) {
            if (user_account != null) {
                user_account.setMemberId(Long.valueOf(dto.getGjks()));
                kpiMemberService.updateById(user_account);
            } else {
                KpiMember member2 = new KpiMember();
                if (StringUtil.isEmpty(dto.getGjks())) {
                    return;
                }
                member2.setMemberId(Long.valueOf(dto.getGjks()));
                member2.setHostId(dto.getUserId());
                member2.setHostCode(dto.getCategoryCode());
                if (StringUtil.isNotBlank(dto.getBusiType())) {
                    member2.setBusiType(dto.getBusiType());
                }
                member2.setMemberType(MemberEnum.ROLE_EMP_GROUP.getType());
                member2.setCreatedDate(new Date());
                member2.setPeriod(0L);
                kpiMemberService.save(member2);
            }
        }
        if (type.equals("1")) {
            kpiMemberService.remove(new QueryWrapper<KpiMember>()
                    .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                    .eq("member_type", MemberEnum.ROLE_EMP_ZW.getType())
                    .eq("host_id", dto.getUserId()));
        }
        KpiMember user_zw = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
                .eq(StringUtil.isNotBlank(dto.getBusiType()), "busi_type", dto.getBusiType())
                .eq("member_type", MemberEnum.ROLE_EMP_ZW.getType())
                .eq("host_id", dto.getUserId()));
        if (StringUtil.isNotBlank(dto.getZw())) {
            if (user_zw != null) {
                user_zw.setMemberCode(dto.getZw());
                kpiMemberService.updateById(user_zw);
            } else {
                KpiMember member2 = new KpiMember();
                if (StringUtil.isEmpty(dto.getZw())) {
                    return;
                }
                member2.setMemberCode(dto.getZw());
                member2.setHostId(dto.getUserId());
                //member2.setHostCode(dto.getCategoryCode());
                if (StringUtil.isNotBlank(dto.getBusiType())) {
                    member2.setBusiType(dto.getBusiType());
                }
                member2.setMemberType(MemberEnum.ROLE_EMP_ZW.getType());
                member2.setCreatedDate(new Date());
                member2.setPeriod(0L);
                kpiMemberService.save(member2);
            }
        }
    }

    public void delUser(Long id) {
        KpiMember byId = kpiMemberService.getById(id);
        //删除了ROLE_EMP关联
        kpiMemberService.removeById(id);
//        //删除EMP_TYPE关联
//        KpiMember user_type = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
//                .eq("member_type", MemberEnum.EMP_TYPE.getType())
//                .eq("host_id", byId.getMemberId()));
//        if (user_type != null) {
//            kpiMemberService.removeById(user_type.getId());
//        }
        //删除ROLE_EMP_GROUP关联
        KpiMember user_account = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
                .eq("member_type", MemberEnum.ROLE_EMP_GROUP.getType())
                .eq("host_id", byId.getMemberId())
                .eq("host_code", byId.getHostCode()));
        if (user_account != null) {
            kpiMemberService.removeById(user_account.getId());
        }
//        //删除ROLE_EMP_ZW关联
//        KpiMember user_zw = kpiMemberService.getOne(new QueryWrapper<KpiMember>()
//                .eq("member_type", MemberEnum.ROLE_EMP_ZW.getType())
//                .eq("host_id", byId.getMemberId())
//                .eq("host_code", byId.getHostCode()));
//        if (user_zw != null) {
//            kpiMemberService.removeById(user_zw.getId());
//        }
    }

    @Override
    public IPage<KpiAccountUserDto> hs_page(KpiAccountUseSearchDto queryDto) {
        Page<KpiAccountUserDto> kpiAccountUserPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        IPage<KpiAccountUserDto> kpiAccountUserDtoIPage = kpiUserAttendanceMapper.listByQueryDto2(kpiAccountUserPage, queryDto);
        List<KpiUserAttendance> locked_list = kpiUserAttendanceMapper.findList(queryDto);
        List<SysDictItem> empType = remoteDictService.getDictByType("imputation_person_type").getData();
        //List<SysDictItem> empType = remoteDictService.getDictByType("user_type").getData();
        for (KpiAccountUserDto a : kpiAccountUserDtoIPage.getRecords()) {
            String account_unit_name = Linq.of(locked_list).where(t -> t.getEmpId().equals(a.getEmpId()))
                    .select(KpiUserAttendance::getName).firstOrDefault();
            if (StringUtils.isNotBlank(a.getUserTypeCode())) {
                String value = Linq.of(empType).where(t -> t.getItemValue().equals(a.getUserTypeCode())).select(SysDictItem::getLabel).firstOrDefault();
                a.setUserType(value);
            }
            if (StringUtils.isNotEmpty(account_unit_name)) {
                a.setAccountUnitName(account_unit_name);
            }
            if (StringUtils.isNotEmpty(a.getGjks())) {
                KpiAccountUnit byId = kpiAccountUnitService.getById(a.getGjks());
                a.setGjksName(byId.getName());
            }
            if (StringUtils.isNotEmpty(a.getZw())) {
                List<SysDictItem> zw_type = remoteDictService.getDictByType("cost_duties").getData();
                String value = Linq.of(zw_type).where(t -> t.getItemValue().equals(a.getZw())).select(SysDictItem::getLabel).firstOrDefault();
                a.setZwName(value);
            }
        }
        return kpiAccountUserDtoIPage;
    }

    /**
     * 自定义字段复制
     */
    public void copyCustomFields(String period) {
        List<CostUserAttendanceCustomFields> costUserAttendanceCustomFields =
                costUserAttendanceCustomFieldsMapper.selectList(new LambdaQueryWrapper<CostUserAttendanceCustomFields>()
                        .eq(CostUserAttendanceCustomFields::getDt, period));
        if (com.baomidou.mybatisplus.core.toolkit.CollectionUtils.isEmpty(costUserAttendanceCustomFields)) {
            //复制最新一个月的
            List<CostUserAttendanceCustomFields> latestByDt = costUserAttendanceCustomFieldsMapper.findLatestByDt();
            for (CostUserAttendanceCustomFields a : latestByDt) {
                CostUserAttendanceCustomFields customFields = new CostUserAttendanceCustomFields();
                BeanUtils.copyProperties(a, customFields);
                customFields.setId(null);
                customFields.setDt(period);
                costUserAttendanceCustomFieldsMapper.insert(customFields);
            }
        }
    }

    public void addDicCoefficient(KpiCoefficientDto2 dtos) {
        List<KpiCoefficientDto> desiredCoefficients = dtos.getDesiredCoefficients();
        if (!CollectionUtils.isEmpty(desiredCoefficients)) {
            for (KpiCoefficientDto dto : desiredCoefficients) {
                List<KpiCoefficient> kpiCoefficients = kpiCoefficientMapper.selectList(new LambdaQueryWrapper<KpiCoefficient>().eq(KpiCoefficient::getDicType, dto.getDic_type())
                        .eq(KpiCoefficient::getDicCode, dto.getDic_code()));
                if (!CollectionUtils.isEmpty(kpiCoefficients)) {
                    KpiCoefficient kpiCoefficient = kpiCoefficients.get(0);
                    kpiCoefficient.setValue(dto.getValue());
                    kpiCoefficientMapper.updateById(kpiCoefficient);
                } else {
                    KpiCoefficient kpiCoefficient = new KpiCoefficient();
                    kpiCoefficient.setDicType(dto.getDic_type());
                    kpiCoefficient.setDicCode(dto.getDic_code());
                    kpiCoefficient.setValue(dto.getValue());
                    kpiCoefficientMapper.insert(kpiCoefficient);
                }
            }
        }
    }

    public List<KpiCoefficientPageDto> pageCoefficient(KpiCoefficientDto dto) {
        List<SysDictItem> zw_type = remoteDictService.getDictByType(dto.getDic_type()).getData();
        List<KpiCoefficient> kpiCoefficients = kpiCoefficientMapper
                .selectList(new LambdaQueryWrapper<KpiCoefficient>().eq(KpiCoefficient::getDicType, dto.getDic_type()));
        List<KpiCoefficientPageDto> list = new ArrayList<>();
        for (SysDictItem b : zw_type) {
            KpiCoefficientPageDto kpiCoefficientPageDto = new KpiCoefficientPageDto();
            kpiCoefficientPageDto.setItem_value(b.getItemValue());
            kpiCoefficientPageDto.setLabel(b.getLabel());
            kpiCoefficientPageDto.setDic_type(dto.getDic_type());
            KpiCoefficient kpiCoefficient = Linq.of(kpiCoefficients)
                    .where(t -> t.getDicCode().equals(b.getItemValue())).firstOrDefault();
            if (kpiCoefficient != null) {
                kpiCoefficientPageDto.setValue(kpiCoefficient.getValue());
            }
            list.add(kpiCoefficientPageDto);
        }
        return list;
    }

    public void addValueAdjust(KpiValueAdjustDto dtos) {
        /*List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, dtos.getPeriod()));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if (kpiConfig.getIssuedFlag().equals("Y")) {
                throw new BizException("已锁定无法计算");
            }
        }*/
        if (dtos.getId() != null) {
            KpiValueAdjust kpiValueAdjust = kpiValueAdjustMapper.selectById(dtos.getId());
            BeanUtils.copyProperties(dtos, kpiValueAdjust);
            kpiValueAdjustMapper.updateById(kpiValueAdjust);
        } else {
            KpiValueAdjust kpiValueAdjust = new KpiValueAdjust();
            BeanUtils.copyProperties(dtos, kpiValueAdjust);
            kpiValueAdjustMapper.insert(kpiValueAdjust);
        }
    }

    public void copyValueAdjust(KpiValueAdjustCopyDto dtos) {
        List<KpiValueAdjust> kpiValueAdjusts = kpiValueAdjustMapper.selectBatchIds(dtos.getIds());
        for (KpiValueAdjust kpiValueAdjust : kpiValueAdjusts) {
            KpiValueAdjust kpiValueAdjustCopy = new KpiValueAdjust();
            BeanUtils.copyProperties(kpiValueAdjust, kpiValueAdjustCopy);
            kpiValueAdjustCopy.setPeriod(dtos.getPeriod());
            kpiValueAdjustCopy.setId(null);
            kpiValueAdjustMapper.insert(kpiValueAdjustCopy);
        }
    }

    public void delValueAdjust(Long id) {
        KpiValueAdjust kpiValueAdjust = kpiValueAdjustMapper.selectById(id);
        List<KpiConfig> config = kpiConfigService.list(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, kpiValueAdjust.getPeriod()));
        if (!CollectionUtils.isEmpty(config)) {
            KpiConfig kpiConfig = config.get(0);
            if (kpiValueAdjust.getBusiType().equals("1") && kpiConfig.getUserFlag().equals("Y")) {
                throw new BizException("已锁定无法删除");
            }
            if (kpiValueAdjust.getBusiType().equals("2") && kpiConfig.getUserFlagKs().equals("Y")) {
                throw new BizException("已锁定无法删除");
            }
        }
        kpiValueAdjustMapper.deleteById(id);
    }

    public IPage<KpiValueAdjustPageDto> pageValueAdjust(KpiValueAdjustSearchDto searchDto) {
        List<KpiAccountUnit> kpiAccountUnits = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>()
                //.eq(KpiAccountUnit::getStatus, 0)
                .eq(KpiAccountUnit::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(searchDto.getBusiType()), KpiAccountUnit::getBusiType, searchDto.getBusiType()));
        List<KpiUserAttendance> kpiUserAttendances = kpiUserAttendanceMapper.selectList(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getDelFlag, "0")
                .eq(KpiUserAttendance::getBusiType, searchDto.getBusiType()).eq(KpiUserAttendance::getPeriod, searchDto.getPeriod()));
        Page<KpiAccountUserDto> matchPage = new Page<>(searchDto.getCurrent(), searchDto.getSize());
        IPage<KpiValueAdjustPageDto> list = kpiUserAttendanceMapper.findAdjust(matchPage, searchDto);
        List<Long> ids = Linq.of(list.getRecords()).select(KpiValueAdjustPageDto::getUserId).toList();
        List<SysUser> userList = remoteUserService.getUserList(ids).getData();
        for (KpiValueAdjustPageDto dto : list.getRecords()) {
            KpiAccountUnit kpiAccountUnit = Linq.of(kpiAccountUnits).where(t -> t.getId().equals(dto.getAccountUnit())).firstOrDefault();
            if (kpiAccountUnit != null) {
                dto.setAccountUnitName(kpiAccountUnit.getName());
            }
//            KpiUserAttendance kpiUserAttendance = Linq.of(kpiUserAttendances)
//                    .where(t -> t.getUserId().equals(dto.getUserId())).firstOrDefault();
            List<String> user_name = Linq.of(userList).where(t -> t.getUserId().equals(dto.getUserId())).select(SysUser::getName).toList();
            if (!CollectionUtils.isEmpty(user_name)) {
                dto.setUserName(user_name.get(0));
            }
        }
        if (StringUtils.isNotEmpty(searchDto.getAccountUnitName())) {
            List<KpiValueAdjustPageDto> list1 = Linq.of(list.getRecords()).where(t -> t.getAccountUnitName().contains(searchDto.getAccountUnitName())).toList();
            list.setRecords(list1);
            list.setTotal(list1.size());
        }
        if (StringUtils.isNotEmpty(searchDto.getUserName())) {
            List<KpiValueAdjustPageDto> list1 = Linq.of(list.getRecords()).where(t -> t.getUserName().contains(searchDto.getUserName())).toList();
            list.setRecords(list1);
            list.setTotal(list1.size());
        }
        return list;
    }

    public List<KpiUserCalculationRule> pageCalculationRule(String busiType) {
        return
                kpiUserCalculationRuleMapper
                        .selectList(new LambdaQueryWrapper<KpiUserCalculationRule>()
                                .eq(KpiUserCalculationRule::getBusiType, busiType)
                                .orderByAsc(KpiUserCalculationRule::getStatus));
    }

    public void addCalculationRule(CalculationRuleInsertDto dto) {
        if (dto.getId() != null) {
            KpiUserCalculationRule kpiUserCalculationRule = kpiUserCalculationRuleMapper.selectById(dto.getId());
            BeanUtils.copyProperties(dto, kpiUserCalculationRule);
            if (StringUtils.isNotEmpty(dto.getJson())) {
                kpiUserCalculationRule.setRule(dto.getJson());
            }
            kpiUserCalculationRuleMapper.updateById(kpiUserCalculationRule);
        } else {
            KpiUserCalculationRule kpiUserCalculationRule = new KpiUserCalculationRule();
            BeanUtils.copyProperties(dto, kpiUserCalculationRule);
            kpiUserCalculationRule.setRule(dto.getJson());
            kpiUserCalculationRule.setTenantId(SecurityUtils.getUser().getTenantId());
            kpiUserCalculationRuleMapper.insert(kpiUserCalculationRule);
        }
    }

    public List<AttendanceCheckDTO> attendanceCheck(String busiType, Long period) {
        List<KpiUserAttendance> list = kpiUserAttendanceMapper.selectList(
                new QueryWrapper<KpiUserAttendance>()
                        .eq("period", period)
                        .eq("del_flag", "0")
                        .eq("busi_type", busiType)
        );
        List<SysUser> users = kpiUserAttendanceMapper.getItemUsers(busiType, period);
        List<Long> userIds = Linq.of(users).select(x -> x.getUserId()).toList();
        userIds.removeAll(Linq.of(list).select(KpiUserAttendance::getUserId).toList());

        if (!userIds.isEmpty()) {
            List<AttendanceCheckDTO> rt = kpiUserAttendanceMapper.getItemNames(
                    new QueryWrapper<AttendanceCheckDTO>()
                            .eq("period", period)
                            .in("user_id", userIds)
            );
            for (AttendanceCheckDTO r : rt) {
                r.setName(Linq.of(users).firstOrDefault(x -> x.getUserId().equals(r.getUserId())).getName());
            }
            return rt;
        }
        return null;
    }

    public void addHsUserRule(HsUserRuleInsertDto dto) {
        if (dto.getId() != null) {
            KpiHsUserRule kpiHsUserRule = kpiHsUserRuleMapper.selectById(dto.getId());
            BeanUtils.copyProperties(dto, kpiHsUserRule);
            if (StringUtils.isNotEmpty(dto.getJson())) {
                kpiHsUserRule.setRule(dto.getJson());
            }
            kpiHsUserRuleMapper.updateById(kpiHsUserRule);
        } else {
            KpiHsUserRule kpiHsUserRule = new KpiHsUserRule();
            BeanUtils.copyProperties(dto, kpiHsUserRule);
            kpiHsUserRule.setRule(dto.getJson());
            kpiHsUserRule.setTenantId(SecurityUtils.getUser().getTenantId());
            kpiHsUserRuleMapper.insert(kpiHsUserRule);
        }
    }

    public List<KpiHsUserRule> pageHsUserRule(HsUserRuleInsertDto dto) {
        return
                kpiHsUserRuleMapper
                        .selectList(new LambdaQueryWrapper<KpiHsUserRule>()
                                .eq(KpiHsUserRule::getBusiType, dto.getBusiType())
                                .eq(KpiHsUserRule::getCategoryCode, dto.getCategoryCode())
                                .orderByAsc(KpiHsUserRule::getStatus));
    }


    public String getNewHsUser(HsUserRuleInsertDto dto) {
        if (StringUtils.isNotBlank(dto.getCategoryCode())) {
            List<KpiHsUserRule> kpiHsUserRules = kpiHsUserRuleMapper
                    .selectList(new LambdaQueryWrapper<KpiHsUserRule>()
                            .eq(KpiHsUserRule::getBusiType, dto.getBusiType())
                            .eq(KpiHsUserRule::getStatus, "0")
                            .eq(KpiHsUserRule::getCategoryCode, dto.getCategoryCode()));
            List<KpiUserAttendance> all = new ArrayList<>();
            if(!CollectionUtils.isEmpty(kpiHsUserRules)) {
                for (KpiHsUserRule kpiHsUserRule : kpiHsUserRules) {
                    String rule = kpiHsUserRule.getRule();
                    KpiHsUserRuleDto kpiHsUserRuleDto = JSON.parseObject(rule, KpiHsUserRuleDto.class);
                    List<KpiUserAttendance> kpiUserAttendances = kpiUserAttendanceMapper.selectList(new LambdaQueryWrapper<KpiUserAttendance>()
                            .eq(KpiUserAttendance::getBusiType, dto.getBusiType()).eq(KpiUserAttendance::getDelFlag, "0")
                            .eq(KpiUserAttendance::getPeriod, dto.getPeriod())
                            .eq(StringUtils.isNotEmpty(kpiHsUserRuleDto.getAccountUnit()), KpiUserAttendance::getAccountUnit, kpiHsUserRuleDto.getAccountUnit())
                            .eq(StringUtils.isNotEmpty(kpiHsUserRuleDto.getAccountGroupName()), KpiUserAttendance::getAccountGroup, kpiHsUserRuleDto.getAccountGroupName())
                            .eq(StringUtils.isNotEmpty(kpiHsUserRuleDto.getJobNatureName()), KpiUserAttendance::getJobNature, kpiHsUserRuleDto.getJobNatureName())
                            .eq(StringUtils.isNotEmpty(kpiHsUserRuleDto.getDutiesName()), KpiUserAttendance::getDutiesName, kpiHsUserRuleDto.getDutiesName())
                            .eq(StringUtils.isNotEmpty(kpiHsUserRuleDto.getReward()), KpiUserAttendance::getReward, kpiHsUserRuleDto.getReward())
                            .eq(KpiUserAttendance::getBusiType, dto.getBusiType()).eq(KpiUserAttendance::getDelFlag, "0"));
                    all.addAll(kpiUserAttendances);
                }
                if (all.isEmpty()) {
                    //清空原有数据
                    kpiMemberMapper.delete(new LambdaQueryWrapper<KpiMember>()
                            .eq(KpiMember::getBusiType, dto.getBusiType())
                            .eq(KpiMember::getHostCode, dto.getCategoryCode())
                            .eq(KpiMember::getPeriod, "0")
                            .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType())
                    );
                    return "最终匹配0人";
                }
                List<Long> userIds = Linq.of(all).select(KpiUserAttendance::getUserId).distinct().toList();
                if (!userIds.isEmpty()) {
                    if (dto.getRemoveUnmatch().equals("Y")) {
                        //移除不匹配人员 删了全部重新添加
                        kpiMemberMapper.delete(new LambdaQueryWrapper<KpiMember>()
                                .eq(KpiMember::getBusiType, dto.getBusiType())
                                .eq(KpiMember::getHostCode, dto.getCategoryCode())
                                .eq(KpiMember::getPeriod, "0")
                                .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType())
                        );
                    } else {
                        //不移除不匹配人员
                        kpiMemberMapper.delete(new LambdaQueryWrapper<KpiMember>()
                                .eq(KpiMember::getBusiType, dto.getBusiType())
                                .eq(KpiMember::getHostCode, dto.getCategoryCode())
                                .eq(KpiMember::getPeriod, "0")
                                .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType())
                                .in(KpiMember::getMemberId, userIds));
                    }
                    List<KpiMember> members = new ArrayList<>();
                    for (Long aLong : userIds) {
                        KpiMember kpiMember = new KpiMember();
                        kpiMember.setTenantId(SecurityUtils.getUser().getTenantId());
                        kpiMember.setMemberId(aLong);
                        kpiMember.setMemberType(MemberEnum.ROLE_EMP.getType());
                        kpiMember.setHostCode(dto.getCategoryCode());
                        kpiMember.setPeriod(0L);
                        kpiMember.setCreatedDate(new Date());
                        kpiMember.setBusiType(dto.getBusiType());
                        members.add(kpiMember);
                    }
                    kpiMemberMapper.insertBatchSomeColumn(members);
                }
                return "最终匹配" + userIds.size() + "人";
            }
            return "";
        }else {
            //全量刷新
            //启用目录
            List<KpiCategory> categoryList = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>()
                    .eq(KpiCategory::getStatus, 0).eq(KpiCategory::getDelFlag, "0").eq(StringUtil.isNotBlank(dto.getBusiType()), KpiCategory::getBusiType, dto.getBusiType()));
            List<KpiCategory> list = Linq.of(categoryList).where(t -> t.getCategoryType().equals(CategoryEnum.USER_GROUP.getType())).toList();
            for (KpiCategory a : list) {
                dto.setCategoryCode(a.getCategoryCode());
                getNewHsUser(dto);
            }
            return "";
        }
    }

    public void hsUserRule_del(Long id) {
        kpiHsUserRuleMapper.deleteById(id);
    }

    public IPage<DicPageOutDto> protectDic(DicPageDto input) {
        Page<KpiAccountUserDto> matchPage = new Page<>(input.getCurrent(), input.getSize());
        IPage<DicPageOutDto> dictype = kpiUserAttendanceMapper.findDictype(matchPage, input);
        return dictype;
    }

    @Override
    public List<String> importData2(String categoryCode, List<Map<Integer, String>> list, String overwriteFlag) {
        //1覆盖 2增量
        List<KpiMember> li_insert = new ArrayList<>();
        List<Long> li_del = new ArrayList<>();
        List<KpiMember> members = new ArrayList<>();

        List<KpiMember> members3 = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .eq("busi_type", "1")
                        .in("member_type", Arrays.asList("emp_type,role_emp_zw".split(",")))
        );

        List<KpiMember> members2 = kpiMemberMapper.selectList(
                new QueryWrapper<KpiMember>()
                        .eq("busi_type", "1")
                        .eq("host_code", categoryCode)
                        .in("member_type", Arrays.asList("role_emp,role_emp_group".split(",")))
        );
        members.addAll(members2);
        members.addAll(members3);

        if ("1".equals(overwriteFlag)) {
            kpiMemberMapper.delete(
                    new QueryWrapper<KpiMember>()
                            .eq("busi_type", "1")
                            .eq("host_code", categoryCode)
                            .in("member_type", Arrays.asList("role_emp,role_emp_group".split(",")))
            );
        }
        //核算人员分组,核算人员类型,分组核算人员手动分配核算单元,分组核算人员手动分配职务
        List<SysDictItem> zw_type = remoteDictService.getDictByType("cost_duties").getData();
        List<SysDictItem> emp_types = remoteDictService.getDictByType("imputation_person_type").getData();
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        List<KpiAccountUnit> depts = kpiAccountUnitService.list(new QueryWrapper<KpiAccountUnit>().eq("busi_type", "1"));

        Map<Integer, String> top = list.get(0);
        List<KpiAccountUserDto> imports = new ArrayList<>();
        for (int i = 1; i < list.size(); i++) {
            KpiAccountUserDto entity = new KpiAccountUserDto();
            int finalI = i;
            top.forEach((key, value) -> {
                if ("核算人员".equals(value)) {
                    entity.setEmpName(list.get(finalI).get(key));
                    if (StringUtil.isNotBlank(entity.getEmpName())) {
                        SysUser first = Linq.of(users).firstOrDefault(x -> x.getUsername().equals(entity.getEmpName()));
                        if (first != null) {
                            entity.setEmpId(first.getUserId().toString());
                        }
                    }
                } else if ("人员类型".equals(value)) {
                    entity.setUserType(list.get(finalI).get(key));
                    if (StringUtil.isNotBlank(entity.getUserType())) {
                        SysDictItem first = Linq.of(emp_types).firstOrDefault(x -> x.getLabel().equals(entity.getUserType()));
                        if (first != null) {
                            entity.setUserTypeCode(first.getItemValue());
                        }
                    }
                } else if ("归集科室".equals(value)) {
                    entity.setGjksName(list.get(finalI).get(key));
                    if (StringUtil.isNotBlank(entity.getGjksName())) {
                        KpiAccountUnit first = Linq.of(depts).firstOrDefault(x -> x.getName().equals(entity.getGjksName()));
                        if (first != null) {
                            entity.setGjks(first.getId().toString());
                        }
                    }
                } else if ("核算职务".equals(value)) {
                    entity.setZwName(list.get(finalI).get(key));
                    if (StringUtil.isNotBlank(entity.getZwName())) {
                        SysDictItem first = Linq.of(zw_type).firstOrDefault(x -> x.getLabel().equals(entity.getZwName()));
                        if (first != null) {
                            entity.setZw(first.getItemValue());
                        }
                    }
                }
            });
            imports.add(entity);
        }

        Linq.of(imports).where(r -> r.getEmpId() != null).forEach(r -> {
            long userid = Long.parseLong(r.getEmpId());
            KpiMember kpiMember = Linq.of(members).firstOrDefault(x -> MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())
                    && userid == x.getMemberId());

            if (("2".equals(overwriteFlag) && kpiMember == null) || "1".equals(overwriteFlag)) {

                li_del.addAll(Linq.of(members).where(x -> !MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())
                        && x.getHostId() != null && x.getHostId() == userid).select(x -> x.getId()).toList());
                li_del.addAll(Linq.of(members).where(x -> MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())
                        && x.getMemberId() != null && x.getMemberId() == userid).select(x -> x.getId()).toList());

                if (StringUtil.isNotBlank(r.getEmpId())) {
                    KpiMember dto = new KpiMember();
                    dto.setMemberType(MemberEnum.ROLE_EMP.getType());
                    dto.setHostCode(categoryCode);
                    dto.setMemberId(userid);
                    dto.setCreatedDate(new Date());
                    dto.setBusiType("1");
                    dto.setPeriod(0L);
                    dto.setTenantId(SecurityUtils.getUser().getTenantId());
                    li_insert.add(dto);
                }
                if (StringUtil.isNotBlank(r.getUserTypeCode())) {
                    KpiMember dto = new KpiMember();
                    dto.setMemberType(MemberEnum.EMP_TYPE.getType());
                    //dto.setHostCode(categoryCode);
                    dto.setHostId(userid);
                    dto.setMemberCode(r.getUserTypeCode());
                    dto.setCreatedDate(new Date());
                    dto.setBusiType("1");
                    dto.setPeriod(0L);
                    dto.setTenantId(SecurityUtils.getUser().getTenantId());
                    li_insert.add(dto);
                }
                if (StringUtil.isNotBlank(r.getGjks())) {
                    KpiMember dto = new KpiMember();
                    dto.setMemberType(MemberEnum.ROLE_EMP_GROUP.getType());
                    dto.setHostCode(categoryCode);
                    dto.setHostId(userid);
                    dto.setMemberId(Long.parseLong(r.getGjks()));
                    dto.setCreatedDate(new Date());
                    dto.setBusiType("1");
                    dto.setPeriod(0L);
                    dto.setTenantId(SecurityUtils.getUser().getTenantId());
                    li_insert.add(dto);
                }
                if (StringUtil.isNotBlank(r.getZw())) {
                    KpiMember dto = new KpiMember();
                    dto.setMemberType(MemberEnum.ROLE_EMP_ZW.getType());
                    //dto.setHostCode(categoryCode);
                    dto.setHostId(userid);
                    dto.setMemberCode(r.getZw());
                    dto.setCreatedDate(new Date());
                    dto.setBusiType("1");
                    dto.setPeriod(0L);
                    dto.setTenantId(SecurityUtils.getUser().getTenantId());
                    li_insert.add(dto);
                }
            }

        });

        //1覆盖 2增量
        /*if ("1".equals(overwriteFlag)){
            li_del.addAll(Linq.of(members).where(x -> !MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())
                    &&Linq.of(imports).select(r -> Long.parseLong(r.getEmpId())).toList().contains(x.getHostId())).select(x -> x.getId()).toList());
            li_del.addAll(Linq.of(members).where(x -> MemberEnum.ROLE_EMP.getType().equals(x.getMemberType())
                    &&Linq.of(imports).select(r -> Long.parseLong(r.getEmpId())).toList().contains(x.getMemberId())).select(x -> x.getId()).toList());

            if (!li_del.isEmpty()){
                kpiMemberMapper.deleteBatchIds(li_del);
            }
        }*/
        if (!li_del.isEmpty()) {
            kpiMemberMapper.deleteBatchIds(li_del);
        }
        if (!li_insert.isEmpty()) {
            kpiMemberMapper.insertBatchSomeColumn(li_insert);
        }

        return Linq.of(imports).where(r -> r.getEmpId() == null).select(r -> r.getEmpName()).toList();
    }


    public List<KpiAttendanceMonthDaysDto> monthDays(Long year) {
        List<KpiAttendanceMonthDaysDto> monthDaysDtos = new ArrayList<>();
        List<KpiMonthDays> kpiMonthDays = kpiMonthDaysMapper
                .selectList(new LambdaQueryWrapper<KpiMonthDays>().like(KpiMonthDays::getPeriod, year)
                        .orderByAsc(KpiMonthDays::getPeriod));
        for (KpiMonthDays a : kpiMonthDays) {
            KpiAttendanceMonthDaysDto b = new KpiAttendanceMonthDaysDto();
            b.setMonthDays(a.getMonthDays());
            b.setPeriod(a.getPeriod());
            monthDaysDtos.add(b);
        }
        return monthDaysDtos;
    }

    public void editMonthDays(KpiAttendanceMonthDaysListDto dto) {
        List<KpiAttendanceMonthDaysDto> monthDays = dto.getMonthDays();
        if (!CollectionUtils.isEmpty(monthDays)) {
            for (KpiAttendanceMonthDaysDto a : monthDays) {
                editMonthDaysChild(a.getPeriod(), a.getMonthDays());
            }
        }
    }


    public Long editMonthDaysChild(Long lastCycle, Long monthDays) {
        LambdaQueryWrapper<KpiMonthDays> queryWrapper = Wrappers.<KpiMonthDays>lambdaQuery()
                .eq(KpiMonthDays::getPeriod, lastCycle);
        KpiMonthDays kpiMonthDays = kpiMonthDaysMapper.selectOne(queryWrapper);
        if (kpiMonthDays != null) {
            if (monthDays != null) {
                kpiMonthDays.setMonthDays(monthDays);
            } else {
                //获得当月天数
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date parse = sdf1.parse(String.valueOf(lastCycle));
                    String format = sdf.format(parse);
                    DateTime dateTime = DateUtil.parse(format);
                    int days = dateTime.getLastDayOfMonth();
                    kpiMonthDays.setMonthDays((long) days);
                } catch (Exception e) {

                }
            }
            kpiMonthDaysMapper.updateById(kpiMonthDays);
            return kpiMonthDays.getId();
        } else {
            KpiMonthDays newMonth = new KpiMonthDays();
            if (monthDays != null) {
                newMonth.setMonthDays(monthDays);
            } else {
                //获得当月天数
                SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMM");
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date parse = sdf1.parse(String.valueOf(lastCycle));
                    String format = sdf.format(parse);
                    DateTime dateTime = DateUtil.parse(format);
                    int days = dateTime.getLastDayOfMonth();
                    newMonth.setMonthDays((long) days);
                } catch (Exception e) {

                }
            }
            newMonth.setPeriod(lastCycle);
            newMonth.setTenantId(SecurityUtils.getUser().getTenantId());
            kpiMonthDaysMapper.insert(newMonth);
            return newMonth.getId();
        }
    }


    public void copyAttendance(DateDto dto) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMM");
        Long next_period = null;
        try {
            Date date = simpleDateFormat.parse(dto.getPeriod());
            Calendar calendar = Calendar.getInstance();
            // 设置为当前时间
            calendar.setTime(date);
            calendar.add(Calendar.MONTH, 1);
            date = calendar.getTime();
            next_period = Long.valueOf(simpleDateFormat.format(date));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        //上月的
        List<KpiUserAttendance> kpiUserAttendances = kpiUserAttendanceMapper
                .selectList(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, dto.getPeriod())
                        .eq(KpiUserAttendance::getBusiType, dto.getBusiType()));
//        Map<Long, Long> map = kpiUserAttendances.stream()
//                .collect(HashMap::new, (k, v) -> k.put(v.getAccountUnit(), v.getUserId()),HashMap::putAll);
        //当月的
        List<KpiUserAttendance> month_kpiUserAttendances = kpiUserAttendanceMapper
                .selectList(new LambdaQueryWrapper<KpiUserAttendance>().eq(KpiUserAttendance::getPeriod, next_period)
                        .eq(KpiUserAttendance::getBusiType, dto.getBusiType()));
        //去重
        List<KpiUserAttendance> finalAttendance = new ArrayList<>(kpiUserAttendances);
        for (KpiUserAttendance a : kpiUserAttendances) {
            Long userId = a.getUserId();
            Long accountUnit = a.getAccountUnit();
            int count = Linq.of(month_kpiUserAttendances)
                    .where(t -> t.getUserId().equals(userId) && t.getAccountUnit().equals(accountUnit)).count();
            if (count > 0) {
                finalAttendance.remove(a);
            }
        }
        if (!finalAttendance.isEmpty()) {
            Long finalNext_period = next_period;
            finalAttendance.forEach(t -> {
                t.setTenantId(SecurityUtils.getUser().getTenantId());
                //设置周期为这个月
                t.setPeriod(finalNext_period);
                t.setIsLocked("0");
            });
            List<List<KpiUserAttendance>> partition = ListUtils.partition(finalAttendance, 1000);
            partition.forEach(x -> {
                kpiUserAttendanceMapper.insertBatchSomeColumn(x);
            });
        }
    }
}
