package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.config.BaseConfig;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.mapper.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.kpi.IKpiImputationService;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import com.hscloud.hs.cost.account.service.kpi.KpiValidatorService;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteDictService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import jodd.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 归集表 服务实现类
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = false)
public class KpiImputationService extends ServiceImpl<KpiImputationMapper, KpiImputation> implements IKpiImputationService {


    private final RemoteUserService remoteUserService;
    private final RemoteDictService remoteDictService;
    @Autowired
    private KpiImputationMapper kpiImputationMapper;
    @Autowired
    private KpiMemberMapper kpiMemberMapper;
    @Autowired
    private KpiAccountUnitMapper kpiAccountUnitMapper;
    @Autowired
    private KpiUserAttendanceMapper kpiUserAttendanceMapper;
    @Autowired
    private KpiImputationRuleService kapiImputationRuleService;
    @Autowired
    private KpiMemberService kpiMemberService;
    @Autowired
    private KpiAccountUnitService kpiAccountUnitService;
    @Autowired
    private KpiCategoryService kpiCategoryService;
    @Autowired
    private KpiConfigService kpiConfigService;
    @Autowired
    private KpiItemService kpiitemservice;
    @Autowired
    private KpiValidatorService kpiValidatorService;
    @Value("${kpi.period.month:1}")
    private Integer month;
    @Autowired
    private KpiItemResultMapper kpiItemResultMapper;

    @Override
    public IPage<KpiImputationDeptDto> pageImputationDeptUnit(KpiImputationSearchDto dto) {
//        List<KpiUserAttendance> user_list = kpiUserAttendanceMapper.selectList(new LambdaQueryWrapper<KpiUserAttendance>()
//                .orderByDesc(KpiUserAttendance::getPeriod));
        Page<KpiImputationDeptDto> kpiImputationPage = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<KpiImputationDeptDto> imputationDeptDtoIPage = kpiImputationMapper.listByQueryDto(kpiImputationPage, dto);
        //核算分组
        List<SysDictItem> hsfz = remoteDictService.getDictByType("kpi_calculate_grouping").getData();
        //有值的话从表里取
        if (CollectionUtils.isNotEmpty(imputationDeptDtoIPage.getRecords())) {
            for (KpiImputationDeptDto a : imputationDeptDtoIPage.getRecords()) {
                String value = Linq.of(hsfz).where(t -> t.getItemValue().equals(a.getAccountGroup())).select(SysDictItem::getLabel).firstOrDefault();
                a.setAccountGroupName(value);
                //人员处理
                if (StringUtils.isNotBlank(a.getEmpids())) {
                    String[] empids = a.getEmpids().split(",");
                    List<String> user_ids = Arrays.asList(empids);
                    List<Long> ids = user_ids.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                    List<SysUser> userList = remoteUserService.getUserList(ids).getData();
                    for (int i = 0; i < empids.length; i++) {
                        AttendanceUserDto d = new AttendanceUserDto();
                        int finalI = i;
                        String user_name = Linq.of(userList).where(t -> t.getUserId().equals(Long.valueOf(empids[finalI]))).select(SysUser::getName).firstOrDefault();
                        d.setEmp_name(user_name);
                        d.setUserId(Long.valueOf(empids[finalI]));
                        a.getUser().add(d);
                    }
                }
            }
        }
        return imputationDeptDtoIPage;
//        else {
//            //没有值默认全部科室单元
////            Map<Long, List<KpiUserAttendance>> ks_user = user_list.stream().filter(t->t.getPeriod().equals(user_list.get(0).getPeriod()))
////                    .collect(Collectors.groupingBy(KpiUserAttendance::getAccountUnit));
////            ArrayList<Long> key2 = new ArrayList<>(ks_user.keySet());
////            for (int b = 0; b < key2.size(); b++) {
////                AttendanceUserDto d = new AttendanceUserDto();
////                Long ks_id = key2.get(b);
////                List<KpiUserAttendance> kpiUserAttendances = ks_user.get(ks_id);
////                List<String> emp_names = Linq.of(kpiUserAttendances).select(KpiUserAttendance::getEmpName).toList();
////                if (CollectionUtils.isNotEmpty(emp_names)){
////                    d.setEmp_name(String.join(",",emp_names));
////                }
////            }
//            imputationDeptDtoIPage = kpiImputationMapper.listByQueryDto_defalt(kpiImputationPage, dto);
//            for (KpiImputationDeptDto a : imputationDeptDtoIPage.getRecords()) {
//                String value = Linq.of(hsfz).where(t -> t.getItemValue().equals(a.getAccountGroup()))
//                        .select(SysDictItem::getLabel).firstOrDefault();
//                a.setAccountGroupName(value);
//            }
//            return imputationDeptDtoIPage;
//        }
    }

    /**
     * 新增规则
     *
     * @param dto
     */
    public Long addRule(KpiImputationAddDto dto) {
        SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
        if (dto.getId() != null && dto.getId() > 0) {
            //修改
            KpiImputationRule byId = kapiImputationRuleService.getById(dto.getId());
            if (byId == null) {
                throw new BizException("数据不存在");
            }
            String old_people = byId.getPeople();
            String old_memberIds = byId.getMemberIds();
            BeanUtils.copyProperties(dto, byId);
            byId.setUpdatedId(SecurityUtils.getUser().getId());
            byId.setUpdatedDate(new Date());
            kapiImputationRuleService.updateById(byId);
            //删除关联
            addRuleChild(old_people, old_memberIds, byId, df, 1L);
            //刷新归集
            //refresh(dto.getCategoryCode());
            return byId.getId();
        } else {
            //新增
            KpiImputationRule rule = new KpiImputationRule();
            BeanUtils.copyProperties(dto, rule);
            rule.setCreatedId(SecurityUtils.getUser().getId());
            rule.setCreatedDate(new Date());
            rule.setDelFalg("0");
            kapiImputationRuleService.save(rule);
            addRuleChild(dto.getPeople(), dto.getMemberIds(), rule, df, 0L);
            //刷新归集
            //refresh(dto.getCategoryCode());
            return rule.getId();
        }
    }

    /**
     * 规则关联
     *
     * @param old_people
     * @param old_memberIds
     * @param byId
     * @param df
     */
    public void addRuleChild(String old_people, String old_memberIds, KpiImputationRule byId, SimpleDateFormat df, Long type) {
        //删除关联
        if (old_people.equals("1")) {
            if (type == 1) {
                //修改
                //个人 userId关联
                String[] split = old_memberIds.split(",");
                List<String> user_ids = Arrays.asList(split);
                kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberId, user_ids)
                        .eq(KpiMember::getHostId, byId.getId()).eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_USER.getType())
                        .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType()));
            }
            //新的插入
            String[] split_new = byId.getMemberIds().split(",");
            List<String> user_ids_new = Arrays.asList(split_new);
            for (String a : user_ids_new) {
                KpiMember member = new KpiMember();
                member.setMemberId(Long.valueOf(a));
                member.setHostId(byId.getId());
                if (StringUtil.isNotBlank(byId.getBusiType())) {
                    member.setBusiType(byId.getBusiType());
                }
                member.setPeriod(0L);
                member.setMemberType(MemberEnum.IMPUTATION_RULE_USER.getType());
                member.setCreatedDate(new Date());
                kpiMemberService.save(member);
            }
        }
        if (old_people.equals("2")) {
            if (type == 1) {
                //人群分组  category_code关联
                String[] split = old_memberIds.split(",");
                List<String> user_codes = Arrays.asList(split);
                kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberCode, user_codes)
                        .eq(KpiMember::getHostId, byId.getId()).eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_GROUP.getType())
                        .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType()));
            }
            //新的插入
            String[] split_new = byId.getMemberIds().split(",");
            List<String> user_codes_new = Arrays.asList(split_new);
            for (String a : user_codes_new) {
                KpiMember member = new KpiMember();
                member.setMemberCode(a);
                member.setHostId(byId.getId());
                if (StringUtil.isNotBlank(byId.getBusiType())) {
                    member.setBusiType(byId.getBusiType());
                }
                member.setPeriod(0L);
                member.setMemberType(MemberEnum.IMPUTATION_RULE_GROUP.getType());
                member.setCreatedDate(new Date());
                kpiMemberService.save(member);
            }
        }
        if (old_people.equals("3")) {
            if (type == 1) {
                //核算项  category_code关联
                String[] split = old_memberIds.split(",");
                List<String> hs_codes = Arrays.asList(split);
                kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberCode, hs_codes)
                        .eq(KpiMember::getHostId, byId.getId()).eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_ITEM.getType())
                        .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType()));
            }
            String[] split_new = byId.getMemberIds().split(",");
            List<String> hs_codes_new = Arrays.asList(split_new);
            for (String a : hs_codes_new) {
                KpiMember member = new KpiMember();
                member.setMemberCode(a);
                member.setHostId(byId.getId());
                member.setPeriod(0L);
                if (StringUtil.isNotBlank(byId.getBusiType())) {
                    member.setBusiType(byId.getBusiType());
                }
                member.setMemberType(MemberEnum.IMPUTATION_RULE_GROUP.getType());
                member.setCreatedDate(new Date());
                kpiMemberService.save(member);
            }
        }
        if (old_people.equals("4")) {
            if (type == 1) {
                //科室id 关联
                kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getMemberCode, old_memberIds)
                        .eq(KpiMember::getHostId, byId.getId()).eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_DEPT.getType())
                        .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType()));
            }
            KpiMember member = new KpiMember();
            member.setMemberCode(byId.getMemberIds());
            member.setHostId(byId.getId());
            member.setPeriod(0L);
            if (StringUtil.isNotBlank(byId.getBusiType())) {
                member.setBusiType(byId.getBusiType());
            }
            member.setMemberType(MemberEnum.IMPUTATION_RULE_DEPT.getType());
            member.setCreatedDate(new Date());
            kpiMemberService.save(member);
        }
    }

    public List<KpiImputationListDto> listImputation(KpiImputationListSearchDto dto) {
        List<KpiAccountUnit> unit_list = kpiAccountUnitMapper.selectAllUnit(SecurityUtils.getUser().getTenantId(),
                dto.getBusiType() == null ? "1" : dto.getBusiType());
        //启用核算项
        List<KpiItem> item_list = kpiitemservice.list(new LambdaQueryWrapper<KpiItem>()
                .eq(KpiItem::getStatus, 0).eq(KpiItem::getDelFlag, "0")
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiItem::getBusiType, dto.getBusiType()));
        List<KpiImputationListDto> dtoList = new ArrayList<>();
        List<KpiImputationRule> list = kapiImputationRuleService.list(new LambdaQueryWrapper<KpiImputationRule>()
                .eq(KpiImputationRule::getDelFalg, "0")
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiImputationRule::getBusiType, dto.getBusiType())
                .eq(KpiImputationRule::getCategoryCode, dto.getCategoryCode())
                .orderByAsc(KpiImputationRule::getSeq));
        //科室对应的人 考勤
        if (dto.getPeriod() == null) {
            Long period = Long.valueOf(kpiConfigService.getLastCycle(false));
            dto.setPeriod(period);
        }
        List<KpiAccountUnit> zhonzhishi = kpiAccountUnitMapper.selectList(new LambdaQueryWrapper<KpiAccountUnit>()
                .eq(KpiAccountUnit::getBusiType, dto.getBusiType())
                .eq(KpiAccountUnit::getStatus, "0")
                .eq(KpiAccountUnit::getName, "中治室"));
        Long zhonzhishiId = null;
        if (!zhonzhishi.isEmpty()) {
            zhonzhishiId = zhonzhishi.get(0).getId();
        }
        List<KpiImputationDeptDto> kpiImputationDeptDtos = kpiImputationMapper.listByQueryDto_defalt2(dto.getBusiType(), dto.getPeriod(), zhonzhishiId);
        List<KpiCategory> listUserGroup = kpiCategoryService.list(Wrappers.<KpiCategory>lambdaQuery()
                .eq(KpiCategory::getCategoryType, CategoryEnum.USER_GROUP.getType())
                .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiCategory::getBusiType, dto.getBusiType()));
        for (KpiImputationRule a : list) {
            //组装
            KpiImputationListDto d = new KpiImputationListDto();
            BeanUtils.copyProperties(a, d);
            KpiAccountUnit dept = Linq.of(unit_list).where(t -> t.getId().equals(a.getDeptId()))
                    .firstOrDefault();
            if (dept != null) {
                String dept_name = dept.getName();
                if ("1".equals(dept.getDelFlag())) {
                    dept_name = dept_name + "(已删除)";
                } else if ("1".equals(dept.getStatus())) {
                    dept_name = dept_name + "(已停用)";
                }
                d.setDeptName(dept_name);
            }
            if (a.getRuleType().equals("1")) {
                d.setRuleTypeName("特殊归集");
            }
            if (a.getRuleType().equals("2")) {
                d.setRuleTypeName("无需归集");
            }
            if (a.getPeople().equals("1")) {
                d.setPeople_name("个人");
                String[] split = a.getMemberIds().split(",");
                List<String> user_ids = Arrays.asList(split);
                List<Long> ids = user_ids.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                List<SysUser> userList = remoteUserService.getUserList(ids).getData();
                List<String> user_name = Linq.of(userList).select(SysUser::getName).toList();
                d.setMemberNames(String.join(",", user_name));
                d.setOriginNames(String.join(",", user_name));
            }
            if (a.getPeople().equals("2")) {
                d.setPeople_name("人员分组");
                String[] split = a.getMemberIds().split(",");
                List<String> user_codes = Arrays.asList(split);
                //KpiCategory role_group = Linq.of(categoryList).where(t -> t.getCategoryCode().equals(a.getMemberIds())).toList().get(0);
                //获得该人群分组下的人
                List<KpiMember> role_emps = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getHostCode, user_codes)
                        .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType())
                        .eq(StringUtil.isNotBlank(dto.getBusiType()), KpiMember::getBusiType, dto.getBusiType()));
                List<Long> user_ids = Linq.of(role_emps).select(KpiMember::getMemberId).toList();
                List<SysUser> userList = remoteUserService.getUserList(user_ids).getData();
                List<String> user_name = Linq.of(userList).select(SysUser::getName).toList();
                d.setMemberNames(String.join(",", user_name));
                d.setOriginNames(String.join(",", Linq.of(listUserGroup).where(t -> user_codes.contains(t.getCategoryCode())).select(KpiCategory::getCategoryName).toList()));
            }
            if (a.getPeople().equals("3")) {
                d.setPeople_name("核算分组");
                String originName = "";
                d.setOriginNames(originName);
                List<Long> user_ids = new ArrayList<>();
                //核算项关联
                List<KpiItem> kpiItem_list = Linq.of(item_list).where(t -> a.getMemberIds().contains(t.getCode())).toList();

                if (CollectionUtils.isNotEmpty(kpiItem_list)) {
                    for (KpiItem kpiItem : kpiItem_list) {
//                        Date lastMonth = DateUtil.offsetMonth(new Date(), -month);
//                        String period = DateUtil.format(lastMonth, DatePattern.NORM_DATE_PATTERN);
//                        KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(kpiItem, period);
//                        ValidatorResultVo resultVo = kpiValidatorService.itemValidator(validatorDTO, false, null, null);
//                        //得到结果集列表
//                        JSONArray resultList = JSONArray.parseArray(resultVo.getResult());
//                        if (resultList != null) {
//                            for (int i = 0; i < resultList.size(); i++) {
//                                JSONObject jsonObject = resultList.getJSONObject(i);
//                                String user_id = jsonObject.getString("user_id");
//                                if (StringUtils.isNotEmpty(user_id)) {
//                                    user_ids.add(Long.valueOf(user_id));
//                                }
//                            }
//                        }
                        d.setOriginNames(d.getOriginNames() + "," + kpiItem.getItemName());
                    }
                    d.setOriginNames(d.getOriginNames().substring(1));
                }
//                List<SysUser> userList = remoteUserService.getUserList(user_ids).getData();
//                List<String> user_name = Linq.of(userList).select(SysUser::getName).toList();
//                if (CollectionUtils.isNotEmpty(user_name)) {
//                    d.setMemberNames(String.join(",", user_name));
//                }
            }
            if (a.getPeople().equals("4")) {
                d.setPeople_name("科室分组");
                List<Long> user_ids = new ArrayList<>();
                KpiImputationDeptDto kpiImputationDeptDto = Linq.of(kpiImputationDeptDtos).where(t -> t.getAccountUnit().equals(a.getMemberIds())).firstOrDefault();
                if (kpiImputationDeptDto != null) {
                    if (StringUtils.isNotBlank(kpiImputationDeptDto.getEmpids())) {
                        String[] split = kpiImputationDeptDto.getEmpids().split(",");
                        List<String> user_id = Arrays.asList(split);
                        user_ids = user_id.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                    }
                    d.setOriginNames(kpiImputationDeptDto.getAccountUnitName());
                }
                List<SysUser> userList = remoteUserService.getUserList(user_ids).getData();
                List<String> user_name = Linq.of(userList).select(SysUser::getName).toList();
                if (CollectionUtils.isNotEmpty(user_name)) {
                    d.setMemberNames(String.join(",", user_name));
                }
            }
            dtoList.add(d);
        }
        if (StringUtils.isNotBlank(dto.getName())) {
            return Linq.of(dtoList).where(t -> t.getMemberNames().contains(dto.getName())).toList();
        }
        return dtoList;
    }

    public void removeRule(Long id) {
        KpiImputationRule byId = kapiImputationRuleService.getById(id);
        byId.setDelFalg("1");
        kapiImputationRuleService.updateById(byId);
        //删除关联
        if (byId.getPeople().equals("1")) {
            //个人 userId关联
            String[] split = byId.getMemberIds().split(",");
            List<String> user_ids = Arrays.asList(split);
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberId, user_ids)
                    .eq(KpiMember::getHostId, byId.getId())
                    .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType())
                    .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_USER.getType()));
        }
        if (byId.getPeople().equals("2")) {
            //人群分组  category_code关联
            String[] split = byId.getMemberIds().split(",");
            List<String> user_codes = Arrays.asList(split);
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberCode, user_codes)
                    .eq(KpiMember::getHostId, byId.getId())
                    .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType())
                    .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_GROUP.getType()));
        }
        if (byId.getPeople().equals("3")) {
            //核算项  code关联
            String[] split = byId.getMemberIds().split(",");
            List<String> hs_codes = Arrays.asList(split);
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().in(KpiMember::getMemberCode, hs_codes)
                    .eq(KpiMember::getHostId, byId.getId())
                    .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType())
                    .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_ITEM.getType()));
        }
        if (byId.getPeople().equals("4")) {
            //核算项  code关联
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getMemberCode, byId.getMemberIds())
                    .eq(KpiMember::getHostId, byId.getId())
                    .eq(StringUtil.isNotBlank(byId.getBusiType()), KpiMember::getBusiType, byId.getBusiType())
                    .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_RULE_DEPT.getType()));
        }
    }

    public void refresh(String code, Long input_period, String busiType) {
        refresh(code, input_period, busiType, null, null,false);
    }

    /**
     * 规则刷新
     */
    @Transactional(rollbackFor = Exception.class)
    @SchedulerLock(name = BaseConfig.appCode + "_refresh")
    public void refresh(String code, Long input_period, String busiType, Long tenantId, Long period,boolean forceRefresh) {
        if (tenantId == null) {
            tenantId = SecurityUtils.getUser().getTenantId();
        }
        if (period == null && input_period == null) {
            period = Long.valueOf(kpiConfigService.getLastCycle(false));
        }
        if (input_period != null) {
            period = input_period;
        }
        KpiConfig one = kpiConfigService.getOne(new LambdaQueryWrapper<KpiConfig>().eq(KpiConfig::getPeriod, period));
        if (busiType.equals("1")) {
            if ((one != null && one.getIssuedFlag().equals("Y") && !forceRefresh) ||
                    (one != null && one.getUserFlag().equals("Y") && !forceRefresh) ) {
                return;
            }
        }
        if (busiType.equals("2")) {
            if ((one != null && one.getIssuedFlag().equals("Y")) ||
                    (one != null && one.getUserFlagKs().equals("Y"))) {
                return;
            }
        }

        //启用目录
        List<KpiCategory> categoryList = kpiCategoryService.list(new LambdaQueryWrapper<KpiCategory>()
                .eq(KpiCategory::getStatus, 0).eq(KpiCategory::getDelFlag, "0").eq(StringUtil.isNotBlank(busiType), KpiCategory::getBusiType, busiType));
        //启用核算项
        List<KpiItem> itemList = kpiitemservice.list(new LambdaQueryWrapper<KpiItem>()
                .eq(KpiItem::getStatus, 0).eq(KpiItem::getDelFlag, "0").eq(StringUtil.isNotBlank(busiType), KpiItem::getBusiType, busiType));

        if (StringUtils.isNotBlank(code)) {
            //删除旧归集
            remove(new LambdaQueryWrapper<KpiImputation>().eq(KpiImputation::getCategoryCode, code)
                    .eq(KpiImputation::getPeriod, period).eq(StringUtil.isNotBlank(busiType), KpiImputation::getBusiType, busiType));
            //移除member关联
            kpiMemberService.remove(new LambdaQueryWrapper<KpiMember>().eq(KpiMember::getHostCode, code)
                    .eq(StringUtil.isNotBlank(busiType), KpiMember::getBusiType, busiType)
                    .eq(KpiMember::getMemberType, MemberEnum.IMPUTATION_DEPT_EMP.getType()));
            List<KpiImputationRule> rule_list = kapiImputationRuleService.
                    list(new LambdaQueryWrapper<KpiImputationRule>()
                            .eq(StringUtil.isNotBlank(busiType), KpiImputationRule::getBusiType, busiType)
                            .eq(KpiImputationRule::getCategoryCode, code)
                            .eq(KpiImputationRule::getDelFalg, "0")
                            .orderByDesc(KpiImputationRule::getSeq));
            List<KpiAccountUnit> zhonzhishi = kpiAccountUnitMapper.selectList(new LambdaQueryWrapper<KpiAccountUnit>()
                    .eq(KpiAccountUnit::getBusiType, busiType)
                    .eq(KpiAccountUnit::getStatus, "0")
                    .eq(KpiAccountUnit::getName, "中治室"));
            Long zhonzhishiId = null;
            if (!zhonzhishi.isEmpty()) {
                zhonzhishiId = zhonzhishi.get(0).getId();
            }
            //默认的人员归集
            List<KpiImputationDeptDto> kpiImputationDeptDtos = kpiImputationMapper.listByQueryDto_defalt2(busiType, period, zhonzhishiId);
            //规则处理
            List<KpiImputationDeptDto> rulesBefore = ruleFirstDispose(rule_list, categoryList, itemList, kpiImputationDeptDtos, period);
            //TODO 默认归集添加鄞州门诊
            //kpiImputationDeptDtos = getDefaltImputation(kpiImputationDeptDtos, rule_list, itemList, period);
            kpiImputationDeptDtos.forEach(t -> {
                if (t.getEmpids() != null) {
                    t.setList(Linq.of(Arrays.asList(t.getEmpids().split(","))).toList());
                } else {
                    t.setList(new ArrayList<>());
                }
            });
            //TODO 做互相比较处理
            for (KpiImputationDeptDto rule : rulesBefore) {
                //优先级从低到高
                String empids = rule.getEmpids();
                if (StringUtils.isNotBlank(empids)) {
                    String[] split = empids.split(",");
                    List<String> rule_UserIds = Arrays.asList(split);
                    if (rule.getAccountUnit().equals("-1")) {
                        //无需归集
                        for (KpiImputationDeptDto d : kpiImputationDeptDtos) {
                            d.setList(Linq.of(d.getList()).where(t -> !rule_UserIds.contains(t)).toList());
                        }
//                        for (String a : rule_UserIds) {
//                            if (StringUtils.isNotBlank(d.getEmpids()) && d.getEmpids().contains(a)) {
//                                //如果上一位是逗号
//                                if (d.getEmpids().indexOf(a) - 1 >= 0 && d.getEmpids().substring(d.getEmpids().indexOf(a) - 1, d.getEmpids().indexOf(a)).equals(",")) {
//                                    String replace = d.getEmpids().replace("," + a, "");
//                                    d.setEmpids(replace);
//                                } else {
//                                    //第一位
//                                    String replace = d.getEmpids().replace(a, "");
//                                    d.setEmpids(replace);
//                                }
//                            }
//                        }
//                }
                    } else {
                        //有具体科室
                        for (KpiImputationDeptDto d : kpiImputationDeptDtos) {
                            d.setList(Linq.of(d.getList()).where(t -> !rule_UserIds.contains(t)).toList());
                            if (d.getAccountUnit().equals(rule.getAccountUnit())) {
                                d.getList().addAll(rule_UserIds);
                            }
//                        for (String a : rule_UserIds) {
//                            if (StringUtils.isNotBlank(d.getEmpids()) && d.getEmpids().contains(a) && !d.getAccountUnit().equals(b.getAccountUnit())) {
//                                //如果上一位是逗号
//                                if (d.getEmpids().indexOf(a) - 1 >= 0 && d.getEmpids().substring(d.getEmpids().indexOf(a) - 1, d.getEmpids().indexOf(a)).equals(",")) {
//                                    String replace = d.getEmpids().replace("," + a, "");
//                                    d.setEmpids(replace);
//                                } else {
//                                    //第一位
//                                    String replace = d.getEmpids().replace(a, "");
//                                    d.setEmpids(replace);
//                                }
//                            }
//                            //缺少补上
//                            if (StringUtils.isNotBlank(d.getEmpids()) && !d.getEmpids().contains(a) && d.getAccountUnit().equals(b.getAccountUnit())) {
//                                d.setEmpids(d.getEmpids() + "," + a);
//                            }
//                        }
                        }
                    }
                }
            }
            for (KpiImputationDeptDto d : kpiImputationDeptDtos) {
                d.setEmpids(String.join(",", d.getList().stream().distinct().collect(Collectors.toList())));
            }
            //最终形态
            List<KpiImputation> imputations = new ArrayList<>();
            List<KpiMember> members = new ArrayList<>();

            for (KpiImputationDeptDto a : kpiImputationDeptDtos) {
                KpiImputation kpiImputation = new KpiImputation();
                kpiImputation.setCategoryCode(code);
                kpiImputation.setCategoryCode(code);
                kpiImputation.setBusiType(busiType);
                kpiImputation.setDeptId(Long.valueOf(a.getAccountUnit()));
                kpiImputation.setPeriod(period);
                //kpiImputation.setPeriod(Long.valueOf(df.format(new Date())));
                kpiImputation.setCreatedId(0L);
                kpiImputation.setCreatedDate(new Date());
                kpiImputation.setEmpids(a.getEmpids());
                imputations.add(kpiImputation);
                //生成Member
                if (StringUtils.isNotBlank(a.getEmpids())) {
                    String[] split = a.getEmpids().split(",");
                    List<String> user_ids = Arrays.asList(split);
                    for (String user : user_ids) {
                        KpiMember member = new KpiMember();
                        //member.setPeriod(Long.valueOf(df.format(new Date())));
                        member.setPeriod(period);
                        member.setHostId(Long.valueOf(a.getAccountUnit()));
                        member.setHostCode(code);
                        member.setBusiType(busiType);
                        member.setMemberType(MemberEnum.IMPUTATION_DEPT_EMP.getType());
                        member.setMemberId(Long.valueOf(user));
                        member.setCreatedDate(new Date());
                        members.add(member);
                    }
                }
            }
            if (!imputations.isEmpty()) {
                for (KpiImputation t : imputations) {
                    t.setTenantId(tenantId);
                }
                List<List<KpiImputation>> partition = ListUtils.partition(imputations, 1000);
                partition.forEach(x -> {
                    kpiImputationMapper.insertBatchSomeColumn(x);
                });
            }
            if (!members.isEmpty()) {
                for (KpiMember t : members) {
                    t.setTenantId(tenantId);
                }
                List<List<KpiMember>> partition = ListUtils.partition(members, 1000);
                partition.forEach(x -> {
                    kpiMemberMapper.insertBatchSomeColumn(x);
                });
            }

        } else {
            //全量刷新
            List<KpiCategory> list = Linq.of(categoryList).where(t -> t.getCategoryType().equals(CategoryEnum.IMPUTATION_GROUP.getType())).toList();
            for (KpiCategory a : list) {
                refresh(a.getCategoryCode(), input_period, busiType, tenantId, period,forceRefresh);
            }
        }
    }

    /**
     * @param before
     * @param
     * @return
     */
    public List<KpiImputationDeptDto> getDefaltImputation(List<KpiImputationDeptDto> before, List<KpiImputationRule> rule_list, List<KpiItem> item_list, Long period) {
        //核算项
        rule_list = Linq.of(rule_list).where(t -> t.getPeople().equals("3")).toList();
        for (KpiImputationRule b : rule_list) {
            KpiImputationDeptDto dto = new KpiImputationDeptDto();
            List<String> user_ids = new ArrayList<>();
            List<KpiItem> list = Linq.of(item_list).where(t -> b.getMemberIds().contains(t.getCode())).toList();
            if (CollectionUtils.isNotEmpty(list)) {
                for (KpiItem kpiItem : list) {
                    KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(kpiItem, period.toString());
                    ValidatorResultVo resultVo = kpiValidatorService.itemValidator(validatorDTO, false, null, null);
                    //得到结果集列表
                    JSONArray resultList = JSONArray.parseArray(resultVo.getResult());
                    if (resultList != null) {
                        for (int i = 0; i < resultList.size(); i++) {
                            JSONObject jsonObject = resultList.getJSONObject(i);
                            String user_id = jsonObject.getString("user_id");
                            if (StringUtil.isNotBlank(user_id)) {
                                user_ids.add(user_id);
                            }
                        }
                    }
                }
                dto.setAccountUnit(String.valueOf(b.getDeptId()));
                dto.setEmpids(String.join(",", user_ids));
            }
            //如果原本已有 把原本默认删除
            KpiImputationDeptDto kpiImputationDeptDto = Linq.of(before).where(t -> t.getAccountUnit().equals(dto.getAccountUnit())).firstOrDefault();
            if (kpiImputationDeptDto == null) {
                before.add(dto);
            } else {
                before.remove(kpiImputationDeptDto);
                before.add(dto);
            }
        }
        return before;
    }

    /**
     * 规则处理
     *
     * @return
     */
    public List<KpiImputationDeptDto> ruleFirstDispose(List<KpiImputationRule> rule_list, List<KpiCategory> categoryList
            , List<KpiItem> item_list, List<KpiImputationDeptDto> kpiImputationDeptDtos, Long period) {
        //rule_list = Linq.of(rule_list).where(t -> !t.getPeople().equals("3")).toList();

        List<KpiImputationDeptDto> dtoList = new ArrayList<>();
        for (KpiImputationRule b : rule_list) {
            List<Long> user_ids = new ArrayList<>();
            if (b.getPeople().equals("2")) {
                //群体
                String[] split = b.getMemberIds().split(",");
                List<String> user_codes = Arrays.asList(split);
                List<KpiMember> role_emps = kpiMemberService.list(new LambdaQueryWrapper<KpiMember>()
                        .in(KpiMember::getHostCode, user_codes)
                        .eq(KpiMember::getBusiType, b.getBusiType())
                        .eq(KpiMember::getMemberType, MemberEnum.ROLE_EMP.getType()));
                user_ids = Linq.of(role_emps).select(KpiMember::getMemberId).toList();
            } else if (b.getPeople().equals("1")) {
                //个人 userId关联
                String[] split = b.getMemberIds().split(",");
                List<String> ids = Arrays.asList(split);
                user_ids = ids.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
            } else if (b.getPeople().equals("3")) {
                //核算项关联
//                String[] split = b.getMemberIds().split(",");
//                List<String> hs_codes = Arrays.asList(split);
//                List<KpiItemResult> kpiItemResults = kpiItemResultMapper.selectList(new LambdaQueryWrapper<KpiItemResult>().eq(KpiItemResult::getPeriod, period)
//                        .eq(KpiItemResult::getBusiType, b.getBusiType())
//                        .in(CollectionUtils.isNotEmpty(hs_codes), KpiItemResult::getCode, hs_codes));
//                user_ids = Linq.of(kpiItemResults).where(t -> t.getUserId() != null).select(KpiItemResult::getUserId).toList();
                List<KpiItem> list = Linq.of(item_list).where(t -> b.getMemberIds().contains(t.getCode())).toList();
                if (CollectionUtils.isNotEmpty(list)) {
                    for (KpiItem kpiItem : list) {
                        KpiValidatorDTO validatorDTO = KpiValidatorDTO.changeToValidatorDTO(kpiItem, period.toString());
                        ValidatorResultVo resultVo = kpiValidatorService.itemValidator(validatorDTO, false, null, null);
                        //得到结果集列表
                        JSONArray resultList = JSONArray.parseArray(resultVo.getResult());
                        if (resultList != null) {
                            for (int i = 0; i < resultList.size(); i++) {
                                JSONObject jsonObject = resultList.getJSONObject(i);
                                String user_id = jsonObject.getString("user_id");
                                if (StringUtils.isNotBlank(user_id)) {
                                    user_ids.add(Long.valueOf(user_id));
                                }
                            }
                        }
                    }
                }
            } else if (b.getPeople().equals("4")) {
                //科室id关联
                KpiImputationDeptDto kpiImputationDeptDto = Linq.of(kpiImputationDeptDtos).where(t -> t.getAccountUnit().equals(b.getMemberIds())).firstOrDefault();
                if (kpiImputationDeptDto != null && StringUtil.isNotEmpty(kpiImputationDeptDto.getEmpids())) {
                    String[] split = kpiImputationDeptDto.getEmpids().split(",");
                    List<String> user_id = Arrays.asList(split);
                    user_ids = user_id.stream().map(s -> Long.parseLong(s.trim())).collect(Collectors.toList());
                }
            }
            KpiImputationDeptDto rule = new KpiImputationDeptDto();
            if (!b.getRuleType().equals("3")) {
                if (b.getRuleType().equals("1")) {
                    rule.setAccountUnit(String.valueOf(b.getDeptId()));
                } else if (b.getRuleType().equals("2")) {
                    //无需归集
                    rule.setAccountUnit("-1");
                }
                StringBuilder builder = new StringBuilder();
                for (Long a : user_ids.stream().distinct().collect(Collectors.toList())) {
                    builder.append(a).append(",");
                }
                if (builder.length() > 0) {
                    rule.setEmpids(builder.substring(0, builder.length() - 1));
                }
                rule.setSeq(b.getSeq());
                dtoList.add(rule);
            } else {
                //群组专用 科室单元指向手动分配的
                String[] split = b.getMemberIds().split(",");
                List<String> user_codes = Arrays.asList(split);
                List<Long> user_idList = new ArrayList<>();
                List<KpiMember> emp_list = kpiMemberService.list(new QueryWrapper<KpiMember>()
                        .eq("member_type", MemberEnum.ROLE_EMP.getType())
                        .eq("busi_type", b.getBusiType())
                        .in("host_code", user_codes));
                if (!CollectionUtils.isEmpty(emp_list)) {
                    user_idList = Linq.of(emp_list).select(KpiMember::getMemberId).toList();
                }
                List<KpiMember> user_account = kpiMemberService.list(new QueryWrapper<KpiMember>()
                        .eq("member_type", MemberEnum.ROLE_EMP_GROUP.getType())
                        .eq("busi_type", b.getBusiType())
                        .in("host_code", user_codes)
                        .in(CollectionUtils.isNotEmpty(user_idList), "host_id", user_idList));
                for (String code : user_codes) {
                    //找到对应核算人员分组的分配的人的科室
                    List<KpiMember> user_account_child = Linq.of(user_account).where(t -> t.getHostCode().equals(code)).toList();
                    //将该分组下的人按科室id分组
                    Map<Long, List<KpiMember>> collect = user_account_child.stream().collect(Collectors.groupingBy(KpiMember::getMemberId));
                    ArrayList<Long> key2 = new ArrayList<>(collect.keySet());
                    for (Long key : key2) {
                        KpiImputationDeptDto rule2 = new KpiImputationDeptDto();
                        //每一条拆分 相当于该分组下的每个人进行个人特殊归集
                        user_ids = Linq.of(collect.get(key)).select(KpiMember::getHostId).toList();
                        rule2.setAccountUnit(String.valueOf(key));
                        StringBuilder builder = new StringBuilder();
                        for (Long a : user_ids.stream().distinct().collect(Collectors.toList())) {
                            builder.append(a).append(",");
                        }
                        if (builder.length() > 0) {
                            rule2.setEmpids(builder.substring(0, builder.length() - 1));
                        }
                        rule2.setSeq(b.getSeq());
                        dtoList.add(rule2);
                    }
                }
            }
        }
        return dtoList;
    }

}
