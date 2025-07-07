package com.hscloud.hs.cost.account.service.impl.imputation;


import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.ObjectUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CostUnitRelateInfoTypeConstants;
import com.hscloud.hs.cost.account.constant.DeptOrUserConstant;
import com.hscloud.hs.cost.account.constant.ImputaionProperties;
import com.hscloud.hs.cost.account.constant.enums.imputation.ImputationType;
import com.hscloud.hs.cost.account.mapper.imputation.ImputationDeptUnitMapper;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDTO;
import com.hscloud.hs.cost.account.model.dto.imputation.ImputationDeptUnitDelDTO;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.entity.CostUnitRelateInfo;
import com.hscloud.hs.cost.account.model.entity.imputation.*;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;
import com.hscloud.hs.cost.account.model.vo.imputation.CostAccountUnitVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDeptUnitVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationDetailsVO;
import com.hscloud.hs.cost.account.model.vo.imputation.ImputationMatchDeptUnitVO;
import com.hscloud.hs.cost.account.service.CostAccountUnitService;
import com.hscloud.hs.cost.account.service.ICostUnitRelateInfoService;
import com.hscloud.hs.cost.account.service.imputation.*;
import com.hscloud.hs.cost.account.service.second.IAttendanceService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.data.monitor.enums.DataOpEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 归集科室单元 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImputationDeptUnitService extends ServiceImpl<ImputationDeptUnitMapper, ImputationDeptUnit> implements IImputationDeptUnitService {

    private final CostAccountUnitService costAccountUnitService;
    private final ImputaionProperties imputaionProperties;
    private final IAttendanceService attendanceService;
    private final IImputationDetailsService imputationDetailsService;
    private final IImputationIndexService imputationIndexService;
    private final IImputationIndexDetailsService imputationIndexDetailsService;
    private final ISpecialImputationPersonService specialImputationPersonService;
    private final INonIncomePersonService nonIncomePersonService;
    private final IPersonChangeService personChangeService;
    private final ICostUnitRelateInfoService costUnitRelateInfoService;
    @Lazy
    @Autowired
    private IImputationService imputationService;

    @Resource
    private RemoteUserService remoteUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void generateLastMonth(String currentCycle) {
        //获取上一周期
        String lastCycle = attendanceService.getLastCycle(currentCycle);
        if (lastCycle == null) {
            throw new BizException("未获取到上一周期");
        }
        //清空最新周期的数据
        this.deleteByCycle(currentCycle);

        //复制上个周期的数据(deptUnit)
        this.copyLastMonth(currentCycle, lastCycle);

        //设置人员
        this.setPersonByCycle(currentCycle);

        //设置标记
        this.update(Wrappers.<ImputationDeptUnit>lambdaUpdate()
                .set(ImputationDeptUnit::getIfLastMonth, "1")
                .eq(ImputationDeptUnit::getImputationCycle, currentCycle));

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setPersonByCycle(String currentCycle) {
        //工作量设置人员
        this.setWorkPerson(currentCycle);

        //收入设置人员
        this.setIncomePerson(currentCycle);

    }

    @Override
    public List<Attendance> getAttendanceList(Imputation imputation, Long accountUnitId, String accountUnitName, String accountGroupCode, ImputationIndex index, String type) {
        List<Attendance> rtnList = new ArrayList<>();
        if (imputation == null || index == null) {
            return rtnList;
        }
        String cycle = imputation.getImputationCycle();
        Long imputationId = imputation.getId();
        Long indexId = index.getId();
        List<Attendance> attendanceList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, cycle)
                .eq(accountUnitId != null, Attendance::getAccountUnitId, String.valueOf(accountUnitId)));
        String imputationCode = imputation.getImputationCode();
        //工作量人员
        if (Objects.equals(imputationCode, ImputationType.WORKLOAD_DATA_IMPUTATION.toString())) {
            String xzGroupName = imputaionProperties.getXzGroupName();
            String xzUnitName = imputaionProperties.getXzUnitName();
            //"行政科室”：行政中高层
            if (Objects.equals(xzUnitName, accountUnitName)) {
                //行政中高层
                List<ImputationDeptUnit> deptUnitList = this.listByZGC(imputation.getImputationCycle());
                return deptUnitList.stream().map(deptUnit -> new Attendance().setUserId(deptUnit.getUserId()).setEmpName(deptUnit.getUserName())).collect(Collectors.toList());
            }
            //非行政组科室单元
            if (!accountGroupCode.contains(xzGroupName)) {
                return attendanceList;
            }
            //行政科室单元：考勤表人员 - 行政中高层
            else {
                //行政中高层
                List<ImputationDeptUnit> deptUnitList = this.listByZGC(imputation.getImputationCycle());
                return attendanceList.stream()
                        .filter(attendance -> deptUnitList.stream()
                                .noneMatch(deptUnit -> Objects.equals(deptUnit.getUserId(), attendance.getUserId())))
                        .collect(Collectors.toList());
            }
        }
        //收入
        else if (Objects.equals(imputationCode, ImputationType.INCOME_DATA_IMPUTATION.toString())) {
            String yzmzUnitName = imputaionProperties.getYzmzUnitName();
            String yzmzGroupName = imputaionProperties.getYzmzGroupName();
            String yzmzIndexName = imputaionProperties.getYzmzIndexName();
            List<Attendance> allTsAttendanceList = specialImputationPersonService.listAllByPId(imputationId, indexId);
            //需要加入的特殊人员
            List<Attendance> addTsAttendanceList = specialImputationPersonService.listByPId(imputationId, accountUnitId, indexId, null);
            //不计人员
            List<Attendance> bjAttendanceList = nonIncomePersonService.listByPId(imputationId, indexId, null);
            //鄞州门诊 科室单元
            if (Objects.equals(yzmzUnitName, accountUnitName) && accountGroupCode.contains(yzmzGroupName)) {
                //鄞州门诊收入归集人员
                if (Objects.equals(yzmzIndexName, index.getName())) {
                    //获取非独立科室下的考勤人员
                    List<Attendance> attendances = attendanceService.listByTypeDept(cycle);
                    rtnList.addAll(attendances);
                    removeBj(allTsAttendanceList, rtnList);
                    rtnList.addAll(addTsAttendanceList);
                    removeBj(bjAttendanceList, rtnList);
                    return rtnList;
                }
            }
            //其他
            List<Attendance> attendances = Objects.equals(type, CostUnitRelateInfoTypeConstants.DEPT) && Objects.equals(yzmzIndexName, index.getName())
                    ? Collections.emptyList() : attendanceService.listByAccountUnitId(cycle, accountUnitId);
            rtnList.addAll(attendances);
            removeBj(allTsAttendanceList, rtnList);
            rtnList.addAll(addTsAttendanceList);
            removeBj(bjAttendanceList, rtnList);
            return rtnList;
        }

        return rtnList;
    }

    private List<ImputationDeptUnit> listByZGC(String imputationCycle) {
        return this.list(Wrappers.<ImputationDeptUnit>lambdaQuery()
                .eq(ImputationDeptUnit::getImputationCycle, imputationCycle)
                .eq(ImputationDeptUnit::getImputationCode, ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION));
    }


    @Override
    public IPage<CostAccountUnitVO> pageUnmatched(Page page, Long imputationId, String accountUnitName, String accountGroupCode) {
        List<ImputationDeptUnit> imputationDeptUnits = list(Wrappers.<ImputationDeptUnit>lambdaQuery().eq(ImputationDeptUnit::getImputationId, imputationId));

        Optional<List<ImputationDeptUnit>> optionalImputationDeptUnits = Optional.ofNullable(imputationDeptUnits);

        List<Long> accountUnitIds = optionalImputationDeptUnits.map(list ->
                        list.stream().map(ImputationDeptUnit::getAccountUnitId).filter(accountUnitId -> !Objects.isNull(accountUnitId)).collect(Collectors.toList()))
                .orElse(Collections.emptyList());

        LambdaQueryWrapper<CostAccountUnit> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(accountUnitName)) {
            wrapper = wrapper.like(CostAccountUnit::getName, accountUnitName);
        }
        if (StringUtils.isNotBlank(accountGroupCode)) {
            wrapper = wrapper.eq(CostAccountUnit::getAccountGroupCode, accountGroupCode);
        }
        wrapper = CollectionUtils.isEmpty(accountUnitIds) ? wrapper : wrapper.notIn(CostAccountUnit::getId, accountUnitIds);
        Page<CostAccountUnit> costAccountUnitPage = costAccountUnitService.page(page, wrapper);
        return costAccountUnitPage.convert(CostAccountUnitVO::build);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImputationMatchDeptUnitVO match(ImputationDeptUnitDTO imputationDeptUnitDTO) {
        ImputationMatchDeptUnitVO matchDeptUnitVO = new ImputationMatchDeptUnitVO();
        Long accountUnitId = imputationDeptUnitDTO.getAccountUnitId();
        List<CostUnitRelateInfo> costUnitRelateInfos = costUnitRelateInfoService.listByAccountUnitIds(Collections.singletonList(accountUnitId));
        if (CollectionUtils.isEmpty(costUnitRelateInfos)) {
            throw new BizException("当前核算单元未关联科室人员！");
        }
        //生成人员信息
        Imputation imputation = new Imputation();
        BeanUtils.copyProperties(imputationDeptUnitDTO, imputation);
        imputation.setId(imputationDeptUnitDTO.getImputationId());
        imputationService.setImputation(imputation, imputationDeptUnitDTO.getImputationId());
        List<ImputationIndex> imputationIndices = imputationIndexService.listByPId(imputationDeptUnitDTO.getImputationId());
        List<ImputationDetails> imputationDetails = Optional.ofNullable(imputationIndices).map(list -> list.stream().map(item -> {
            List<Attendance> attendanceList = getAttendanceList(imputation, accountUnitId, imputationDeptUnitDTO.getAccountUnitName(), imputationDeptUnitDTO.getAccountGroupCode(), item, costUnitRelateInfos.get(0).getType());
            //过滤掉null数据
            List<Attendance> result = filterNullUserAttendance(attendanceList);
            ImputationDetails details = new ImputationDetails();
            details.setImputationIndexId(item.getId());
            details.setImputationIndexName(item.getName());
            if (!result.isEmpty()) {
                details.setEmpNames(result.stream().map(Attendance::getEmpName).collect(Collectors.joining(",")));
                details.setUserIds(result.stream().map(attendance -> attendance.getUserId() + "").collect(Collectors.joining(",")));
            }
            return details;
        }).collect(Collectors.toList())).orElse(Collections.emptyList());

        //返回人员明细信息
        List<ImputationDetailsVO> imputationDetailsVOs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(imputationDetails)) {
            fillImputationDetails(imputationDetailsVOs, imputationDetails);
        }
        matchDeptUnitVO.setImputationDetailsVOs(imputationDetailsVOs);
        return matchDeptUnitVO;
    }


    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean addImputationDeptUnit(ImputationDeptUnitDTO imputationDeptUnitDTO) {
        imputationService.setImputation(imputationDeptUnitDTO, imputationDeptUnitDTO.getImputationId());
        fillDeptAndUser(imputationDeptUnitDTO);
        //校验数据，去重
        checkData(imputationDeptUnitDTO);

        ImputationDeptUnit imputationDeptUnit = new ImputationDeptUnit();
        BeanUtils.copyProperties(imputationDeptUnitDTO, imputationDeptUnit);
        imputationDeptUnit.setId(null);
        save(imputationDeptUnit);

        //生成人员信息
        List<ImputationDetails> imputationDetails = Optional.ofNullable(imputationDeptUnitDTO.getImputationDetails()).map(list -> list.stream().map(item -> {
            ImputationDetails details = new ImputationDetails();
            BeanUtils.copyProperties(item, details);
            imputationService.setImputation(details, imputationDeptUnitDTO.getImputationId());
            details.setImputationId(imputationDeptUnitDTO.getImputationId());
            details.setImputationDeptUnitId(imputationDeptUnit.getId());
            details.setAccountUnitId(imputationDeptUnitDTO.getAccountUnitId());
            details.setAccountUnitName(imputationDeptUnitDTO.getAccountUnitName());
            if (Objects.nonNull(item.getLeaderUser())) {
                details.setUserIds(CommonUtils.getValueFromUserObj(item.getLeaderUser(), "id", DeptOrUserConstant.USER_LIST));
                details.setEmpNames(CommonUtils.getValueFromUserObj(item.getLeaderUser(), "name", DeptOrUserConstant.USER_LIST));
            }
            return details;
        }).collect(Collectors.toList())).orElse(Collections.emptyList());

        //记录人员变更记录
        personChangeService.savePersonChange(imputationDeptUnitDTO.getImputationDetails(), match(imputationDeptUnitDTO).getImputationDetailsVOs(), imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getAccountUnitId());
        if (CollectionUtils.isNotEmpty(imputationDetails)) {
            imputationDetailsService.saveBatch(imputationDetails);

        }
        return true;
    }

    private void fillDeptAndUser(ImputationDeptUnitDTO imputationDeptUnitDTO) {
        Map<String, Object> leaderDept = imputationDeptUnitDTO.getLeaderDept();
        Map<String, Object> leaderUser = imputationDeptUnitDTO.getLeaderUser();
        if (MapUtil.isNotEmpty(leaderDept)) {
            imputationDeptUnitDTO.setAccountUnitId(Long.valueOf(Objects.requireNonNull(CommonUtils.getValueFromUserObj(leaderDept, "id", DeptOrUserConstant.DEPT_LIST))));
            imputationDeptUnitDTO.setAccountUnitName(CommonUtils.getValueFromUserObj(leaderDept, "name", DeptOrUserConstant.DEPT_LIST));
        }
        if (MapUtil.isNotEmpty(leaderUser)) {
            imputationDeptUnitDTO.setUserId(Long.valueOf(Objects.requireNonNull(CommonUtils.getValueFromUserObj(leaderUser, "id", DeptOrUserConstant.USER_LIST))));
            imputationDeptUnitDTO.setUserName(CommonUtils.getValueFromUserObj(leaderUser, "name", DeptOrUserConstant.USER_LIST));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean updateImputationDeptUnit(ImputationDeptUnitDTO imputationDeptUnitDTO) {
        log.info("编辑科室单元入参：{}", imputationDeptUnitDTO);
        imputationService.setImputation(imputationDeptUnitDTO, imputationDeptUnitDTO.getImputationId());
        //工作量数据归集和收入数据归集编辑人员
        if (Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.WORKLOAD_DATA_IMPUTATION.toString())
                || Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.INCOME_DATA_IMPUTATION.toString())) {
            Optional<List<ImputationDetailsVO>> imputationDetailsDTOList = Optional.ofNullable(imputationDeptUnitDTO.getImputationDetails());

            List<ImputationDetails> imputationDetailsList = imputationDetailsDTOList.map(list ->
                    list.stream().map(
                            details -> {
                                ImputationDetails imputationDetails = new ImputationDetails();
                                BeanUtils.copyProperties(details, imputationDetails);
                                imputationService.setImputation(imputationDetails, imputationDeptUnitDTO.getImputationId());
                                imputationDetails.setImputationDeptUnitId(imputationDeptUnitDTO.getId());
                                if (MapUtil.isNotEmpty(details.getLeaderUser())) {
                                    //imputationDetails.setUserIds("," + CommonUtils.getValueFromUserObj(details.getLeaderUser(), "id", DeptOrUserConstant.USER_LIST) + ",");
                                    imputationDetails.setUserIds(CommonUtils.getValueFromUserObj(details.getLeaderUser(), "id", DeptOrUserConstant.USER_LIST));
                                    imputationDetails.setEmpNames(CommonUtils.getValueFromUserObj(details.getLeaderUser(), "name", DeptOrUserConstant.USER_LIST));
                                }
                                return imputationDetails;
                            }
                    ).collect(Collectors.toList())).orElse(Collections.emptyList());

            //记录变更
            personChangeService.savePersonChange(imputationDeptUnitDTO.getImputationDetails(), imputationDeptUnitDTO.getImputationDetailsVOs(), imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getAccountUnitId());
            if (CollectionUtils.isNotEmpty(imputationDetailsList)) {
                imputationDetailsService.updateBatchById(imputationDetailsList);
            }
        } else {
            checkData(imputationDeptUnitDTO);
            ImputationDeptUnit imputationDeptUnit = new ImputationDeptUnit();
            BeanUtils.copyProperties(imputationDeptUnitDTO, imputationDeptUnit);
            updateById(imputationDeptUnit);
        }

        return true;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean removeImputationDeptUnit(ImputationDeptUnitDelDTO imputationDeptUnitDelDTO) {
        log.info("归集管理删除，入参：{}", imputationDeptUnitDelDTO);

        if (CollectionUtils.isNotEmpty(imputationDeptUnitDelDTO.getImputationDetailsIds())) {
            imputationDetailsService.removeByIds(imputationDeptUnitDelDTO.getImputationDetailsIds());
        }
        return removeById(imputationDeptUnitDelDTO.getImputationDeptUnitId());
    }

    @Override
    public IPage<ImputationDeptUnitVO> pageImputationDeptUnit(Page<ImputationDeptUnit> page, QueryWrapper<ImputationDeptUnit> wrapper, Long imputationId) {
        log.info("分页查询归集管理，主档ID入参：{}", imputationId);
        Page<ImputationDeptUnit> deptUnitPage = page(page, wrapper.eq("imputation_id", imputationId));
        IPage<ImputationDeptUnitVO> imputationDeptUnitVOIPage = deptUnitPage.convert(ImputationDeptUnitVO::builder);
        List<ImputationDeptUnitVO> records = imputationDeptUnitVOIPage.getRecords();
        log.info("分页查询结果{}", records);
        if (CollectionUtils.isNotEmpty(records)) {
            List<Long> imputationDeptUnitIds = records.stream().map(ImputationDeptUnitVO::getId).collect(Collectors.toList());
            List<ImputationDetails> imputationDetailsList = imputationDetailsService.list(Wrappers.<ImputationDetails>lambdaQuery().in(ImputationDetails::getImputationDeptUnitId, imputationDeptUnitIds));
            for (ImputationDeptUnitVO imputationDeptUnitVO : records) {
                if (CollectionUtils.isNotEmpty(imputationDetailsList)) {
                    Map<Long, List<ImputationDetails>> longListMap = imputationDetailsList.stream().collect(Collectors.groupingBy(ImputationDetails::getImputationDeptUnitId));
                    List<ImputationDetailsVO> list = new ArrayList<>();
                    List<ImputationDetails> details = longListMap.getOrDefault(imputationDeptUnitVO.getId(), Collections.emptyList());
                    for (ImputationDetails detail : details) {
                        ImputationDetailsVO imputationDetailsVO = new ImputationDetailsVO();
                        BeanUtils.copyProperties(detail, imputationDetailsVO);
                        imputationDetailsVO.setLeaderUser(CommonUtils.getUserObj(detail.getUserIds(), detail.getEmpNames(), DeptOrUserConstant.USER_LIST));
                        list.add(imputationDetailsVO);
                    }
                    imputationDeptUnitVO.setImputationDetailsVOs(list);
                }
                String imputationCode = imputationDeptUnitVO.getImputationCode();
                if (StringUtils.equals(imputationCode, ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION.toString())
                        || StringUtils.equals(imputationCode, ImputationType.ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())
                        || StringUtils.equals(imputationCode, ImputationType.ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    imputationDeptUnitVO.setLeaderDept(CommonUtils.getUserObj(imputationDeptUnitVO.getAccountUnitId() + "", imputationDeptUnitVO.getAccountUnitName(), DeptOrUserConstant.DEPT_LIST));
                    imputationDeptUnitVO.setLeaderUser(CommonUtils.getUserObj(imputationDeptUnitVO.getUserId() + "", imputationDeptUnitVO.getUserName(), DeptOrUserConstant.USER_LIST));
                }
            }


        }
        return imputationDeptUnitVOIPage;
    }

    private void checkData(ImputationDeptUnitDTO imputationDeptUnitDTO) {
        if (Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.ADMIN_CHIEF_NURSE_PERFORMANCE_DATA_IMPUTATION.toString())
                || Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.ADMIN_NON_STAFF_PERFORMANCE_DATA_IMPUTATION.toString())
                || Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.ADMIN_MIDDLE_HIGH_PERFORMANCE_DATA_IMPUTATION.toString())
                || Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.ADMIN_GENERAL_PERFORMANCE_DATA_IMPUTATION.toString())) {
            //根据人员去重
            ImputationDeptUnit imputationDeptUnit = getOne(Wrappers.<ImputationDeptUnit>lambdaQuery()
                    .eq(ImputationDeptUnit::getImputationId, imputationDeptUnitDTO.getImputationId())
                    .eq(ImputationDeptUnit::getUserId, imputationDeptUnitDTO.getUserId()));
            if (ObjectUtils.isNotNull(imputationDeptUnitDTO.getId())) {
                if (ObjectUtils.isNotNull(imputationDeptUnit) && !Objects.equals(imputationDeptUnit.getId(), imputationDeptUnitDTO.getId())) {
                    log.error("更新操作，月份为{}的{}人员{}已经在已匹配列表中", imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getImputationName(), imputationDeptUnitDTO.getAccountUnitName());
                    throw new BizException("此人员已经存在，不可重复！");
                }
            } else {
                if (ObjectUtils.isNotNull(imputationDeptUnit)) {
                    log.error("新增操作，月份为{}的{}人员{}已经在已匹配列表中", imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getImputationName(), imputationDeptUnitDTO.getAccountUnitName());
                    throw new BizException("此人员已经存在，不可重复！");
                }
            }
        } else {
            //根据科室去重
            ImputationDeptUnit imputationDeptUnit = getOne(Wrappers.<ImputationDeptUnit>lambdaQuery()
                    .eq(ImputationDeptUnit::getImputationId, imputationDeptUnitDTO.getImputationId())
                    .eq(ImputationDeptUnit::getAccountUnitId, imputationDeptUnitDTO.getAccountUnitId()));
            if (ObjectUtils.isNotNull(imputationDeptUnitDTO.getId())) {
                if (ObjectUtils.isNotNull(imputationDeptUnit) && !Objects.equals(imputationDeptUnit.getId(), imputationDeptUnitDTO.getId())
                        && !Objects.equals(imputationDeptUnitDTO.getImputationCode(), ImputationType.COST_PERFORMANCE_DATA_IMPUTATION.toString())) {
                    log.error("更新操作，月份为{}的{}科室{}已经在已匹配列表中", imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getImputationName(), imputationDeptUnitDTO.getAccountUnitName());
                    throw new BizException("此科室已经在已匹配列表中，科室不可重复！");
                }
            } else {
                if (ObjectUtils.isNotNull(imputationDeptUnit)) {
                    log.error("新增操作，月份为{}的{}科室{}已经在已匹配列表中", imputationDeptUnitDTO.getImputationCycle(), imputationDeptUnitDTO.getImputationName(), imputationDeptUnitDTO.getAccountUnitName());
                    throw new BizException("此科室已经在已匹配列表中，科室不可重复！");
                }
            }
        }
    }

    private void fillImputationDetails(List<ImputationDetailsVO> imputationDetailsVOs, List<ImputationDetails> imputationDetailsList) {
        for (ImputationDetails details : imputationDetailsList) {
            ImputationDetailsVO imputationDetailsVO = new ImputationDetailsVO();
            BeanUtils.copyProperties(details, imputationDetailsVO);
            Map<String, Object> userObj = CommonUtils.getUserObj(details.getUserIds(), details.getEmpNames(), DeptOrUserConstant.USER_LIST);
            imputationDetailsVO.setLeaderUser(userObj);
            imputationDetailsVOs.add(imputationDetailsVO);
        }
    }

    @Override
    public void setIncomePerson(String currentCycle) {
        //获取归集主档
        Imputation imputation = imputationService.getByType(currentCycle, ImputationType.INCOME_DATA_IMPUTATION);
        Long tenantId = imputation.getTenantId();
        Long imputationId = imputation.getId();
        //获取主档下的科室单元
        List<ImputationDeptUnit> deptUnitList = this.listByPId(imputationId);
        List<Long> accountUnitIds = deptUnitList.stream().map(ImputationDeptUnit::getAccountUnitId).collect(Collectors.toList());
        //获取核算单元关联科室人员
        List<CostUnitRelateInfo> costUnitRelateInfos = costUnitRelateInfoService.listByAccountUnitIds(accountUnitIds);
        Map<Long, String> relateMap = costUnitRelateInfos.stream().collect(Collectors.toMap(CostUnitRelateInfo::getAccountUnitId, CostUnitRelateInfo::getType, (v1, v2) -> v1));
        //获取主档下的归集指标
        List<ImputationIndex> indexList = imputationIndexService.listByPId(imputationId);

        List<ImputationDetails> dbList = imputationDetailsService.listByPId(imputationId);
        //key : deptUnitId+"-"+indexId
        Map<String, ImputationDetails> dbMap = dbList.stream().collect(Collectors.toMap(item -> item.getImputationDeptUnitId() + "_" + item.getImputationIndexId(), Function.identity(), (v1, v2) -> v1));
        List<ImputationDetails> addList = new ArrayList<>();
        List<ImputationDetails> editList = new ArrayList<>();

        //特殊人员
        List<SpecialImputationPerson> specialImputationPersonAllList = specialImputationPersonService.list(Wrappers.<SpecialImputationPerson>lambdaQuery().eq(SpecialImputationPerson::getImputationId, imputationId));
        //统计 每个归集指标下面有几个特殊归集人员
        Map<Long, List<Attendance>> allTsAttendanceMap = calculateTsAttendanceList(specialImputationPersonAllList);
        log.info("每个归集指标下面有多少特殊归集人员：{}", allTsAttendanceMap);
        String yzmzUnitName = imputaionProperties.getYzmzUnitName();
        String yzmzGroupName = imputaionProperties.getYzmzGroupName();
        String yzmzIndexName = imputaionProperties.getYzmzIndexName();

        Map<Long, List<Attendance>> bjMap = calculateBjAttendanceList(imputationId);
        log.info("每个归集指标下面有多少不计收入人员：{}", bjMap);

        List<Attendance> attendanceListByTypeDept = attendanceService.listByTypeDept(currentCycle);
        List<Attendance> allTAttendanceList = attendanceService.list(Wrappers.<Attendance>lambdaQuery().eq(Attendance::getCycle, currentCycle));
        allTAttendanceList.removeIf(item -> Objects.isNull(item.getAccountUnitId()));
        Map<String, List<Attendance>> attendanceMap = allTAttendanceList.stream().collect(Collectors.groupingBy(Attendance::getAccountUnitId));
        log.info("科室单元分组考勤人员：{}", attendanceMap);
        deptUnitList.forEach(deptUnit -> {
            List<Attendance> attendances;
            for (ImputationIndex index : indexList) {

                String accountUnitName = deptUnit.getAccountUnitName();
                String accountGroupCode = deptUnit.getAccountGroupCode();
                Long indexId = index.getId();
                //鄞州门诊 科室单元
                if (Objects.equals(yzmzUnitName, accountUnitName) && accountGroupCode.contains(yzmzGroupName) && Objects.equals(yzmzIndexName, index.getName())) {
                    //鄞州门诊收入归集人员
                    attendances = attendanceListByTypeDept;
                } else {
                    String type = relateMap.get(deptUnit.getAccountUnitId());
                    attendances = Objects.equals(type, CostUnitRelateInfoTypeConstants.DEPT) && Objects.equals(yzmzIndexName, index.getName())
                            ? Collections.emptyList()
                            : attendanceMap.getOrDefault(deptUnit.getAccountUnitId().toString(), new ArrayList<>());
                }

                List<Attendance> tsAttendanceList = specialImputationPersonService.listByPId(imputationId, deptUnit.getAccountUnitId(), indexId, specialImputationPersonAllList);
                List<Attendance> bjAttendanceList = bjMap.get(indexId);
                List<Attendance> attendanceList = this.findAttendanceList(tsAttendanceList, bjAttendanceList, allTsAttendanceMap.get(indexId), attendances);
                attendanceList = filterNullUserAttendance(attendanceList);
                ImputationDetails details = imputationDetailsService.createDetails(imputation, dbMap, deptUnit, index);
                details.setEmpNames(attendanceList.stream().map(Attendance::getEmpName).collect(Collectors.joining(",")));
                details.setUserIds(attendanceList.stream().map(Attendance::getUserId).map(String::valueOf).collect(Collectors.joining(",")));
                details.setTenantId(tenantId);
                if (details.getId() == null) {
                    addList.add(details);
                } else {
                    editList.add(details);
                }

            }
        });

        if (!addList.isEmpty()) {
            log.info("最后的add归集人员为：{}", addList);
            //这个方法会总是新增
            //imputationDetailsService.saveOrUpdateBatchImputationDetails(addOrEditList);
            //根据上月人员变更规则更新人员
            updatePersons(addList, currentCycle);
            imputationDetailsService.saveBatch(addList);
        }
        if (!editList.isEmpty()) {
            log.info("最后的edit归集人员为：{}", editList);
            //这个方法会总是编辑
            //imputationDetailsService.saveOrUpdateBatchImputationDetails(addOrEditList);
            //根据上月人员变更规则更新人员
            updatePersons(editList, currentCycle);
            imputationDetailsService.updateBatchById(editList);
        }

    }

    //统计不同的归集指标下面有哪些人员
    private Map<Long, List<Attendance>> calculateBjAttendanceList(Long imputationId) {
        Map<Long, List<Attendance>> allBjAttendanceList = new HashMap<>();
        List<NonIncomePerson> nonIncomePersonList = nonIncomePersonService.list(Wrappers.<NonIncomePerson>lambdaQuery().eq(NonIncomePerson::getImputationId, imputationId));
        if (CollectionUtils.isEmpty(nonIncomePersonList)) {
            return allBjAttendanceList;
        }

        Set<Long> indexIds = nonIncomePersonList.stream()
                .flatMap(specialImputationPerson -> Stream.of(specialImputationPerson.getImputationIndexIds().split(",")))
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        List<Long> deptIds = nonIncomePersonList.stream()
                .filter(normalImputationPerson -> DeptOrUserConstant.DEPT.equals(normalImputationPerson.getDeptOrUserType())).map(NonIncomePerson::getDeptOrUserId)
                .collect(Collectors.toList());

        Map<Long, List<SysUser>> deptToUsersMap = remoteUserService.listUserListByDeptIdList(deptIds).getData();

        for (Long indexId : indexIds) {
            String indexIdStr = indexId.toString();
            ConcurrentLinkedQueue<Attendance> attendances = new ConcurrentLinkedQueue<>();

            nonIncomePersonList.parallelStream().forEach(nonIncomePerson -> {
                if (nonIncomePerson.getImputationIndexIds().contains(indexIdStr)) {
                    String deptOrUserType = nonIncomePerson.getDeptOrUserType();
                    if (DeptOrUserConstant.USER.equals(deptOrUserType)) {
                        attendances.add(new Attendance()
                                .setEmpName(nonIncomePerson.getDeptOrUserName())
                                .setUserId(nonIncomePerson.getDeptOrUserId()));
                    } else if (DeptOrUserConstant.DEPT.equals(deptOrUserType)) {
                        List<SysUser> users = deptToUsersMap.get(nonIncomePerson.getDeptOrUserId());
                        if (CollectionUtils.isNotEmpty(users)) {
                            String needIncomePersonIds = nonIncomePerson.getNeedIncomePersonIds();
                            if (StringUtils.isNotBlank(needIncomePersonIds)) {
                                users.forEach(user -> {
                                    if (!needIncomePersonIds.contains(user.getUserId().toString())) {
                                        attendances.add(new Attendance()
                                                .setEmpName(user.getName())
                                                .setUserId(user.getUserId()));
                                    }
                                });
                            } else {
                                users.forEach(user -> {
                                    attendances.add(new Attendance()
                                            .setEmpName(user.getName())
                                            .setUserId(user.getUserId()));
                                });
                            }

                        }
                    }
                }
            });

            allBjAttendanceList.put(indexId, new ArrayList<>(attendances));
        }
        return allBjAttendanceList;


    }

    private Map<Long, List<Attendance>> calculateTsAttendanceList(List<SpecialImputationPerson> specialImputationPersonAllList) {
        Map<Long, List<Attendance>> allTsAttendanceMap = new HashMap<>();
        Set<Long> indexIds = specialImputationPersonAllList.stream()
                .flatMap(specialImputationPerson -> Stream.of(specialImputationPerson.getImputationIndexIds().split(",")))
                .map(Long::parseLong)
                .collect(Collectors.toSet());

        for (Long indexId : indexIds) {
            List<Attendance> attendanceList = specialImputationPersonAllList.stream()
                    .filter(specialImputationPerson -> specialImputationPerson.getImputationIndexIds().contains(indexId + ""))
                    .map(item -> new Attendance().setUserId(item.getUserId()).setEmpName(item.getUserName()))
                    .collect(Collectors.toList());
            allTsAttendanceMap.put(indexId, attendanceList);
        }

        return allTsAttendanceMap;
    }

    private List<Attendance> findAttendanceList(List<Attendance> tsAttendanceList, List<Attendance> bjAttendanceList, List<Attendance> allTsAttendanceList, List<Attendance> attendanceList) {
        List<Attendance> rtnList = new ArrayList<>(attendanceList);
        removeBj(allTsAttendanceList, rtnList);
        rtnList.addAll(tsAttendanceList);
        removeBj(bjAttendanceList, rtnList);
        return rtnList;
    }

    private void removeBj(List<Attendance> bjAttendanceList, List<Attendance> rtnList) {
        Optional.ofNullable(bjAttendanceList)
                .map(list -> list.stream()
                        .map(Attendance::getUserId)
                        .collect(Collectors.toList()))
                .ifPresent(finalBjUserIds -> rtnList.removeIf(item -> finalBjUserIds.contains(item.getUserId())));
    }

    @Override
    public void setWorkPerson(String currentCycle) {
        String xzGroupName = imputaionProperties.getXzGroupName();
        String xzUnitName = imputaionProperties.getXzUnitName();

        //获取归集主档
        Imputation imputation = imputationService.getByType(currentCycle, ImputationType.WORKLOAD_DATA_IMPUTATION);
        Long imputationId = imputation.getId();
        //获取主档下的科室单元
        List<ImputationDeptUnit> deptUnitList = this.listByPId(imputationId);
        //获取主档下的归集指标
        List<ImputationIndex> indexList = imputationIndexService.listByPId(imputationId);

        if (indexList.isEmpty()) {
            return;
        }

        //非行政科室单元：从考勤表获得 科室下的人员，塞入到每个归集指标上（实际上这里只会有一个归集指标：归集人员）
        List<ImputationDeptUnit> otherDeptUnitList = deptUnitList.stream().filter(deptUnit -> deptUnit.getAccountGroupCode() != null && !deptUnit.getAccountGroupCode().contains(xzGroupName)).collect(Collectors.toList());
        List<Attendance> otherAttendanceList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, currentCycle)
                .ne(Attendance::getGroupName, xzGroupName));
        List<Attendance> otherAttendances = filterNullUserAttendance(otherAttendanceList);
        imputationDetailsService.addByListBatch(imputation, otherDeptUnitList, indexList, otherAttendances);

        //行政科室单元：考勤表人员 - 行政中高层
        List<ImputationDeptUnit> zgcList = this.listByZGC(currentCycle);
        List<ImputationDeptUnit> xzzDeptUnitList = deptUnitList.stream().filter(deptUnit -> deptUnit.getAccountGroupCode() != null && deptUnit.getAccountGroupCode().contains(xzGroupName)).collect(Collectors.toList());
        List<Attendance> xzzAttendanceList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, currentCycle)
                .eq(Attendance::getGroupName, xzGroupName));
        xzzAttendanceList = xzzAttendanceList.stream()
                .filter(attendance -> zgcList.stream()
                        .noneMatch(deptUnit -> Objects.equals(deptUnit.getUserId(), attendance.getUserId())) && Objects.nonNull(attendance.getUserId()) && Objects.nonNull(attendance.getEmpName()))
                .collect(Collectors.toList());
        imputationDetailsService.addByListBatch(imputation, xzzDeptUnitList, indexList, xzzAttendanceList);

        //"行政科室”：行政中高层
        for (ImputationDeptUnit imputationDeptUnit : deptUnitList) {
            if (Objects.equals(xzUnitName, imputationDeptUnit.getAccountUnitName())) {
                //行政中高层
                List<Attendance> zgcAttendanceList = zgcList.stream().map(deptUnit -> new Attendance().setUserId(deptUnit.getUserId()).setEmpName(deptUnit.getUserName())).collect(Collectors.toList());
                imputationDetailsService.addByList(imputation, Collections.singletonList(imputationDeptUnit), indexList, zgcAttendanceList);
                break;
            }
        }
    }

    @Override
    public ImputationDeptUnit getGroupByUnitName(String unitName) {
        LambdaQueryWrapper<ImputationDeptUnit> qr = new LambdaQueryWrapper<>();
        qr.eq(ImputationDeptUnit::getAccountUnitName, unitName).last("LIMIT 1");
        return getOne(qr);
    }

    private List<ImputationDeptUnit> listByPId(Long imputationId) {
        return this.list(Wrappers.<ImputationDeptUnit>lambdaQuery()
                .eq(ImputationDeptUnit::getImputationId, imputationId));
    }

    private void copyLastMonth(String currentCycle, String lastCycle) {
        //查询 主档
        List<Imputation> currentImputationList = imputationService.list(Wrappers.<Imputation>lambdaQuery().eq(Imputation::getImputationCycle, currentCycle));
        List<Imputation> lastImputationList = imputationService.list(Wrappers.<Imputation>lambdaQuery().eq(Imputation::getImputationCycle, lastCycle));
        //Map<String,Imputation> lastImputationMap = lastImputationList.stream().collect(Collectors.toMap(Imputation::getImputationCode, Function.identity()));
        //key 上月的主档id ，value 本月的主档id
        Map<Long, Long> last2currentMap = last2currentMap(lastImputationList, currentImputationList);

        //复制deptUnit
        List<ImputationDeptUnit> deptUnitList = this.list(Wrappers.<ImputationDeptUnit>lambdaQuery().eq(ImputationDeptUnit::getImputationCycle, lastCycle));
        List<ImputationDeptUnit> addDeptUnitList = new ArrayList<>();
        for (ImputationDeptUnit deptUnit : deptUnitList) {
            Long currentImputationId = last2currentMap.get(deptUnit.getImputationId());
            deptUnit.setId(null);
            deptUnit.setImputationId(currentImputationId);
            deptUnit.setImputationCycle(currentCycle);
            addDeptUnitList.add(deptUnit);
        }

        if (!addDeptUnitList.isEmpty()) {
            this.saveBatch(addDeptUnitList);
        }


        //复制归集指标
        Map<Long, Long> lastMothIndexId2NowIdMap = new HashMap<>();
        List<ImputationIndex> lastIndexList = imputationIndexService.list(Wrappers.<ImputationIndex>lambdaQuery().eq(ImputationIndex::getImputationCycle, lastCycle));
        for (ImputationIndex index : lastIndexList) {
            Long lastIndexId = index.getId();
            Long currentImputationId = last2currentMap.get(index.getImputationId());
            index.setId(null);
            index.setImputationId(currentImputationId);
            index.setImputationCycle(currentCycle);
            imputationIndexService.save(index);
            lastMothIndexId2NowIdMap.put(lastIndexId, index.getId());
            //addIndexList.add(index);
            //复制归集指标明细
            List<ImputationIndexDetails> addIndexDetailsList = new ArrayList<>();
            List<ImputationIndexDetails> lastIndexDetailsList = imputationIndexDetailsService.list(Wrappers.<ImputationIndexDetails>lambdaQuery().eq(ImputationIndexDetails::getImputationIndexId, lastIndexId));
            for (ImputationIndexDetails indexDetails : lastIndexDetailsList) {
                indexDetails.setId(null);
                indexDetails.setImputationIndexId(index.getId());
                addIndexDetailsList.add(indexDetails);
            }
            if (!addIndexDetailsList.isEmpty()) {
                imputationIndexDetailsService.saveBatch(addIndexDetailsList);
            }
        }
//        if(!addIndexList.isEmpty()){
//            imputationIndexService.saveBatch(addIndexList);
//        }


        //复制特殊人员
        List<SpecialImputationPerson> addSpecialImputationPersonList = new ArrayList<>();
        List<SpecialImputationPerson> lastSpecialImputationPersonList = specialImputationPersonService.list(Wrappers.<SpecialImputationPerson>lambdaQuery().eq(SpecialImputationPerson::getImputationCycle, lastCycle));
        for (SpecialImputationPerson specialImputationPerson : lastSpecialImputationPersonList) {
            Long currentImputationId = last2currentMap.get(specialImputationPerson.getImputationId());
            specialImputationPerson.setId(null);
            specialImputationPerson.setImputationId(currentImputationId);
            specialImputationPerson.setImputationCycle(currentCycle);
            String imputationIndexIds = specialImputationPerson.getImputationIndexIds();
            if (Objects.nonNull(imputationIndexIds)) {
                for (Long key : lastMothIndexId2NowIdMap.keySet()) {
                    if (imputationIndexIds.contains(key + "")) {
                        imputationIndexIds = imputationIndexIds.replace(key + "", lastMothIndexId2NowIdMap.get(key) + "");
                    }
                }
                specialImputationPerson.setImputationIndexIds(imputationIndexIds);
            }

            addSpecialImputationPersonList.add(specialImputationPerson);
        }
        if (!addSpecialImputationPersonList.isEmpty()) {
            specialImputationPersonService.saveBatch(addSpecialImputationPersonList);
        }

        //复制不计人员
        List<NonIncomePerson> addNonIncomePersonList = new ArrayList<>();
        List<NonIncomePerson> lastNonIncomePersonList = nonIncomePersonService.list(Wrappers.<NonIncomePerson>lambdaQuery().eq(NonIncomePerson::getImputationCycle, lastCycle));
        for (NonIncomePerson nonIncomePerson : lastNonIncomePersonList) {
            Long currentImputationId = last2currentMap.get(nonIncomePerson.getImputationId());
            nonIncomePerson.setId(null);
            nonIncomePerson.setImputationId(currentImputationId);
            nonIncomePerson.setImputationCycle(currentCycle);
            String imputationIndexIds = nonIncomePerson.getImputationIndexIds();
            if (Objects.nonNull(imputationIndexIds)) {
                for (Long key : lastMothIndexId2NowIdMap.keySet()) {
                    if (imputationIndexIds.contains(key + "")) {
                        imputationIndexIds = imputationIndexIds.replace(key + "", lastMothIndexId2NowIdMap.get(key) + "");
                    }
                }
                nonIncomePerson.setImputationIndexIds(imputationIndexIds);
            }
            addNonIncomePersonList.add(nonIncomePerson);
        }
        if (!addNonIncomePersonList.isEmpty()) {
            nonIncomePersonService.saveBatch(addNonIncomePersonList);
        }

        //复制上月人员变更规则
        List<PersonChange> personChanges = personChangeService.listByCycle(lastCycle);
        personChanges.forEach(item -> {
            item.setId(null);
            item.setImputationCycle(currentCycle);
        });
        if (CollectionUtils.isNotEmpty(personChanges)) {
            personChangeService.saveBatch(personChanges);
        }

    }

    private Map<Long, Long> last2currentMap(List<Imputation> lastImputationList, List<Imputation> currentImputationList) {
        Map<Long, Long> rtnMap = new HashMap<>();
        Map<String, Imputation> currentImputationMap = currentImputationList.stream().collect(Collectors.toMap(Imputation::getImputationCode, Function.identity()));
        for (Imputation lastImputation : lastImputationList) {
            String code = lastImputation.getImputationCode();
            Imputation currentImputation = currentImputationMap.get(code);
            if (currentImputation != null) {
                rtnMap.put(lastImputation.getId(), currentImputation.getId());
            }
        }
        return rtnMap;
    }

    private void deleteByCycle(String currentCycle) {
        //删除主档
        //imputationService.remove(Wrappers.<Imputation>lambdaQuery().eq(Imputation::getImputationCycle, currentCycle));

        //删除deptUnit
        this.remove(Wrappers.<ImputationDeptUnit>lambdaQuery().eq(ImputationDeptUnit::getImputationCycle, currentCycle));

        //删除 details
        imputationDetailsService.remove(Wrappers.<ImputationDetails>lambdaQuery().eq(ImputationDetails::getImputationCycle, currentCycle));

        //删除归集指标
        imputationIndexService.remove(Wrappers.<ImputationIndex>lambdaQuery().eq(ImputationIndex::getImputationCycle, currentCycle));

        //删除不计
        nonIncomePersonService.remove(Wrappers.<NonIncomePerson>lambdaQuery().eq(NonIncomePerson::getImputationCycle, currentCycle));

        //删除特殊人员
        specialImputationPersonService.remove(Wrappers.<SpecialImputationPerson>lambdaQuery().eq(SpecialImputationPerson::getImputationCycle, currentCycle));

        //删除变更规则
        personChangeService.remove(Wrappers.<PersonChange>lambdaQuery().eq(PersonChange::getImputationCycle, currentCycle));
    }

    //根据上月人员变更规则更新人员
    @Override
    public void updatePersons(List<ImputationDetails> imputationDetails, String currentCycle) {
        List<PersonChange> personChanges = personChangeService.listByCycle(currentCycle);
        Map<String, List<PersonChange>> str2ListMap = personChanges.stream().collect(Collectors.groupingBy(item -> item.getAccountUnitId() + "-" + item.getImputationIndexName()));
        for (ImputationDetails imputationDetail : imputationDetails) {
            String key = imputationDetail.getAccountUnitId() + "-" + imputationDetail.getImputationIndexName();
            if (str2ListMap.containsKey(key)) {
                List<PersonChange> personChangeList = str2ListMap.get(key);

                Map<String, String> detailsMap = personChangeService.convertToMap(imputationDetail.getUserIds(), imputationDetail.getEmpNames());
                for (PersonChange personChange : personChangeList) {
                    Map<String, String> changeMap = personChangeService.convertToMap(personChange.getUserIds(), personChange.getUserNames());
                    String operationType = personChange.getOperationType();
                    if (DataOpEnum.CREATE.getCode().equals(operationType)) {
                        //新增
                        detailsMap.putAll(changeMap);
                    } else if (DataOpEnum.DELETE.getCode().equals(operationType)) {
                        //删除
                        detailsMap.keySet().removeAll(changeMap.keySet());
                    }
                }
                imputationDetail.setUserIds(String.join(",", detailsMap.keySet()));
                imputationDetail.setEmpNames(String.join(",", detailsMap.values()));
            }
        }
    }


    private List<Attendance> filterNullUserAttendance(List<Attendance> attendanceList) {
        return attendanceList.stream().filter(attendance -> !Objects.isNull(attendance.getUserId()) && !Objects.isNull(attendance.getEmpName())).collect(Collectors.toList());
    }

}
