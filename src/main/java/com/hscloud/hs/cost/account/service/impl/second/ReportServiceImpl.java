package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.constant.DmoProperties;
import com.hscloud.hs.cost.account.model.dto.MingBeiExcel;
import com.hscloud.hs.cost.account.model.dto.second.RepotHulijxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxValueDTO;
import com.hscloud.hs.cost.account.model.dto.second.RepotZhigongjxflValueDTO;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiUserAttendance;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendance;
import com.hscloud.hs.cost.account.model.vo.report.SumZhigongjxVO;
import com.hscloud.hs.cost.account.model.vo.report.SumZhigongjxflVO;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.kpi.IKpiUserAttendanceService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.service.userAttendance.ICostUserAttendanceService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import static com.github.yulichang.method.SqlMethod.collect;
import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
 * @author 小小w
 * @date 2024/3/9 11:50
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements IReportService {

    private final IAttendanceService attendanceService;
    private final ISecondTaskCountService secondTaskCountService;
    private final ISecondTaskService secondTaskService;
    private final IGrantUnitService grantUnitService;
    private final IUnitTaskService unitTaskService;
    private final IUnitTaskCountService unitTaskCountService;
    private final DmoUtil dmoUtil;
    private final ICostUserAttendanceService costUserAttendanceService;
    private final IKpiUserAttendanceService kpiUserAttendanceService;
    private final DmoProperties dmoProperties;
    private final RemoteUserService remoteUserService;
    private final SecondKpiService secondKpiService;
    private final Executor threadPoolExecutor;

    @Override
    public Page<RepotZhigongjxValueDTO> zhigongjxList(String cycle, String endCycle, String userName, String isMingBei, Page page) {
        List<RepotZhigongjxValueDTO> firstList = listAllZhigongjx(cycle, endCycle, userName, isMingBei);
        return (Page<RepotZhigongjxValueDTO>) list2Page(firstList, page);
    }

    private List<RepotZhigongjxValueDTO> listAllZhigongjx(String cycle, String endCycle, String userName, String isMingBei) {
        List<RepotZhigongjxValueDTO> result = new ArrayList<>();
        List<String> cycleList = getCycleList(cycle, endCycle);
        //线程提交处理的方法有远程调用是不会自动传递用户上下文信息的
        RequestAttributes context = RequestContextHolder.getRequestAttributes();
        // 使用 CountDownLatch 进行线程同步，线程数量与任务列表大小相同
        CountDownLatch latch = new CountDownLatch(cycleList.size());

        for (String s : cycleList) {

            //线程池处理
            threadPoolExecutor.execute(() -> {
                        try {
                            //设置上下文
                            RequestContextHolder.setRequestAttributes(context);
                            List<RepotZhigongjxValueDTO> allZhigongjxList = getAllZhigongjxList(s, isMingBei);
                            result.addAll(allZhigongjxList);
                        } finally {
                            //重置上下文
                            RequestContextHolder.resetRequestAttributes();
                            latch.countDown(); // 任务完成，计数器减少
                        }

                    }
            );
        }

        // 等待所有线程任务完成
        try {
            // 主线程在此阻塞，直到 latch 计数器归零
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("主线程被中断", e);
        }

        Map<String, List<RepotZhigongjxValueDTO>> zhigongjxGroupMap = result.stream().filter(item -> item.getUserId() != null)
                .collect(Collectors.groupingBy(RepotZhigongjxValueDTO::getUserId));

        //汇总金额
        List<RepotZhigongjxValueDTO> valueDTOList = zhigongjxGroupMap.values().stream().map(list -> {
            RepotZhigongjxValueDTO repotZhigongjxValueDTO = new RepotZhigongjxValueDTO();
            BeanUtils.copyProperties(list.get(0), repotZhigongjxValueDTO);
            BigDecimal totalAmt = list.stream().map(RepotZhigongjxValueDTO::getTotalAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondAmt = list.stream().map(RepotZhigongjxValueDTO::getSecondAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal amt = list.stream().map(RepotZhigongjxValueDTO::getAmt).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            //BigDecimal glAmt = list.stream().map(RepotZhigongjxValueDTO::getGlAmt).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            repotZhigongjxValueDTO.setAmt(amt + "");
            //repotZhigongjxValueDTO.setGlAmt(glAmt + "");
            repotZhigongjxValueDTO.setTotalAmt(totalAmt);
            repotZhigongjxValueDTO.setSecondAmt(secondAmt);
            return repotZhigongjxValueDTO;
        }).collect(Collectors.toList());


        if (StrUtil.isNotBlank(userName)) {
            valueDTOList = valueDTOList.stream().filter(item -> StrUtil.isNotBlank(item.getUserName()) && item.getUserName().contains(userName))
                    .collect(Collectors.toList());
        }
        return valueDTOList;
    }

    private List<RepotZhigongjxValueDTO> getAllZhigongjxList(String cycle, String isMingBei) {
        List<RepotZhigongjxValueDTO> firstList = secondKpiService.zhigongjxList(cycle);

        //加上没有一次分配，仅有二次分配的人
        this.addSecondJx(cycle, firstList);

        //是否明贝人员
        filterIsMingBei(cycle, isMingBei, firstList);

        //如果一个编外的人，他的科室被发放单元设置为含编外的，那么他的一次分配金额置为0
        this.checkBianwai(cycle, firstList);
        this.setOtherZhigongjx(cycle, firstList);
        return firstList;
    }

    private void filterIsMingBei(String cycle, String isMingBei, List<RepotZhigongjxValueDTO> firstList) {
        //入参的周期处理为attendance的周期格式 2024-06---->202406
        String[] split = cycle.split("-");
        String newCycle = split[0] + split[1];
        //从考勤表查询人员的工作性质
        if (StrUtil.isNotBlank(isMingBei)) {
            //查询明贝的考勤数据
            List<Attendance> attendances = attendanceService.list(Wrappers.<Attendance>lambdaQuery().eq(Attendance::getCycle, newCycle).like(Attendance::getWorkType, "明贝"));
            List<String> mingBeiUserIds = attendances.stream().map(Attendance::getUserId).filter(Objects::nonNull).map(String::valueOf).collect(Collectors.toList());

            if ("0".equals(isMingBei)) {
                // 剔除明贝
                firstList.removeIf(item -> mingBeiUserIds.contains(item.getUserId()));
            } else {
                // 只要明贝
                firstList.removeIf(item -> !mingBeiUserIds.contains(item.getUserId()));
            }

        }
    }

    private void checkBianwai(String cycle, List<RepotZhigongjxValueDTO> firstList) {
        log.info("firstList:{}", firstList);
        //额外人员
        List<GrantUnit> exList = grantUnitService.list();
        String exUserIds = exList.stream().map(GrantUnit::getExtraUserIds).filter(Objects::nonNull).collect(Collectors.joining(","));
        List<String> exUserIdList = StringUtils.isNotBlank(exUserIds) ? Arrays.asList(exUserIds.split(",")) : new ArrayList<>();
        for (RepotZhigongjxValueDTO dto : firstList) {
            if (Objects.equals(dto.getUserType(), "Y")) {
                //如果一个编外的人，他的科室被发放单元设置为含编外的，那么他的一次分配金额置为0
                log.info("编外 username:{}", dto.getUserName());
                String accountUnitId = dto.getDeptId();
                boolean exists = grantUnitService.exists(Wrappers.<GrantUnit>lambdaQuery().like(GrantUnit::getKsUnitIds, accountUnitId));
                if (exists) {
                    dto.setAmt("0");
                    continue;
                }
            }
            //如果是额外人员
            if (!exUserIdList.isEmpty() && exUserIdList.contains(dto.getUserId())) {
                dto.setAmt("0");
            }

        }
    }

    private void addSecondJx(String cycle, List<RepotZhigongjxValueDTO> firstList) {
        //二次分配金额
        SecondTask secondTask = secondTaskService.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle, cycle));
        if (secondTask != null) {
            List<SecondTaskCount> secondTaskCountList = secondTaskCountService.list(Wrappers.<SecondTaskCount>lambdaQuery()
                    .eq(SecondTaskCount::getSecondTaskId, secondTask.getId()));
            Map<String, RepotZhigongjxValueDTO> firstMap = firstList.stream().filter(item -> item.getUserId() != null).collect(Collectors.toMap(RepotZhigongjxValueDTO::getUserId, item -> item,
                    (v1, v2) -> v2));
            //secondTaskCountList 不存在firstList的数据
            secondTaskCountList.forEach(secondTaskCount -> {
                RepotZhigongjxValueDTO repotZhigongjxValueDTO = firstMap.get(secondTaskCount.getUserId() + "");
                if (repotZhigongjxValueDTO == null) {
                    repotZhigongjxValueDTO = new RepotZhigongjxValueDTO();
                    repotZhigongjxValueDTO.setUserId(secondTaskCount.getUserId() + "");
                    repotZhigongjxValueDTO.setUserName(secondTaskCount.getEmpName());
                    repotZhigongjxValueDTO.setAmt("0");
                    firstList.add(repotZhigongjxValueDTO);
                    firstMap.put(secondTaskCount.getUserId() + "", repotZhigongjxValueDTO);
                }
            });
        }
    }

    private void addSecondJxfl(String cycle, List<RepotZhigongjxflValueDTO> firstList) {
        //二次分配金额
        SecondTask secondTask = secondTaskService.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle, cycle));
        if (secondTask != null) {
            List<SecondTaskCount> secondTaskCountList = secondTaskCountService.list(Wrappers.<SecondTaskCount>lambdaQuery()
                    .eq(SecondTaskCount::getSecondTaskId, secondTask.getId()));
            Map<String, RepotZhigongjxflValueDTO> firstMap = firstList.stream().filter(item -> item.getUserId() != null).collect(Collectors.toMap(RepotZhigongjxflValueDTO::getUserId, item -> item, (v1, v2) -> v2));
            //secondTaskCountList 不存在firstList的数据
            secondTaskCountList.forEach(secondTaskCount -> {
                RepotZhigongjxflValueDTO repotZhigongjxflValueDTO = firstMap.get(secondTaskCount.getUserId() + "");
                if (repotZhigongjxflValueDTO == null) {
                    repotZhigongjxflValueDTO = new RepotZhigongjxflValueDTO();
                    repotZhigongjxflValueDTO.setUserId(secondTaskCount.getUserId() + "");
                    repotZhigongjxflValueDTO.setUserName(secondTaskCount.getEmpName());
                    repotZhigongjxflValueDTO.setMenzhenjx("0");
                    repotZhigongjxflValueDTO.setGuanlijx("0");
                    firstList.add(repotZhigongjxflValueDTO);
                    firstMap.put(secondTaskCount.getUserId() + "", repotZhigongjxflValueDTO);
                }
            });
        }
    }

    private void setOtherZhigongjx(String cycle, List<RepotZhigongjxValueDTO> list) {
        //岗位
        List<String> userIds = list.stream().map(RepotZhigongjxValueDTO::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        //List<String> userCodes = list.stream().map(RepotZhigongjxValueDTO::getEmpCode).filter(Objects::nonNull).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }
        List<Attendance> attendanceList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                .eq(Attendance::getCycle, cycle.replace("-", ""))
                .in(CollectionUtil.isNotEmpty(userIds), Attendance::getUserId, userIds));
        //.and(wrapper -> wrapper.in(CollectionUtil.isNotEmpty(userIds),Attendance::getUserId,userIds).or().in(CollectionUtil.isNotEmpty(userCodes),Attendance::getEmpCode,userCodes)));

        //key:userId
        Map<String, Attendance> attendanceMap = new HashMap<>();
        for (Attendance attendance : attendanceList) {
            attendanceMap.put(attendance.getUserId() + "", attendance);
        }
        //List<String> empCodes = attendanceList.stream().map(Attendance::getEmpCode).collect(Collectors.toList());
        //key:userId
        Map<String, String> userIdEmpCodeMap = new HashMap<>();
        for (Attendance attendance : attendanceList) {
            userIdEmpCodeMap.put(attendance.getUserId() + "", attendance.getEmpCode());
        }

        //二次分配金额
        SecondTask secondTask = secondTaskService.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle, cycle));
        //key:userId
        Map<Long, BigDecimal> secondTaskCountMap = new HashMap<>();
        if (secondTask != null) {
            List<SecondTaskCount> secondTaskCountList = secondTaskCountService.list(Wrappers.<SecondTaskCount>lambdaQuery()
                    .eq(SecondTaskCount::getSecondTaskId, secondTask.getId()).in(SecondTaskCount::getUserId, userIds));
            secondTaskCountMap = secondTaskCountList.stream().collect(Collectors.toMap(SecondTaskCount::getUserId, SecondTaskCount::getAmt, (v1, v2) -> v2));
        }

        for (RepotZhigongjxValueDTO vo : list) {
            String userId = vo.getUserId();
            //String empCode = vo.getEmpCode() == null?userIdEmpCodeMap.get(userId):vo.getEmpCode();
            Attendance attendance = attendanceMap.get(userId);
            if (StringUtils.isBlank(vo.getPostName()) && attendance != null) {
                vo.setPostName(attendance.getTitle());
            }
            if (StringUtils.isBlank(vo.getDeptName()) && attendance != null) {
                vo.setDeptName(attendance.getDeptName());
            }
            vo.setSecondAmt(secondTaskCountMap.get(Long.parseLong(userId)) == null ? BigDecimal.ZERO : secondTaskCountMap.get(Long.parseLong(userId)));
            //总金额
            BigDecimal amt = BigDecimal.ZERO;
            try {
                amt = new BigDecimal(vo.getAmt());
            } catch (Exception e) {
            }
            BigDecimal secondAmt = vo.getSecondAmt() == null ? BigDecimal.ZERO : vo.getSecondAmt();
            BigDecimal totalAmt = amt.add(secondAmt);
            vo.setTotalAmt(totalAmt);
        }
    }

    private void setOtherZhigongjxfl(String cycle, List<RepotZhigongjxflValueDTO> list) {
        //获取管理绩效和门诊绩效数据
        List<RepotZhigongjxflValueDTO> firstList = secondKpiService.zhigongjxflList(cycle);
        Map<String, RepotZhigongjxflValueDTO> collect = firstList.stream().collect(Collectors.toMap(RepotZhigongjxflValueDTO::getUserId, item -> item, (v1, v2) -> v2));
        //人员工号集合
        List<String> userIds = list.stream().map(RepotZhigongjxflValueDTO::getUserId).filter(Objects::nonNull).collect(Collectors.toList());
        if (userIds.isEmpty()) {
            return;
        }

        //二次分配金额
        SecondTask secondTask = secondTaskService.getOne(Wrappers.<SecondTask>lambdaQuery().eq(SecondTask::getCycle, cycle));
        if (secondTask == null) {
            return;
        }

        //鄞州门诊医生组金额
        String yzGrantUnitName = "鄞州门诊医生组";
        GrantUnit grantUnit = grantUnitService.getOne(Wrappers.<GrantUnit>lambdaQuery().eq(GrantUnit::getName, yzGrantUnitName));
        //key:userId
        Map<Long, BigDecimal> yzCountMap = new HashMap<>();
        if (grantUnit != null) {
            UnitTask unitTask = unitTaskService.getOne(Wrappers.<UnitTask>lambdaQuery()
                    .eq(UnitTask::getGrantUnitId, grantUnit.getId())
                    .eq(UnitTask::getCycle, cycle));
            if (unitTask != null) {
                List<UnitTaskCount> unitTaskCountList = unitTaskCountService.list(Wrappers.<UnitTaskCount>lambdaQuery()
                        .eq(UnitTaskCount::getUnitTaskId, unitTask.getId())
                        .in(UnitTaskCount::getUserId, userIds));
                yzCountMap = unitTaskCountList.stream().collect(Collectors.toMap(UnitTaskCount::getUserId, UnitTaskCount::getAmt, (v1, v2) -> v2));
            }
        }

        //明湖院区医生组
        String yhGrantUnitName = "明湖院区";
        GrantUnit grantUnit2 = grantUnitService.getOne(Wrappers.<GrantUnit>lambdaQuery().eq(GrantUnit::getName, yhGrantUnitName));
        //key:userId
        Map<Long, BigDecimal> yhCountMap = new HashMap<>();
        if (grantUnit2 != null) {
            UnitTask unitTask = unitTaskService.getOne(Wrappers.<UnitTask>lambdaQuery()
                    .eq(UnitTask::getGrantUnitId, grantUnit2.getId())
                    .eq(UnitTask::getCycle, cycle));
            if (unitTask != null) {
                List<UnitTaskCount> unitTaskCountList = unitTaskCountService.list(Wrappers.<UnitTaskCount>lambdaQuery()
                        .eq(UnitTaskCount::getUnitTaskId, unitTask.getId())
                        .in(UnitTaskCount::getUserId, userIds));
                yhCountMap = unitTaskCountList.stream().collect(Collectors.toMap(UnitTaskCount::getUserId, UnitTaskCount::getAmt, (v1, v2) -> v2));
            }
        }

        for (RepotZhigongjxflValueDTO vo : list) {
            try {
                Long userId = Long.parseLong(vo.getUserId());
                if (collect.containsKey(String.valueOf(userId))) {
                    RepotZhigongjxflValueDTO jxflValueDTO = collect.get(String.valueOf(userId));
                    if (Objects.nonNull(jxflValueDTO.getGuanlijx())) {
                        vo.setGuanlijx(jxflValueDTO.getGuanlijx());
                    }
                    if (Objects.nonNull(jxflValueDTO.getMenzhenjx())) {
                        vo.setMenzhenjx(jxflValueDTO.getMenzhenjx());
                    }
                }
                //鄞州门诊医生组金额
                vo.setSecondYzAmt(yzCountMap.get(userId) == null ? BigDecimal.ZERO : yzCountMap.get(userId));
                //明湖院区医生组金额
                vo.setSecondYhAmt(yhCountMap.get(userId) == null ? BigDecimal.ZERO : yhCountMap.get(userId));
                //除鄞州门诊医生组金额
                BigDecimal guanlijx = new BigDecimal(vo.getGuanlijx());
                BigDecimal menzhenjx = new BigDecimal(vo.getMenzhenjx());
                //科室绩效=合计-管理绩效-门诊绩效-鄞州门诊-明湖院区
                vo.setSecondAmtWithoutYz(vo.getTotalAmt().subtract(guanlijx).subtract(menzhenjx).subtract(vo.getSecondYzAmt()).subtract(vo.getSecondYhAmt()));
            } catch (Exception e) {
                log.error("RepotZhigongjxflValueDTO 数据异常" + vo.getUserId() + vo.getUserName(), e);
            }
        }
    }

    private Page list2Page(List list, Page page) {
        //分页要求数据为 indexStart ~ indexEnd
        long pageNum = page.getCurrent();
        long pageSize = page.getSize();
        long indexStart = (pageNum - 1) * pageSize;
        long indexEnd = pageNum * pageSize - 1;
        long index = 0L;//total = index
        boolean ifEnough = false;

        Page rtnPage = new Page<>(pageNum, pageSize);

        rtnPage.setRecords(new ArrayList());
        List records = new ArrayList();
        for (Object patient : list) {
            if (!ifEnough) {
                if (index >= indexStart && index <= indexEnd) { //符合分页要求
                    rtnPage.getRecords().add(patient);
                    records.add(patient);
                    if (index == indexEnd) {//满足分页要求的数据量
                        //break;
                        ifEnough = true;
                    }
                }
            }
            index++;//符合要求的数据总量
        }
        rtnPage.setTotal(index);
        return rtnPage;
    }


    @Override
    public List<RepotHulijxValueDTO> hulijxList(String cycle) {
        List<RepotHulijxValueDTO> list = dmoUtil.hulijxList(cycle);
        return list;
    }

    @Override
    public void exportZhigongjx(String cycle, HttpServletResponse response) {
        List<RepotZhigongjxValueDTO> list = secondKpiService.zhigongjxList(cycle);

        this.setOtherZhigongjx(cycle, list);

        try (OutputStream out = response.getOutputStream();) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(cycle + "职工绩效");
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            //查全部
            table.setHead(this.getHeadZhigongjx());
            // 写数据
            writer.write(this.getContentZhigongjx(list), sheet1, table);
            writer.finish();
        } catch (Exception e) {

        }
    }

    private List<List<Object>> getContentZhigongjx(List<RepotZhigongjxValueDTO> list) {
        List<List<Object>> totalContent = new ArrayList<>();
        for (RepotZhigongjxValueDTO item : list) {
            List<Object> colList = new ArrayList<>();
            colList.add(item.getUserName());
            colList.add(item.getPostName());
            colList.add(item.getDeptName());
            colList.add(item.getTotalAmt());
            colList.add(item.getAmt());
            colList.add(item.getSecondAmt());
            totalContent.add(colList);
        }
        return totalContent;
    }

    private List<List<String>> getHeadZhigongjx() {
        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> name = new ArrayList<>();
        name.add("职工姓名");
        total.add(name);

        List<String> empCode = new ArrayList<>();
        empCode.add("职务");
        total.add(empCode);

        List<String> unit = new ArrayList<>();
        unit.add("科室单元");
        total.add(unit);

        List<String> count = new ArrayList<>();
        count.add("总核算值");
        total.add(count);

        List<String> first = new ArrayList<>();
        first.add("一次分配绩效");
        total.add(first);

        List<String> second = new ArrayList<>();
        second.add("二次分配绩效");
        total.add(second);

        return total;
    }

    @Override
    public void exportHulijx(String cycle, HttpServletResponse response) {
        List<RepotHulijxValueDTO> list = dmoUtil.hulijxList(cycle);


        try (OutputStream out = response.getOutputStream();) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(cycle + "护理绩效");
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            //查全部
            table.setHead(this.getHeadHulijx());
            // 写数据
            writer.write(this.getContentHulijx(list), sheet1);
            writer.finish();
        } catch (Exception e) {

        }
    }

    @Override
    public List<RepotZhigongjxValueDTO> exportZhigongjx(String cycle, String endCycle, String userName) {
        return listAllZhigongjx(cycle, endCycle, userName, null);
    }

    @Override
    public String lastCycle() {
        SecondTask lastOne = secondTaskService.getOne(Wrappers.<SecondTask>lambdaQuery()
                .orderByDesc(SecondTask::getCycle)
                .last("limit 1"));
        return lastOne.getCycle() == null ? "" : lastOne.getCycle();
    }

    @Override
    public Page<RepotZhigongjxflValueDTO> zhigongjxflList(String cycle, String endCycle, String userName, String isMingBei, Page page) {
        List<RepotZhigongjxflValueDTO> firstList = listAllZhigongjxfl(cycle, endCycle, userName, isMingBei);

        return (Page<RepotZhigongjxflValueDTO>) list2Page(firstList, page);
    }

    private List<RepotZhigongjxflValueDTO> listAllZhigongjxfl(String cycle, String endCycle, String userName, String isMingBei) {
        List<String> cycleList = getCycleList(cycle, endCycle);
        List<RepotZhigongjxflValueDTO> result = new ArrayList<>();
        //线程提交处理的方法有远程调用是不会自动传递用户上下文信息的
        RequestAttributes context = RequestContextHolder.getRequestAttributes();
        // 使用 CountDownLatch 进行线程同步，线程数量与任务列表大小相同
        CountDownLatch latch = new CountDownLatch(cycleList.size());

        for (String s : cycleList) {

            //线程池处理
            threadPoolExecutor.execute(() -> {
                        try {
                            //设置上下文
                            RequestContextHolder.setRequestAttributes(context);
                            List<RepotZhigongjxflValueDTO> allZhigongjxflList = getAllZhigongjxflList(s, isMingBei);
                            result.addAll(allZhigongjxflList);
                        } finally {
                            //重置上下文
                            RequestContextHolder.resetRequestAttributes();
                            latch.countDown(); // 任务完成，计数器减少
                        }

                    }
            );
        }

        // 等待所有线程任务完成
        try {
            // 主线程在此阻塞，直到 latch 计数器归零
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("主线程被中断", e);
        }

        Map<String, List<RepotZhigongjxflValueDTO>> zhigongjxflGroupMap = result.stream().filter(item -> item.getUserId() != null)
                .collect(Collectors.groupingBy(RepotZhigongjxflValueDTO::getUserId));

        //汇总金额
        List<RepotZhigongjxflValueDTO> values = zhigongjxflGroupMap.values().stream().map(list -> {
            RepotZhigongjxflValueDTO repotZhigongjxflValueDTO = new RepotZhigongjxflValueDTO();
            BeanUtils.copyProperties(list.get(0), repotZhigongjxflValueDTO);
            BigDecimal totalAmt = list.stream().map(RepotZhigongjxflValueDTO::getTotalAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondYzAmt = list.stream().map(RepotZhigongjxflValueDTO::getSecondYzAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondYhAmt = list.stream().map(RepotZhigongjxflValueDTO::getSecondYhAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal guanlijx = list.stream().map(RepotZhigongjxflValueDTO::getGuanlijx).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal glAmt = list.stream().map(RepotZhigongjxflValueDTO::getMenzhenjx).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal secondAmtWithoutYz = list.stream().map(RepotZhigongjxflValueDTO::getSecondAmtWithoutYz).reduce(BigDecimal.ZERO, BigDecimal::add);

            repotZhigongjxflValueDTO.setTotalAmt(totalAmt);
            repotZhigongjxflValueDTO.setSecondYzAmt(secondYzAmt);
            repotZhigongjxflValueDTO.setSecondYhAmt(secondYhAmt);
            repotZhigongjxflValueDTO.setGuanlijx(guanlijx + "");
            repotZhigongjxflValueDTO.setMenzhenjx(glAmt + "");
            repotZhigongjxflValueDTO.setSecondAmtWithoutYz(secondAmtWithoutYz);
            return repotZhigongjxflValueDTO;
        }).collect(Collectors.toList());

        if (StrUtil.isNotBlank(userName)) {
            return values.stream().filter(item -> StrUtil.isNotBlank(item.getUserName()) && item.getUserName().contains(userName))
                    .collect(Collectors.toList());
        }

        return values;

    }

    private List<RepotZhigongjxflValueDTO> getAllZhigongjxflList(String cycle, String isMingbei) {
        List<RepotZhigongjxValueDTO> allZhigongjxList = getAllZhigongjxList(cycle, isMingbei);
        if (CollectionUtils.isEmpty(allZhigongjxList)) {
            return Collections.emptyList();
        }
        List<RepotZhigongjxflValueDTO> collect = allZhigongjxList.stream().map(e -> {
            RepotZhigongjxflValueDTO fl = new RepotZhigongjxflValueDTO();
            fl.setCycle(e.getCycle());
            fl.setUserId(e.getUserId());
            fl.setUserName(e.getUserName());
            fl.setGuanlijx("0");
            fl.setMenzhenjx("0");
            fl.setSecondAmtWithoutYz(BigDecimal.ZERO);
            fl.setSecondYzAmt(BigDecimal.ZERO);
            fl.setTotalAmt(e.getTotalAmt());
            return fl;
        }).collect(Collectors.toList());
        this.setOtherZhigongjxfl(cycle, collect);
        if (Objects.equals(isMingbei, "1")) {
            this.fillAttendanceGroup(collect, cycle);
        }
        return collect;
    }

    private void fillAttendanceGroup(List<RepotZhigongjxflValueDTO> collect, String cycle) {
        String newCycle = cycle.replace("-", "");
        String apiMonth = dmoProperties.getApiMonth().replace("-", "");
        List<Long> userIds = collect.stream().map(RepotZhigongjxflValueDTO::getUserId).filter(Objects::nonNull).map(Long::valueOf).distinct().collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }

        List<SysUser> sysUsers = remoteUserService.getUserListPost(userIds).getData();
        Map<Long, String> userId2JobNumberMap = sysUsers.stream().filter(e -> Objects.nonNull(e.getJobNumber())).collect(Collectors.toMap(SysUser::getUserId, SysUser::getJobNumber, (v1, v2) -> v1));
        List<String> jobNumbers = sysUsers.stream().map(SysUser::getJobNumber).collect(Collectors.toList());

        Map<String, String> empId2GroupMap;
        // 周期大于9月的用KpiUserAttendance表 周期小于等于九月的用CostUserAttendance表
        if (newCycle.compareTo(apiMonth) > 0) {
            List<KpiUserAttendance> kpiUserAttendances = kpiUserAttendanceService.list(Wrappers.<KpiUserAttendance>lambdaQuery().in(KpiUserAttendance::getEmpId, jobNumbers).eq(KpiUserAttendance::getPeriod, Long.valueOf(newCycle)));
            empId2GroupMap = kpiUserAttendances.stream().collect(Collectors.toMap(KpiUserAttendance::getEmpId, KpiUserAttendance::getAttendanceGroup, (v1, v2) -> v1));
        } else {
            List<CostUserAttendance> costUserAttendances = costUserAttendanceService.list(Wrappers.<CostUserAttendance>lambdaQuery().in(CostUserAttendance::getEmpId, jobNumbers).eq(CostUserAttendance::getDt, newCycle));
            empId2GroupMap = costUserAttendances.stream().collect(Collectors.toMap(CostUserAttendance::getEmpId, CostUserAttendance::getAttendanceGroup, (v1, v2) -> v1));
        }

        collect.forEach(e -> {
            String jobNumber = userId2JobNumberMap.get(Long.valueOf(e.getUserId()));
            e.setAttendanceGroup(empId2GroupMap.get(jobNumber));
        });
    }

    @Override
    public List<RepotZhigongjxflValueDTO> exportZhigongjxfl(String cycle, String endCycle, String userName) {
        return listAllZhigongjxfl(cycle, endCycle, userName, "0");
    }

    @Override
    public List<MingBeiExcel> exportMingBei(String cycle, String endCycle, String userName) {
        List<RepotZhigongjxflValueDTO> zhigongjxflValueDTOList = listAllZhigongjxfl(cycle, endCycle, userName, "1");
        return BeanUtil.copyToList(zhigongjxflValueDTOList, MingBeiExcel.class);
    }

    @Override
    public SumZhigongjxVO sumZhigongjx(String cycle, String endCycle, String userName) {
        List<RepotZhigongjxValueDTO> repotZhigongjxValueDTOS = listAllZhigongjx(cycle, endCycle, userName, null);
        SumZhigongjxVO sumZhigongjxVO = new SumZhigongjxVO();
        //汇总金额
        sumZhigongjxVO.setTotalAmt(repotZhigongjxValueDTOS.stream().map(RepotZhigongjxValueDTO::getTotalAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxVO.setAmt(repotZhigongjxValueDTOS.stream().map(RepotZhigongjxValueDTO::getAmt).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxVO.setSecondAmt(repotZhigongjxValueDTOS.stream().map(RepotZhigongjxValueDTO::getSecondAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        return sumZhigongjxVO;
    }

    @Override
    public SumZhigongjxflVO sumZhigongjxfl(String cycle, String endCycle, String userName, String isMingBei) {
        List<RepotZhigongjxflValueDTO> zhigongjxflValueDTOList = listAllZhigongjxfl(cycle, endCycle, userName, isMingBei);
        SumZhigongjxflVO sumZhigongjxflVO = new SumZhigongjxflVO();
        sumZhigongjxflVO.setTotalAmt(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getTotalAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setSecondAmt(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getSecondAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setSecondYzAmt(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getSecondYzAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setSecondYhAmt(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getSecondYhAmt).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setSecondAmtWithoutYz(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getSecondAmtWithoutYz).filter(Objects::nonNull).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setGuanlijx(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getGuanlijx).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add));
        sumZhigongjxflVO.setMenzhenjx(zhigongjxflValueDTOList.stream().map(RepotZhigongjxflValueDTO::getMenzhenjx).filter(StringUtils::isNotBlank).map(BigDecimal::new).reduce(BigDecimal.ZERO, BigDecimal::add));
        return sumZhigongjxflVO;
    }

    private List<List<Object>> getContentHulijx(List<RepotHulijxValueDTO> list) {
        List<List<Object>> totalContent = new ArrayList<>();
        for (RepotHulijxValueDTO item : list) {
            List<Object> colList = new ArrayList<>();
            colList.add(item.getDeptName());
            colList.add(item.getKsAmt());
            colList.add(item.getGlAmt());
            colList.add(item.getHszAmt());
            totalContent.add(colList);
        }
        return totalContent;
    }

    private List<List<String>> getHeadHulijx() {
        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> unit = new ArrayList<>();
        unit.add("科室单元");
        total.add(unit);

        List<String> name = new ArrayList<>();
        name.add("科室绩效");
        total.add(name);

        List<String> empCode = new ArrayList<>();
        empCode.add("管理绩效");
        total.add(empCode);

        List<String> count = new ArrayList<>();
        count.add("护士长平均绩效（测算值）");
        total.add(count);

        return total;
    }


    /**
     * 获取周期区间
     *
     * @param cycle    开始周期区间
     * @param endCycle 结束周期区间
     */
    private static List<String> getCycleList(String cycle, String endCycle) {
        if (cycle.compareTo(endCycle) > 0) {
            throw new BizException("开始时间不能大于结束时间");
        }

        List<String> cycleList = new ArrayList<>();

        int startYear = Integer.parseInt(cycle.substring(0, 4));
        int startMonth = Integer.parseInt(cycle.substring(5, 7));
        int endYear = Integer.parseInt(endCycle.substring(0, 4));
        int endMonth = Integer.parseInt(endCycle.substring(5, 7));

        while (startYear < endYear || (startYear == endYear && startMonth <= endMonth)) {
            cycleList.add(String.format("%04d-%02d", startYear, startMonth));
            if (startMonth == 12) {
                startYear++;
                startMonth = 1;
            } else {
                startMonth++;
            }
        }

        return cycleList;
    }
}
