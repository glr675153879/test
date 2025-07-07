package com.hscloud.hs.cost.account.service.impl.kpi;

import cn.hutool.core.util.NumberUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.bestvike.linq.Linq;
import com.hscloud.hs.cost.account.constant.enums.EnableEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.CategoryEnum;
import com.hscloud.hs.cost.account.constant.enums.kpi.MemberEnum;
import com.hscloud.hs.cost.account.listener.kpi.KpiUnitRelationListener;
import com.hscloud.hs.cost.account.mapper.kpi.KpiCalculateMapper;
import com.hscloud.hs.cost.account.model.dto.kpi.*;
import com.hscloud.hs.cost.account.model.dto.kpi.excel.KpiUnitRelationImportDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.*;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountRelationVO;
import com.hscloud.hs.cost.account.model.vo.kpi.KpiAccountUnitVO;
import com.hscloud.hs.cost.account.service.kpi.IKpiCategoryService;
import com.pig4cloud.pigx.admin.api.entity.SysDictItem;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserCoreVo;
import com.pig4cloud.pigx.common.core.constant.SecurityConstants;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import io.netty.util.internal.StringUtil;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountUnitMapper;
import com.hscloud.hs.cost.account.service.kpi.KpiAccountUnitService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
* 核算单元 服务实现类
*
 * @author Administrator
 */
@Service
@RequiredArgsConstructor
@Transactional(rollbackFor = Exception.class)
public class KpiAccountUnitServiceImpl extends ServiceImpl<KpiAccountUnitMapper, KpiAccountUnit> implements KpiAccountUnitService {
    private final RemoteUserService remoteUserService;
    private final IKpiCategoryService kpiCategoryService;
    private final KpiMemberService kpiMemberService;
    private final KpiCalculateMapper kpiCalculateMapper;
    private final KpiAccountUnitMapper kpiAccountUnitMapper;

    /**
     * 科室负责人默认类型 人员
     */
    private static final String DEFAULT_PERSON_TYPE = "user";
    /**
     * 医生组code
     */
    private static final String DOCTOR_ACCOUNT_UNIT = "HSDX001";
    /**
     * 护士组code
     */
    private static final String NURSE_ACCOUNT_UNIT = "HSDX002";
    /**
     * 护士组默认结尾
     */
    private static final String WARD_ACCOUNT_UNIT = "病区";
    /**
     * 科室成本的类型
     */
    private static final String BUSI_TYPE_2 = "2";

    @Override
    public Long saveOrUpdate(KpiAccountUnitDTO dto) {
        LambdaQueryWrapper<KpiAccountUnit> queryWrapper = Wrappers.<KpiAccountUnit>lambdaQuery()
                .eq(KpiAccountUnit::getName, dto.getName())
                .eq(KpiAccountUnit::getBusiType, dto.getBusiType())
                .eq(ObjectUtils.isNotEmpty(dto.getGroupCode()), KpiAccountUnit::getGroupCode, dto.getGroupCode());

        KpiAccountUnit unit = new KpiAccountUnit();
        List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        String userName = getUserName(dto.getResponsiblePersonId(), var1);
        if (null == dto.getId() || dto.getId() == 0) {
            if (this.count(queryWrapper) > 0) {
                throw new BizException("科室单元名称已存在");
            }

            BeanUtils.copyProperties(dto, unit);
            unit.setResponsiblePersonType(DEFAULT_PERSON_TYPE);
            if (StringUtils.hasLength(dto.getResponsiblePersonType())) {
                unit.setResponsiblePersonType(dto.getResponsiblePersonType());
            }
            unit.setResponsiblePersonName(userName);
            this.save(unit);
        } else {
            unit = getById(dto.getId());
            if (!dto.getName().equals(unit.getName()) && this.count(queryWrapper) > 0) {
                throw new BizException("科室单元名称已存在");
            }
            BeanUtils.copyProperties(dto, unit);
            unit.setResponsiblePersonName(userName);
            this.updateById(unit);
        }
        return unit.getId();
    }

    @Override
    public void switchStatus(BaseIdStatusDTO dto) {
        LambdaUpdateWrapper<KpiAccountUnit> updateWrapper = Wrappers.<KpiAccountUnit>lambdaUpdate()
                .eq(KpiAccountUnit::getId, dto.getId())
                .set(KpiAccountUnit::getStatus, dto.getStatus());
        this.update(updateWrapper);
    }

    @Override
    public void deleteUnit(Long id) {
        int flag = unitCheck(id);
        if (flag > 0) {
            throw new BizException("该单元正在使用中，无法删除");
        }
        this.removeById(id);
    }

    @Override
    public IPage<KpiAccountUnitVO> getUnitPageList(KpiAccountUnitQueryDTO dto) {
        Page<KpiAccountUserDto> matchPage = new Page<>(dto.getCurrent(), dto.getSize());
        IPage<KpiAccountUnitVO> page = kpiAccountUnitMapper.getAccountUnit(matchPage, dto);
//        page = this.page(new Page<>(dto.getCurrent(), dto.getSize()),Wrappers.<KpiAccountUnit>lambdaQuery()
//                        .eq(KpiAccountUnit::getDelFlag, EnableEnum.ENABLE.getType())
//                        .eq(ObjectUtils.isNotEmpty(dto.getStatus()),KpiAccountUnit::getStatus,dto.getStatus())
//                        .eq(ObjectUtils.isNotEmpty(dto.getCategoryCode()),KpiAccountUnit::getCategoryCode,dto.getCategoryCode())
//                        .eq(ObjectUtils.isNotEmpty(dto.getAccountTypeCode()),KpiAccountUnit::getAccountTypeCode,dto.getAccountTypeCode())
//                        .eq(ObjectUtils.isNotEmpty(dto.getBusiType()),KpiAccountUnit::getBusiType,dto.getBusiType())
//                        .eq(ObjectUtils.isNotEmpty(dto.getGroupCode()),KpiAccountUnit::getGroupCode,dto.getGroupCode())
//                        .eq(ObjectUtils.isNotEmpty(dto.getAccountGroup()),KpiAccountUnit::getAccountGroup,dto.getAccountGroup())
//                        .like(ObjectUtils.isNotEmpty(dto.getName()),KpiAccountUnit::getName,dto.getName())
//                        .like(ObjectUtils.isNotEmpty(dto.getResponsiblePersonId()),KpiAccountUnit::getResponsiblePersonId,dto.getResponsiblePersonId())
//                        .like(ObjectUtils.isNotEmpty(dto.getResponsiblePersonName()),KpiAccountUnit::getResponsiblePersonName,dto.getResponsiblePersonName())
//                        .eq(ObjectUtils.isNotEmpty(dto.getDeptType()),KpiAccountUnit::getDeptType,dto.getDeptType())
//                )
//                .convert(KpiAccountUnitVO::changeToVo);
        List<KpiAccountUnitVO> records = page.getRecords();

        Map<String, String> categoryMap = kpiCategoryService.getCodeAndNameMap(CategoryEnum.ACCOUNT_UNIT_GROUP.getType(),null,dto.getBusiType());
        List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
        records.forEach(item -> {
            String userName = getUserName(item.getResponsiblePersonId(), var1);
            item.setResponsiblePersonName(userName);
            item.setGroupCodeName(categoryMap.get(item.getGroupCode()));
            if (BUSI_TYPE_2.equals(dto.getBusiType())) {
                item.setAccountTypeName(categoryMap.get(item.getAccountTypeCode()));
            }
        });

        return page;
    }

    @Override
    public List<KpiAccountUnitVO> getUnitList(KpiAccountUnitQueryDTO dto) {
        List<KpiAccountUnit> list = this.list(Wrappers.<KpiAccountUnit>lambdaQuery()
                .eq(KpiAccountUnit::getDelFlag, EnableEnum.ENABLE.getType())
                .eq(ObjectUtils.isNotEmpty(dto.getStatus()),KpiAccountUnit::getStatus,dto.getStatus())
                .eq(ObjectUtils.isNotEmpty(dto.getCategoryCode()),KpiAccountUnit::getCategoryCode,dto.getCategoryCode())
                .eq(ObjectUtils.isNotEmpty(dto.getAccountTypeCode()),KpiAccountUnit::getAccountTypeCode,dto.getAccountTypeCode())
                .eq(ObjectUtils.isNotEmpty(dto.getBusiType()),KpiAccountUnit::getBusiType,dto.getBusiType())
                .eq(ObjectUtils.isNotEmpty(dto.getGroupCode()),KpiAccountUnit::getGroupCode,dto.getGroupCode())
                .eq(ObjectUtils.isNotEmpty(dto.getAccountGroup()),KpiAccountUnit::getGroupCode,dto.getAccountGroup())
                .like(ObjectUtils.isNotEmpty(dto.getResponsiblePersonId()),KpiAccountUnit::getResponsiblePersonId,dto.getResponsiblePersonId())
                .like(ObjectUtils.isNotEmpty(dto.getName()),KpiAccountUnit::getName,dto.getName()));
        List<KpiAccountUnitVO> result = new ArrayList<>(512);

        Map<String, String> categoryMap = kpiCategoryService.getCodeAndNameMap(CategoryEnum.ACCOUNT_UNIT_GROUP.getType(),null,dto.getBusiType());
        List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();

        if ("2".equals(dto.getBusiType()) && "1".equals(dto.getVirtualFilter())){
            //虚拟核算单元
            List<Long> virtualKpiAccountUnitIds = this.getBaseMapper().getVirtualIds();
            list =  Linq.of(list).where(x->!virtualKpiAccountUnitIds.contains(x.getId())).toList();
        }

        for (KpiAccountUnit unit : list) {
            KpiAccountUnitVO vo = KpiAccountUnitVO.changeToVo(unit);
            String userName = getUserName(vo.getResponsiblePersonId(), var1);
            vo.setResponsiblePersonName(userName);
            vo.setGroupCodeName(categoryMap.get(vo.getGroupCode()));
            if (BUSI_TYPE_2.equals(unit.getBusiType())) {
                vo.setAccountTypeName(categoryMap.get(vo.getAccountTypeCode()));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public Integer unitCheck(Long id) {
        // todo 是否被各处所应用到
        return 0;
    }

    @Override
    public KpiAccountUnitVO getUnit(Long id) {
        KpiAccountUnitVO vo = new KpiAccountUnitVO();
        KpiAccountUnit unit = getById(id);
        if (unit != null) {
            vo = KpiAccountUnitVO.changeToVo(unit);
            List<UserCoreVo> var1 = remoteUserService.listMainDetails(SecurityConstants.FROM_IN).getData();
            String userName = getUserName(vo.getResponsiblePersonId(), var1);
            vo.setResponsiblePersonName(userName);

            Map<String, String> categoryMap = kpiCategoryService.getCodeAndNameMap(CategoryEnum.ACCOUNT_UNIT_GROUP.getType(),null,unit.getBusiType());
            vo.setGroupCodeName(categoryMap.get(vo.getGroupCode()));
            if (BUSI_TYPE_2.equals(unit.getBusiType())) {
                vo.setAccountTypeName(categoryMap.get(vo.getAccountTypeCode()));
            }
        }
        return vo;
    }

    @Override
    public Map<Long, String> getUnitMap(String busiType) {
        LambdaQueryWrapper<KpiAccountUnit> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasLength(busiType)) {
            wrapper.eq(KpiAccountUnit::getBusiType, busiType);
        }
        List<KpiAccountUnit> list = list(wrapper);
        return list.stream().collect(Collectors.toMap(KpiAccountUnit::getId, KpiAccountUnit::getName));
    }

    @Override
    public Map<String, Long> getUnitMapV2(String busiType) {
        if (!StringUtils.hasLength(busiType)) {
            busiType = EnableEnum.DISABLE.getType();
        }
        List<KpiAccountUnit> list = list(Wrappers.<KpiAccountUnit>lambdaQuery().eq(KpiAccountUnit::getBusiType, busiType));
        return list.stream().collect(Collectors.toMap(KpiAccountUnit::getName, KpiAccountUnit::getId, (v1, v2) -> v2));
    }

    public static String getUserName(String userIds, List<UserCoreVo> userList) {
        if (!StringUtils.hasLength(userIds) || CollectionUtils.isEmpty(userList)) {
            return "";
        }
        Map<Long, String> userIdToUsernameMap = userList.stream().collect(Collectors.toMap(UserCoreVo::getUserId,UserCoreVo::getName));
        StringBuilder userName = new StringBuilder();
        String[] userIdArr = userIds.split(",");
        for (String userId : userIdArr) {
            String username = userIdToUsernameMap.get(Long.valueOf(userId));
            if (username != null) {
                userName.append(username).append(",");
            }
        }
        if (userName.length() > 0) {
            return userName.substring(0, userName.length() - 1);
        } else {
            return "";
        }
    }

    @Override
    public void saveAccountRelation(KpiAccountRelationDTO dto) {
        LambdaQueryWrapper<KpiMember> queryWrapper = Wrappers.<KpiMember>lambdaQuery()
                .eq(KpiMember::getMemberType,MemberEnum.ACCOUNT_UNIT_RELATION.getType())
                .eq(KpiMember::getHostCode,dto.getCategoryCode())
                .eq(KpiMember::getHostId,dto.getDocAccountId());
        kpiMemberService.remove(queryWrapper);

        if (StringUtils.hasLength(dto.getNurseAccountId())) {
            List<String> var1 = Arrays.stream(dto.getNurseAccountId().split(",")).distinct().collect(Collectors.toList());
            List<KpiMember> list = new ArrayList<>(64);
            for (String s : var1) {
                KpiMember member = new KpiMember();
                member.setPeriod(0L);
                member.setHostId(dto.getDocAccountId());
                member.setHostCode(dto.getCategoryCode());
                member.setMemberId(Long.valueOf(s));
                member.setMemberType(MemberEnum.ACCOUNT_UNIT_RELATION.getType());
                member.setCreatedDate(new Date());
                member.setTenantId(SecurityUtils.getUser().getTenantId());
                list.add(member);
            }
            kpiMemberService.insertBatchSomeColumn(list);
        }/* else {
            KpiMember member = new KpiMember();
            member.setPeriod(0L);
            member.setHostId(dto.getDocAccountId());
            member.setHostCode(dto.getCategoryCode());
            member.setMemberType(MemberEnum.ACCOUNT_UNIT_RELATION.getType());
            member.setCreatedDate(new Date());
            member.setTenantId(SecurityUtils.getUser().getTenantId());
            kpiMemberService.save(member);
        }*/
    }

    @Override
    public IPage<KpiAccountRelationVO> getAccountRelationPageList(KpiAccountRelationQueryDTO dto) {
        IPage<Long> page = this.getBaseMapper().getAccountRelationIdList(new Page<>(dto.getCurrent(),dto.getSize()),
                dto,
                DOCTOR_ACCOUNT_UNIT,
                MemberEnum.ACCOUNT_UNIT_RELATION.getType());
        IPage<KpiAccountRelationVO> page2 = new Page<>(dto.getCurrent(),dto.getSize());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            List<Long> ids = page.getRecords();
            List<KpiAccountRelationVO> var1 = this.getBaseMapper().getAccountRelationPageList(ids,dto,
                    DOCTOR_ACCOUNT_UNIT,
                    MemberEnum.ACCOUNT_UNIT_RELATION.getType());
            var1.forEach(s -> {
                if (null == s.getNurseAccountList()) {
                    s.setNurseAccountList(new ArrayList<>());
                }
            });
            page2.setPages(page.getPages());
            page2.setTotal(page.getTotal());
            page2.setSize(page.getSize());
            page2.setCurrent(page.getCurrent());
            page2.setRecords(var1);
        }
        return page2;
    }

    @Override
    public List<KpiAccountRelationVO> getAccountRelationPageList2(KpiAccountRelationQueryDTO dto) {
        IPage<Long> page = this.getBaseMapper().getAccountRelationIdList(new Page<>(dto.getCurrent(),9999,false),
                dto,
                DOCTOR_ACCOUNT_UNIT,
                MemberEnum.ACCOUNT_UNIT_RELATION.getType());
        if (!CollectionUtils.isEmpty(page.getRecords())) {
            List<Long> ids = page.getRecords();
            List<KpiAccountRelationVO> var1 = this.getBaseMapper().getAccountRelationPageList(ids,dto,
                    DOCTOR_ACCOUNT_UNIT,
                    MemberEnum.ACCOUNT_UNIT_RELATION.getType());
            var1.forEach(s -> {
                long max = -1L;
                if (s.getNurseAccountList().isEmpty()) {
                    s.setNurseAccountList(new ArrayList<>());
                }else {
                    max = Linq.of(s.getNurseAccountList()).select(x->x.getStatus()).max();
                }
                s.setStatus(max);
            });
            var1 = Linq.of(var1).orderByDescending(x->x.getStatus()).toList();
            return var1;
        }
        return new ArrayList<>();
    }

    @Override
    public void accountRelationCopy(Long id) {
        KpiCategory category = kpiCategoryService.getById(id);
        if (null == category || EnableEnum.DISABLE.getType().equals(category.getDelFlag())) {
            throw new BizException("未找到该分类信息");
        }
        KpiCategoryDto var1 = new KpiCategoryDto();
        BeanUtils.copyProperties(category,var1);
        var1.setId(null);
        var1.setCategoryCode(null);
        var1.setCategoryName(category.getCategoryName() + "-2");
        Long newId = kpiCategoryService.saveOrUpdateGroup(var1);
        KpiCategory newCategory = kpiCategoryService.getById(newId);

        LambdaQueryWrapper<KpiMember> queryWrapper = Wrappers.<KpiMember>lambdaQuery()
                .eq(KpiMember::getMemberType,MemberEnum.ACCOUNT_UNIT_RELATION.getType())
                .eq(KpiMember::getHostCode,category.getCategoryCode())
                .eq(KpiMember::getBusiType, category.getBusiType());
        List<KpiMember> memberList = kpiMemberService.list(queryWrapper);
        if (!CollectionUtils.isEmpty(memberList)) {
            List<KpiMember> list = new ArrayList<>(512);
            for (KpiMember member : memberList) {
                member.setId(null);
                member.setHostCode(newCategory.getCategoryCode());
                member.setCreatedDate(new Date());
                list.add(member);
            }
            kpiMemberService.insertBatchSomeColumn(list);
        }
    }

    @Override
    public String accountRelationImport(String categoryCode,String busiType, MultipartFile file) {
        if (null == file || file.isEmpty() || !StringUtils.hasLength(categoryCode)) {
            throw new BizException("分组或上传文件为空");
        }
        List<KpiAccountUnit> docAccountList = this.list(Wrappers.<KpiAccountUnit>lambdaQuery().eq(KpiAccountUnit::getCategoryCode, DOCTOR_ACCOUNT_UNIT)
                .eq(ObjectUtils.isNotEmpty(busiType),KpiAccountUnit::getBusiType,busiType));
        List<KpiAccountUnit> nurseAccountList = this.list(Wrappers.<KpiAccountUnit>lambdaQuery().eq(KpiAccountUnit::getCategoryCode, NURSE_ACCOUNT_UNIT)
                .eq(ObjectUtils.isNotEmpty(busiType),KpiAccountUnit::getBusiType,busiType));
        KpiUnitRelationListener listener = new KpiUnitRelationListener(this, docAccountList, nurseAccountList, categoryCode);
        try {
            EasyExcel.read(file.getInputStream(), KpiUnitRelationImportDTO.class, listener).headRowNumber(1).sheet(0).doRead();
        } catch (Exception e) {
            log.error("excel解析失败",e);
            throw new BizException("excel解析失败");
        }
        return listener.errorArray.toString();
    }

    @Override
    public void accountRelationMatch(String categoryCode,String busiType) {
        KpiAccountRelationQueryDTO queryDTO = new KpiAccountRelationQueryDTO();
        queryDTO.setCategoryCode(categoryCode);
        queryDTO.setCurrent(1L);
        queryDTO.setSize(9999L);
        IPage<KpiAccountRelationVO> page = getAccountRelationPageList(queryDTO);
        if (CollectionUtils.isEmpty(page.getRecords())) {
            return;
        }
        List<KpiAccountRelationVO> list = page.getRecords();
        List<KpiAccountUnit> nurseAccountList = this.list(Wrappers.<KpiAccountUnit>lambdaQuery().eq(KpiAccountUnit::getCategoryCode, NURSE_ACCOUNT_UNIT)
                .eq(ObjectUtils.isNotEmpty(busiType),KpiAccountUnit::getBusiType,busiType));
        Map<Long,String> map = new HashMap<>(512);
        for (KpiAccountRelationVO relationVO : list) {
            if (!CollectionUtils.isEmpty(relationVO.getNurseAccountList())) {
                continue;
            }
            String name = relationVO.getDocAccountName() + WARD_ACCOUNT_UNIT;
            KpiAccountUnit nurseAccount = nurseAccountList.stream().filter(item -> item.getName().equals(name)).findFirst().orElse(null);
            if (null == nurseAccount) {
                continue;
            }
            String value = map.get(relationVO.getDocAccountId());
            if (StringUtils.hasLength(value)) {
                value = value + "," + nurseAccount.getId();
                map.put(relationVO.getDocAccountId(), value);
            } else {
                map.put(relationVO.getDocAccountId(), nurseAccount.getId().toString());
            }
        }
        if (map.size() > 0) {
            for (Long key : map.keySet()) {
                KpiAccountRelationDTO dto = new KpiAccountRelationDTO();
                dto.setCategoryCode(categoryCode);
                dto.setDocAccountId(key);
                dto.setNurseAccountId(map.get(key));
                saveAccountRelation(dto);
            }
        }
    }

    @Override
    public List<String> importData(List<Map<Integer, String>> list, String overwriteFlag) {
        List<String> rt = new ArrayList<>();
        //overwriteFlag1覆盖导入(清空后插) 2增量导入
        //核算分组-kpi_calculate_grouping  科室单元类型-kpi_unit_calc_type 科室单元人员类型-user_type
        List<SysDictItem> dicts = kpiCalculateMapper.getDicts2(SecurityUtils.getUser().getTenantId());
        List<SysUser> users = kpiCalculateMapper.getUsers(SecurityUtils.getUser().getTenantId());
        List<KpiAccountUnit> units = this.list();

        Map<Integer, String> top = list.get(0);
        int count = 0;
        for (int i = 1; i < list.size(); i++) {
            KpiAccountUnitVO entity = new KpiAccountUnitVO();
            int finalI = i;
            top.forEach((key, value) -> {
                if ("科室单元".equals(value)) {
                    entity.setName(list.get(finalI).get(key));
                } else if ("核算分组".equals(value)) {
                    entity.setCategoryName(list.get(finalI).get(key));
                } else if ("科室单元类型".equals(value)) {
                    entity.setAccountTypeName(list.get(finalI).get(key));
                } else if ("科室单元人员类型".equals(value)) {
                    entity.setAccountUserName(list.get(finalI).get(key));
                } else if ("编码".equals(value)) {
                    entity.setThirdCode(list.get(finalI).get(key));
                } else if ("负责人".equals(value)) {
                    entity.setResponsiblePersonName(list.get(finalI).get(key));
                } else if ("科室系数".equals(value)) {
                    if (!StringUtil.isNullOrEmpty(list.get(finalI).get(key)) && NumberUtil.isNumber(list.get(finalI).get(key))) {
                        entity.setFactor(new BigDecimal(list.get(finalI).get(key)));
                    }
                }
            });
            if (!StringUtil.isNullOrEmpty(entity.getName())) {
                if (!StringUtil.isNullOrEmpty(entity.getCategoryName())) {
                    SysDictItem dict = Linq.of(dicts).firstOrDefault(x -> "kpi_calculate_grouping".equals(x.getDictType()) && entity.getCategoryName().equals(x.getLabel()));
                    if (dict == null) {
                        rt.add("第" + i + "行  " + "核算分组" + entity.getCategoryName() + "不存在!");
                        continue;
                    } else {
                        entity.setCategoryCode(dict.getItemValue());
                    }
                }
                if (!StringUtil.isNullOrEmpty(entity.getAccountTypeName())) {
                    SysDictItem dict = Linq.of(dicts).firstOrDefault(x -> "kpi_unit_calc_type".equals(x.getDictType()) && entity.getAccountTypeName().equals(x.getLabel()));
                    if (dict == null) {
                        rt.add("第" + i + "行  " + "科室单元类型" + entity.getAccountTypeName() + "不存在!");
                        continue;
                    } else {
                        entity.setAccountTypeCode(dict.getItemValue());
                    }
                }
                if (!StringUtil.isNullOrEmpty(entity.getAccountUserName())) {
                    SysDictItem dict = Linq.of(dicts).firstOrDefault(x -> "user_type".equals(x.getDictType()) && entity.getAccountUserName().equals(x.getLabel()));
                    if (dict == null) {
                        rt.add("第" + i + "行  " + "科室单元人员类型" + entity.getAccountUserName() + "不存在!");
                        continue;
                    } else {
                        entity.setAccountUserCode(dict.getItemValue());
                    }
                }
                if (!StringUtil.isNullOrEmpty(entity.getResponsiblePersonName())) {
                    String name = entity.getResponsiblePersonName();
                    List<String> li = Arrays.asList(entity.getResponsiblePersonName().split(","));
                    boolean flag = false;
                    for (String l : li) {
                        SysUser user = Linq.of(users).firstOrDefault(x -> l.equals(x.getName()));
                        if (user == null) {
                            rt.add("第" + i + "行  " + "负责人" + l + "不存在!");
                            flag = true;
                            break;
                        } else {
                            name = name.replace(l, user.getUserId().toString());
                        }
                    }
                    if (flag) {
                        continue;
                    }
                    entity.setResponsiblePersonId(name);
                }
                if ("2".equals(overwriteFlag)) {
                    KpiAccountUnit t = new KpiAccountUnit();
                    BeanUtils.copyProperties(entity, t);
                    t.setBusiType("1");

                    LambdaQueryWrapper<KpiAccountUnit> queryWrapper = Wrappers.<KpiAccountUnit>lambdaQuery()
                            .eq(KpiAccountUnit::getName, t.getName())
                            .eq(KpiAccountUnit::getBusiType, t.getBusiType());
                    if (this.count(queryWrapper) > 0) {
                        rt.add("第" + i + "行  " + "科室单元" + t.getName() + "名称已存在");
                        continue;
                    }
                    this.saveOrUpdate(t);
                } else {
                    KpiAccountUnit unit = Linq.of(units).firstOrDefault(x -> x.getName().equals(entity.getName()));
                    if (unit != null) {
                        if (!unit.getName().equals(entity.getName())) {
                            LambdaQueryWrapper<KpiAccountUnit> queryWrapper = Wrappers.<KpiAccountUnit>lambdaQuery()
                                    .eq(KpiAccountUnit::getName, entity.getName())
                                    .eq(KpiAccountUnit::getBusiType, "1");
                            if (this.count(queryWrapper) > 0) {
                                rt.add("第" + i + "行  " + "科室单元" + entity.getName() + "名称已存在");
                                continue;
                            }
                        }
                        BeanUtils.copyProperties(entity, unit, "id");
                        this.updateById(unit);
                    }else{
                        KpiAccountUnit t = new KpiAccountUnit();
                        BeanUtils.copyProperties(entity,t);
                        t.setBusiType("1");

                        LambdaQueryWrapper<KpiAccountUnit> queryWrapper = Wrappers.<KpiAccountUnit>lambdaQuery()
                                .eq(KpiAccountUnit::getName, t.getName())
                                .eq(KpiAccountUnit::getBusiType, t.getBusiType());
                        if (this.count(queryWrapper) > 0) {
                            rt.add("第"+i+"行  "+"科室单元"+t.getName()+"名称已存在");
                            continue;
                        }
                        this.saveOrUpdate(t);
                    }
                }
                count++;
            }
        }
        rt.add(0, "成功添加/修改" + count + "条数据");
        return rt;
    }
}
