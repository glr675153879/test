package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.tree.Tree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.model.vo.second.count.PerformanceDetailsVO;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 绩效明细 计算接口
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PerformanceDetailsService {

    private final IAttendanceService attendanceService;
    private final ISecondTaskCountService secondTaskCountService;
    private final ISecondTaskService secondTaskService;
    private final IUnitTaskCountService unitTaskCountService;
    private final IUnitTaskDetailCountService unitTaskDetailCountService;
    private final IUnitTaskDetailItemService unitTaskDetailItemService;
    private final IUnitTaskService unitTaskService;
    private final IUnitTaskProjectCountService unitTaskProjectCountService;
    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;
    private final IUnitTaskProjectService unitTaskProjectService;
    private final IProgrammeService programmeService;
    private final IUnitTaskUserService unitTaskUserService;
    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;

    public PerformanceDetailsVO performanceDetails(Long secondTaskId, Long unitTaskId) {
        // 1. 组装表头 所有列均通过字段名+id生成
        // 查出方案
        // 获取project
        // 根据project获取detail
        // 根据detail获取item
        // 生成表头id
        // 2 获取所有计算数据
        //      sec_unit_task
        //          sec_unit_task_user
        //          sec_unit_task_project
        //              sec_unit_task_project_detail
        //                  sec_unit_task_detail_item
        //          sec_unit_task_count
        //          sec_unit_task_project_count
        //          sec_unit_task_detail_count
        // 根据project计算每层数据，并且同时塞到map中（key:字段名+id value）
        /* 第一步 组装表头 */
        UnitTask unitTask = unitTaskService.getById(unitTaskId);
        ProgrammeInfoVo programmeInfo = programmeService.getProgrammeInfo(unitTask.getProgrammeId());
        List<Tree<String>> heads = assembleHead(programmeInfo);

        List<UnitTaskUser> unitTaskUsers = unitTaskUserService.listByTaskId(unitTaskId);
        Map<String, UnitTaskUser> unitTaskUserMap = unitTaskUsers.stream().filter(e -> Objects.nonNull(e.getEmpCode())).collect(Collectors.toMap(UnitTaskUser::getEmpCode, e -> e,
                (v1, v2) -> v2));

        List<UnitTaskProject> unitTaskProjects = unitTaskProjectService.listByUnitTask(unitTaskId);

        List<Map<String, Object>> data = new ArrayList<>();
        Map<String, Map<String, Object>> dataMap = new HashMap<>();

        List<UnitTaskCount> unitTaskCounts = unitTaskCountService.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId, unitTaskId));
        Map<String, UnitTaskCount> unitTaskCountMap = unitTaskCounts.stream().filter(e -> Objects.nonNull(e.getEmpCode())).collect(Collectors.toMap(UnitTaskCount::getEmpCode, e -> e, (v1, v2) -> v2));
        // 姓名 工号 科室 职务 绩效合计
        for (UnitTaskUser unitTaskUser : unitTaskUsers) {
            Map<String, Object> row = new HashMap<>();
            row.put("xingming", unitTaskUser.getEmpName());// 姓名
            row.put("gonghao", unitTaskUser.getEmpCode());// 工号
            row.put("keshi", unitTaskUser.getDeptName());// 科室
            row.put("zhiwu", unitTaskUser.getPostName());// 职务
            if (unitTaskCountMap.containsKey(unitTaskUser.getEmpCode())) {
                row.put("jixiaoheji", unitTaskCountMap.get(unitTaskUser.getEmpCode()).getAmt());
            }
            row.put("cqts", unitTaskUser.getWorkdays());// 出勤天数
            data.add(row);
            dataMap.put(unitTaskUser.getEmpCode(), row);
        }
        for (UnitTaskProject unitTaskProject : unitTaskProjects) {
            String projectType = CommonUtils.getDicVal(unitTaskProject.getProjectType());
            if (Objects.equals(projectType, ProjectType.pinjun.toString())) {
                // 平均绩效直接从projectCount中拿数据
                // 绩效倍数 人员系数 人员分值 出勤天数 平均绩效
                Map<String, UnitTaskCount> unitTaskProjectCountMap = unitTaskCounts.stream().filter(e -> Objects.nonNull(e.getEmpCode())).collect(Collectors.toMap(UnitTaskCount::getEmpCode, e -> e,
                        (v1, v2) -> v2));
                for (Map.Entry<String, Map<String, Object>> stringMapEntry : dataMap.entrySet()) {
                    String key = stringMapEntry.getKey();// empCode
                    Map<String, Object> rowData = stringMapEntry.getValue();// rowData
                    rowData.put(unitTaskProject.getProgProjectId() + "_jxbs", unitTaskUserMap.get(key).getAvgRate());// 绩效倍数
                    rowData.put(unitTaskProject.getProgProjectId() + "_ryxs", unitTaskUserMap.get(key).getUserRate());// 人员系数
                    rowData.put(unitTaskProject.getProgProjectId() + "_cqts", unitTaskUserMap.get(key).getWorkdays());// 出勤天数

                    // 人员分值=人员系数×出勤天数
                    rowData.put(unitTaskProject.getProgProjectId() + "_ryfz", unitTaskUserMap.get(key).getUserRate().multiply(unitTaskUserMap.get(key).getWorkdays()));// 人员分值
                    if (unitTaskProjectCountMap.containsKey(key)) {
                        if (unitTaskUserMap.get(key).getAvgRate().compareTo(BigDecimal.ZERO) == 0 ||
                                unitTaskUserMap.get(key).getUserRate().multiply(unitTaskUserMap.get(key).getWorkdays()).compareTo(BigDecimal.ZERO) == 0) {
                            rowData.put(unitTaskProject.getProgProjectId() + "_pjjx", BigDecimal.ZERO);// 平均绩效
                        } else {
                            rowData.put(unitTaskProject.getProgProjectId() + "_pjjx", unitTaskProjectCountMap.get(key).getAmt());// 平均绩效
                        }
                    }
                }
            } else if (Objects.equals(projectType, ProjectType.danxiang.toString())) {

                List<UnitTaskProjectDetailVo> userList = unitTaskProjectDetailService.userList(unitTaskProject.getId(), null);
                // 计算每个人的projectCount
                for (UnitTaskProjectDetailVo projectDetailVo : userList) {
                    String empCode = projectDetailVo.getEmpCode();
                    Map<String, Object> rowData = dataMap.get(empCode);// rowData

                    List<UnitTaskProjectDetail> detailList = projectDetailVo.getDetailList();
                    BigDecimal totalAmount = BigDecimal.ZERO;
                    for (UnitTaskProjectDetail unitTaskProjectDetail : detailList) {
                        rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_sl", unitTaskProjectDetail.getQty());// 数量
                        rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_bzy", unitTaskProjectDetail.getPriceValue());// 标准（元）
                        rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_hjy", unitTaskProjectDetail.getAmt());// 合计（元）
                        totalAmount = totalAmount.add(unitTaskProjectDetail.getAmt());
                    }
                    rowData.put(unitTaskProject.getProgProjectId() + "_hj", totalAmount);// 绩效倍数
                }
            } else if (Objects.equals(projectType, ProjectType.erci.toString())) {
                // 获取二次合计
                List<UnitTaskProjectCount> unitTaskProjectCounts =
                        unitTaskProjectCountService.list(Wrappers.<UnitTaskProjectCount>lambdaQuery().eq(UnitTaskProjectCount::getUnitTaskId, unitTaskId).eq(UnitTaskProjectCount::getProjectId,
                                unitTaskProject.getId()));
                Map<String, UnitTaskProjectCount> unitTaskProjectCountMap =
                        unitTaskProjectCounts.stream().filter(e -> Objects.nonNull(e.getEmpCode()))
                                .collect(Collectors.toMap(UnitTaskProjectCount::getEmpCode, e -> e, (k1, k2) -> k1));
                // 获取二次分配的项目
                List<UnitTaskProjectDetail> detailList = unitTaskProjectDetailService.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,
                        unitTaskProject.getId()));
                // 遍历 职称、风险责任、工作质量
                for (UnitTaskProjectDetail unitTaskProjectDetail : detailList) {
                    // 获取绩效合计
                    List<UnitTaskDetailCount> unitTaskDetailCounts = unitTaskDetailCountService.listByDetailId(unitTaskProjectDetail.getId());
                    Map<String, UnitTaskDetailCount> unitTaskDetailCountMap =
                            unitTaskDetailCounts.stream().filter(e -> Objects.nonNull(e.getEmpCode()))
                                    .collect(Collectors.toMap(UnitTaskDetailCount::getEmpCode, e -> e, (v1, v2) -> v2));
                    // 所有人的item数据
                    List<UnitTaskDetailItemVo> voList = unitTaskDetailItemService.userList(unitTaskProjectDetail.getId());
                    // 分值数据
                    Map<String, BigDecimal> userPointMap = unitTaskProjectCountService.getUserPointMap(unitTaskProjectDetail, voList);
                    String modeTpe = CommonUtils.getDicVal(unitTaskProjectDetail.getModeType());
                    if (Objects.equals(ModeType.ratio.toString(), modeTpe)) {
                        // 遍历人
                        for (UnitTaskDetailItemVo unitTaskDetailItemVo : voList) {
                            String empCode = unitTaskDetailItemVo.getEmpCode();// empCode
                            Map<String, Object> rowData = dataMap.get(empCode);// rowData
                            // 大项
                            if (CollUtil.isNotEmpty(unitTaskDetailItemVo.getItemList())) {
                                // 有小项
                                for (UnitTaskDetailItem unitTaskDetailBigItem : unitTaskDetailItemVo.getItemList()) {
                                    if (CollUtil.isNotEmpty(unitTaskDetailBigItem.getUnitTaskDetailItemList())) {
                                        // 有小项
                                        for (UnitTaskDetailItem unitTaskDetailSmallItem : unitTaskDetailBigItem.getUnitTaskDetailItemList()) {
                                            rowData.put(unitTaskDetailSmallItem.getProgDetailItemId() + "_xxxs", unitTaskDetailSmallItem.getAmt());// 分数
                                        }
                                    } else {
                                        rowData.put(unitTaskDetailBigItem.getProgDetailItemId() + "_dxxs", unitTaskDetailBigItem.getAmt());// 分数
                                    }
                                }
                            }
                            if (userPointMap.containsKey(empCode)) {
                                // rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_xmfzhj", userPointMap.get(empCode).divide(unitTaskUserMap.get(empCode).getWorkdays(),
                                //         RoundingMode.DOWN));//  项目分值合计
                                rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_grfz", userPointMap.get(empCode));// 个人分值
                            }
                            if (unitTaskDetailCountMap.containsKey(empCode)) {
                                rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_jxhj", unitTaskDetailCountMap.get(empCode).getAmt());// 绩效合计
                            }
                        }
                    } else if (Objects.equals(ModeType.work.toString(), modeTpe)) {
                        // 获取这个projectDetail的工作量系数数据
                        // 获取绩效合计
                        List<UnitTaskDetailItemWork> unitTaskDetailItemWorks = unitTaskDetailItemWorkService.listByDetailId(unitTaskProjectDetail.getId());
                        Map<String, UnitTaskDetailItemWork> unitTaskDetailItemWorkMap =
                                unitTaskDetailItemWorks.stream().filter(e -> Objects.nonNull(e.getEmpCode()))
                                        .collect(Collectors.toMap(UnitTaskDetailItemWork::getEmpCode, e -> e, (v1, v2) -> v2));
                        // 遍历人
                        for (UnitTaskDetailItemVo unitTaskDetailItemVo : voList) {
                            String empCode = unitTaskDetailItemVo.getEmpCode();// empCode
                            Map<String, Object> rowData = dataMap.get(empCode);// rowData
                            // 大项 工作量类型只有一层
                            if (CollUtil.isNotEmpty(unitTaskDetailItemVo.getItemList())) {
                                for (UnitTaskDetailItem unitTaskDetailItem : unitTaskDetailItemVo.getItemList()) {
                                    rowData.put(unitTaskDetailItem.getProgDetailItemId() + "_sl", unitTaskDetailItem.getPoint());// 数量
                                    rowData.put(unitTaskDetailItem.getProgDetailItemId() + "_fz", unitTaskDetailItem.getPriceValue());// 分值
                                    rowData.put(unitTaskDetailItem.getProgDetailItemId() + "_hj", unitTaskDetailItem.getAmt());// 合计
                                }
                            }
                            if (userPointMap.containsKey(empCode)) {
                                if (unitTaskDetailItemWorkMap.containsKey(empCode)) {
                                    BigDecimal examPoint = unitTaskDetailItemWorkMap.get(empCode).getExamPoint();
                                    BigDecimal workRate = unitTaskDetailItemWorkMap.get(empCode).getWorkRate();
                                    // rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_xmfzhj", userPointMap.get(empCode).divide(examPoint, 20,
                                    //         RoundingMode.DOWN).divide(workRate, 6,
                                    //         RoundingMode.DOWN));// 项目分值合计
                                    rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_grfz", userPointMap.get(empCode));// 个人分值
                                }
                            }
                            if (unitTaskDetailCountMap.containsKey(empCode)) {
                                rowData.put(unitTaskProjectDetail.getProgProjectDetailId() + "_jxhj", unitTaskDetailCountMap.get(empCode).getAmt());// 绩效合计
                            }
                        }
                    } else {
                        throw new BizException("错误的科室二次分配方式");
                    }
                }
                for (UnitTaskUser unitTaskUser : unitTaskUsers) {
                    String empCode = unitTaskUser.getEmpCode();// empCode
                    Map<String, Object> rowData = dataMap.get(empCode);// rowData
                    if (unitTaskProjectCountMap.containsKey(empCode)) {
                        rowData.put(unitTaskProject.getProgProjectId() + "_hj", unitTaskProjectCountMap.get(empCode).getAmt());// 绩效合计
                    }
                }
            }
        }

        PerformanceDetailsVO performanceDetailsVO = new PerformanceDetailsVO();
        performanceDetailsVO.setHeads(heads);
        performanceDetailsVO.setData(data);
        return performanceDetailsVO;
    }

    /**
     * 组装表头
     *
     * @param programmeInfo
     * @return {@link List }<{@link Tree }<{@link String }>>
     */
    private static @NotNull List<Tree<String>> assembleHead(ProgrammeInfoVo programmeInfo) {
        List<Tree<String>> heads = new ArrayList<>();
        heads.add(new Tree<String>().setId("xingming").setName("姓名"));
        heads.add(new Tree<String>().setId("gonghao").setName("工号"));
        heads.add(new Tree<String>().setId("keshi").setName("科室"));
        heads.add(new Tree<String>().setId("zhiwu").setName("职务"));
        heads.add(new Tree<String>().setId("jixiaoheji").setName("绩效合计"));

        for (ProgProject progProject : programmeInfo.getProjectList()) {
            String projectType = CommonUtils.getDicVal(progProject.getProjectType());

            Tree<String> level1 = new Tree<>();
            level1.setId(progProject.getId().toString());
            level1.setName(progProject.getName());
            List<Tree<String>> level1Child = new ArrayList<>();

            if (Objects.equals(projectType, ProjectType.pinjun.toString())) {
                // 如果是平均绩效
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_jxbs").setName("绩效倍数"));
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_ryxs").setName("人员系数"));
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_cqts").setName("出勤天数"));
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_ryfz").setName("人员分值"));
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_pjjx").setName("平均绩效"));
            } else if (Objects.equals(projectType, ProjectType.danxiang.toString())) {
                // 单项只有detail，不存在item
                List<ProgProjectDetail> progProjectDetails = progProject.getProgProjectDetailList();
                for (ProgProjectDetail progProjectDetail : progProjectDetails) {
                    Tree<String> level2 = new Tree<>();
                    level2.setId(progProjectDetail.getId().toString());
                    level2.setName(progProjectDetail.getName());
                    List<Tree<String>> level2Child = new ArrayList<>();
                    level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_sl").setName("数量"));
                    level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_bzy").setName("标准（元）"));
                    level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_hjy").setName("合计（元）"));
                    level2.setChildren(level2Child);
                    level1Child.add(level2);
                }
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_hj").setName("合计"));
            } else if (Objects.equals(projectType, ProjectType.erci.toString())) {
                List<ProgProjectDetail> progProjectDetails = progProject.getProgProjectDetailList();
                for (ProgProjectDetail progProjectDetail : progProjectDetails) {

                    // 科室二次分配：系数分配可以有两层，工作量分配只有一层
                    String modelType = CommonUtils.getDicVal(progProjectDetail.getModeType());
                    Tree<String> level2 = new Tree<>();
                    level2.setId(progProjectDetail.getId().toString());
                    level2.setName(progProjectDetail.getName());
                    List<Tree<String>> level2Child = new ArrayList<>();

                    if (Objects.equals(modelType, ModeType.work.toString())) {
                        // 工作量分配只有一层
                        List<ProgDetailItem> progDetailBigItems = progProjectDetail.getProgDetailItemList();
                        for (ProgDetailItem progDetailBigItem : progDetailBigItems) {
                            Tree<String> level3 = new Tree<>();
                            level3.setId(progDetailBigItem.getId().toString());
                            level3.setName(progDetailBigItem.getName());
                            List<Tree<String>> level3Child = new ArrayList<>();
                            level3Child.add(new Tree<String>().setId(progDetailBigItem.getId() + "_sl").setName("数量"));
                            level3Child.add(new Tree<String>().setId(progDetailBigItem.getId() + "_fz").setName("分值"));
                            level3Child.add(new Tree<String>().setId(progDetailBigItem.getId() + "_hj").setName("合计"));
                            level3.setChildren(level3Child);
                            level2Child.add(level3);
                        }
                    } else if (Objects.equals(modelType, ModeType.ratio.toString())) {

                        // 系数分配可以有两层
                        List<ProgDetailItem> progDetailBigItems = progProjectDetail.getProgDetailItemList();
                        for (ProgDetailItem progDetailBigItem : progDetailBigItems) {
                            Tree<String> level3 = new Tree<>();
                            level3.setId(progDetailBigItem.getId().toString() + "_dxxs");
                            level3.setName(progDetailBigItem.getName());
                            if (CollUtil.isNotEmpty(progDetailBigItem.getChildItemList())) {
                                List<Tree<String>> level3Child = new ArrayList<>();
                                for (ProgDetailItem progDetailSmallItem : progDetailBigItem.getChildItemList()) {
                                    Tree<String> level4 = new Tree<>();
                                    level4.setId(progDetailSmallItem.getId().toString() + "_xxxs");
                                    level4.setName(progDetailSmallItem.getName());
                                    level3Child.add(level4);
                                }
                                level3.setChildren(level3Child);
                            }
                            level2Child.add(level3);
                        }
                    } else {
                        throw new BizException("错误的分配方式");
                    }
                    // level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_xmfzhj").setName("项目分值合计"));
                    level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_grfz").setName("个人分值"));
                    level2Child.add(new Tree<String>().setId(progProjectDetail.getId() + "_jxhj").setName("绩效合计"));
                    level2.setChildren(level2Child);
                    level1Child.add(level2);
                }
                level1Child.add(new Tree<String>().setId(progProject.getId() + "_hj").setName("合计"));
            } else {
                throw new BizException("错误的绩效类型");
            }

            level1.setChildren(level1Child);
            heads.add(level1);
        }
        return heads;
    }

}
