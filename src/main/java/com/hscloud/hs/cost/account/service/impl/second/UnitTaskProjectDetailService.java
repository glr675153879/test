package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.comparator.CompareUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.second.ActionType;
import com.hscloud.hs.cost.account.constant.enums.second.InputType;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.constant.enums.second.ProjectType;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectDetailMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskProjectMapper;
import com.hscloud.hs.cost.account.model.dto.second.*;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskProjectDetailVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
* 核算指标值 服务实现类
*
*/
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskProjectDetailService extends ServiceImpl<UnitTaskProjectDetailMapper, UnitTaskProjectDetail> implements IUnitTaskProjectDetailService {

    private final IProgProjectDetailService progProjectDetailService;
    private final IGrantUnitService grantUnitService;
    private final IUnitTaskUserService unitTaskUserService;
    private final IUnitTaskDetailItemService unitTaskDetailItemService;
    private final IUnitTaskDetailItemWorkService unitTaskDetailItemWorkService;
    private final UnitTaskProjectMapper unitTaskProjectMapper;
    private final UnitTaskMapper unitTaskMapper;
    private final SecondKpiService secondKpiService;
    private final IUnitTaskProjectCountService unitTaskProjectCountService;

    private final RedisUtil redisUtil;
    @Lazy
    @Autowired
    private IUnitTaskProjectService unitTaskProjectService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createByProject(String cycle, UnitTaskProject unitTaskProject, ProgProject progProject) {
        Long unitTaskId = unitTaskProject.getUnitTaskId();
        Long taskProjectId = unitTaskProject.getId();

        List<ProgProjectDetail> progProjectDetailList = progProjectDetailService.listByPidCache(cycle, progProject.getId());
        String projectType = CommonUtils.getDicVal(progProject.getProjectType());
        this.createByProgDetailList(cycle,unitTaskId,taskProjectId,progProjectDetailList,projectType);

    }

    @Override
    public void initUserData(Long unitTaskId, UnitTaskProject unitTaskProject, ProgProject progProject, List<UnitTaskUser> userList) {
        //人员list
        if(userList == null){
            userList = unitTaskUserService.listByTaskId(unitTaskId);
        }
        //方案指标明细
        List<ProgProjectDetail> progProjectDetailList = progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId, progProject.getId()));

        this.addByProgDetailList(unitTaskProject,userList,progProjectDetailList);
    }
    @Override
    public List<UnitTaskProjectDetailVo> userList(Long projectId) {
        return this.userList(projectId,null);
    }
    @Override
    public List<UnitTaskProjectDetailVo> userList(Long projectId,String empCode) {
        UnitTaskProject project = unitTaskProjectMapper.selectById(projectId);
        Long unitTaskId = project.getUnitTaskId();
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(unitTaskId,empCode);
        List<UnitTaskProjectDetail> detailList = this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                .eq(UnitTaskProjectDetail::getUnitTaskProjectId,projectId)
                .eq(empCode!=null,UnitTaskProjectDetail::getEmpCode,empCode)
                .orderByAsc(UnitTaskProjectDetail::getProgProjectDetailId));
        sortDetail(project.getProgProjectId(), detailList);
        //根据人员分组 detailMap ，key：empcode
        Map<String,List<UnitTaskProjectDetail>> detailMap = new HashMap<>();
        for (UnitTaskProjectDetail detail : detailList){
            String key = detail.getEmpCode();
            List<UnitTaskProjectDetail> list = detailMap.computeIfAbsent(key, k -> new ArrayList<>());
            list.add(detail);
        }
        List<UnitTaskProjectDetailVo> rtnList = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList){
            String empCode1 = unitTaskUser.getEmpCode();
            UnitTaskProjectDetailVo vo = new UnitTaskProjectDetailVo();
            BeanUtils.copyProperties(unitTaskUser, vo);
            vo.setDetailList(detailMap.get(empCode1));
            rtnList.add(vo);
        }
        return rtnList;
    }

    private void sortDetail(Long progProjectId, List<UnitTaskProjectDetail> detailList) {
        if(CollUtil.isEmpty(detailList)){
            return;
        }
        //方案指标明细大项
        List<ProgProjectDetail> progProjectDetails =
                progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId,progProjectId).orderByAsc(ProgProjectDetail::getSortNum));
        Map<Long, Long> collect = progProjectDetails.stream().collect(Collectors.toMap(e -> e.getId(), e -> e.getSortNum(), (v1, v2) -> v1));
        detailList.sort((o1,o2)->{
            Long sort1 = collect.get(o1.getProgProjectDetailId());
            Long sort2 = collect.get(o2.getProgProjectDetailId());
            int compare = CompareUtil.compare(sort1, sort2);
            if (compare != 0) {
                return compare;
            }
            return CompareUtil.compare(o1.getProgProjectDetailId(), o2.getProgProjectDetailId());
        });
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDetail(UnitTaskProjectDetailSaveDTO unitTaskProjectDetailSaveDTO) {
        //保存project
        Long projectId = unitTaskProjectDetailSaveDTO.getProjectId();
        UnitTaskProject project = unitTaskProjectMapper.selectById(projectId);

        //int scale = project.getReservedDecimal();
        int scale = 2;
        RoundingMode roundingMode = CommonUtils.getCarryRule(project.getCarryRule());

        //保存detailList
        List<UnitTaskProjectDetail> detailList = unitTaskProjectDetailSaveDTO.getUserList().stream()
                .flatMap(unitTaskProjectDetailVo -> {
                    if(unitTaskProjectDetailVo.getDetailList() != null){
                        return  unitTaskProjectDetailVo.getDetailList().stream();
                    }
                   return Collections.<UnitTaskProjectDetail>emptyList().stream();
                })
                .collect(Collectors.toList());
        //计算detail amt
        for(UnitTaskProjectDetail detail : detailList){
            String modeType = CommonUtils.getDicVal(detail.getModeType());
            if(ModeType.qtyxprice.toString().equals(modeType)){
                BigDecimal amt = detail.getQty().multiply(detail.getPriceValue()).setScale(scale,roundingMode);
                detail.setAmt(amt);
            }else if(ModeType.input.toString().equals(modeType)){
                BigDecimal amt = detail.getQty().setScale(scale,roundingMode);
                detail.setAmt(amt);
            }
        }
        this.updateBatchById(detailList);

        //平均绩效 更新绩效倍数
        String projectType = CommonUtils.getDicVal(project.getProjectType());
        if(ProjectType.pinjun.toString().equals(projectType)){
            List<UnitTaskUser> userList = new ArrayList<>(unitTaskProjectDetailSaveDTO.getUserList());
            unitTaskUserService.updateBatchById(userList);
        }

        //计算projectCount
        unitTaskProjectCountService.doCount(project.getUnitTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveDetail2(UnitTaskProjectDetailSave2DTO unitTaskProjectDetailSave2DTO) {
        //保存project
        Long projectId = unitTaskProjectDetailSave2DTO.getProjectId();
        UnitTaskProject project = unitTaskProjectMapper.selectById(projectId);

        //保存detailList
        this.updateBatchById(unitTaskProjectDetailSave2DTO.getDetailList());

        //修改发放单元方案
        List<ProgProjectDetail> projectDetailList = new ArrayList<>();
        for (UnitTaskProjectDetail detail : unitTaskProjectDetailSave2DTO.getDetailList()){
            ProgProjectDetail projectDetail = progProjectDetailService.getById(detail.getProgProjectDetailId());
            projectDetail.setName(detail.getName());
            projectDetail.setErciRate(detail.getErciRate());
            projectDetail.setIfExtendLast(detail.getIfExtendLast());
            projectDetailList.add(projectDetail);
        }
        if(!projectDetailList.isEmpty()){
            progProjectDetailService.updateBatchById(projectDetailList);
        }

        //计算projectCount
        unitTaskProjectCountService.doCount(project.getUnitTaskId());
    }

    @Override
    public void addByProgDetailList(UnitTaskProject unitTaskProject, List<UnitTaskUser> userList, List<ProgProjectDetail> progProjectDetailList) {
        List<UnitTaskProjectDetail> addList = new ArrayList<>();
        String userIds = userList.stream().map(UnitTaskUser::getUserId).collect(Collectors.joining(","));

        UnitTask unitTask = unitTaskMapper.selectById(unitTaskProject.getUnitTaskId());
        String cycle = unitTask.getCycle();
        GrantUnit grantUnit = grantUnitService.getById(unitTask.getGrantUnitId());
        String deptIds = grantUnit.getKsUnitIds();

        for (ProgProjectDetail progProjectDetail : progProjectDetailList) {
            // 是否需要采集数据 key userid
            Map<String, List<ItemValueDTO>> userItemValueMap = new HashMap<>();
            String inputType = CommonUtils.getDicVal(progProjectDetail.getInputType());
            String modeType = CommonUtils.getDicVal(progProjectDetail.getModeType());
            if (InputType.auto.toString().equals(inputType)) {
                List<ItemValueDTO> itemValueDTOList = secondKpiService.accountItemValue(cycle, deptIds, userIds, progProjectDetail.getAccountItemCode(), progProjectDetail.getAccountItemType());
                userItemValueMap = itemValueDTOList.stream().collect(Collectors.groupingBy(ItemValueDTO::getUserId));
            }
            // 上一次的人员项目数据
            List<UnitTaskProjectDetail> userDetailValueList = this.getLastDetailValue(unitTaskProject.getId(), unitTaskProject.getProgProjectId(), progProjectDetail);
            Map<String, UnitTaskProjectDetail> userDetailValueMap = userDetailValueList.stream().collect(Collectors.toMap(UnitTaskProjectDetail::getEmpCode, item -> item, (v1, v2) -> v2));
            for (UnitTaskUser unitTaskUser : userList) {
                String empCode = unitTaskUser.getEmpCode();
                String userId = unitTaskUser.getUserId();
                UnitTaskProjectDetail unitTaskProjectDetail = new UnitTaskProjectDetail();
                BeanUtils.copyProperties(progProjectDetail, unitTaskProjectDetail);

                unitTaskProjectDetail.setId(null);
                unitTaskProjectDetail.setEmpCode(empCode);
                unitTaskProjectDetail.setUnitTaskProjectId(unitTaskProject.getId());
                unitTaskProjectDetail.setProgProjectDetailId(progProjectDetail.getId());
                if (InputType.auto.toString().equals(inputType)) {
                    // 采集方式走数据小组接口
                    List<ItemValueDTO> itemValueDTOList = userItemValueMap.get(userId);
                    if (itemValueDTOList != null) {
                        BigDecimal itemValue = itemValueDTOList.stream().map(o->(o.getItemValue() == null?BigDecimal.ZERO:new BigDecimal(o.getItemValue()))).reduce(BigDecimal.ZERO,BigDecimal::add);
                        unitTaskProjectDetail.setQty(itemValue);
                    }
                } else if (InputType.input.toString().equals(inputType)) {
                    // 继承上个月 手工上报数量（手工填报方式）
                    if ("1".equals(progProjectDetail.getIfExtendLast())) {
                        UnitTaskProjectDetail lastDetail = userDetailValueMap.get(empCode);
                        if (lastDetail != null) {
                            unitTaskProjectDetail.setQty(lastDetail.getQty());
                        }
                    }
                }
                if (Objects.equals(ModeType.qtyxprice.toString(), modeType)) {// 继承上个月量
                    // 继承上个月 每数量单位标准（按数量*标准计算方式）
                    UnitTaskProjectDetail lastDetail = userDetailValueMap.get(empCode);
                    if (lastDetail != null) {
                        unitTaskProjectDetail.setPriceValue(lastDetail.getPriceValue());
                    }
                }
                // 计算金额
                this.setDetailAmt(progProjectDetail.getModeType(), unitTaskProjectDetail, unitTaskProject.getCarryRule());

                addList.add(unitTaskProjectDetail);
            }
        }
        if (!addList.isEmpty()) {
            this.saveBatch(addList);
        }
    }

    private void setDetailAmt(String modeType, UnitTaskProjectDetail unitTaskProjectDetail, String carryRule) {
        String itemModeType = CommonUtils.getDicVal(modeType);
        if(ModeType.input.toString().equals(itemModeType)){//直接输入金额
            unitTaskProjectDetail.setAmt(unitTaskProjectDetail.getQty());
        }else{//金额 = 数量 * 单价
            if(unitTaskProjectDetail.getQty() != null) {
                BigDecimal amt = unitTaskProjectDetail.getQty().multiply(unitTaskProjectDetail.getPriceValue());
                amt = CommonUtils.amtSetScale(amt, 6, carryRule);
                unitTaskProjectDetail.setAmt(amt);
            }
        }
    }

    private List<UnitTaskProjectDetail> getLastDetailValue(Long projectId, Long progProjectId, ProgProjectDetail progProjectDetail) {
        //查询最近一笔 progProjectId 所对应的 unitProjectId
        UnitTaskProject unitTaskProject = unitTaskProjectMapper.selectOne(Wrappers.<UnitTaskProject>lambdaQuery()
                .eq(UnitTaskProject::getProgProjectId,progProjectId)
                .ne(UnitTaskProject::getId,projectId)
                .orderByDesc(UnitTaskProject::getCreateTime)
                .last("limit 1"));
        //查询 unitProjectId 下的 所有 progProjectDetail对应的unitDetail的数据
        if(unitTaskProject != null){
            return this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                    .eq(UnitTaskProjectDetail::getUnitTaskProjectId,unitTaskProject.getId())
                    .eq(UnitTaskProjectDetail::getProgProjectDetailId,progProjectDetail.getId()));
        }
        return new ArrayList<>();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveProgDetail(ProgProjectDetailSaveDTO progProjectDetailSaveDTO) {
        Long unitTaskProjectId = progProjectDetailSaveDTO.getUnitTaskProjectId();
        UnitTaskProject unitTaskProject = unitTaskProjectMapper.selectById(unitTaskProjectId);
        Long unitTaskId = unitTaskProject.getUnitTaskId();
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);

        List<ProgProjectDetail> delList = new ArrayList<>();
        List<ProgProjectDetail> addList = new ArrayList<>();
        List<ProgProjectDetail> updateList = new ArrayList<>();
        for (ProgProjectDetail progProjectDetail : progProjectDetailSaveDTO.getDetailList()) {
            String actionType = progProjectDetail.getActionType();
            //新增的项目
            if(ActionType.add.toString().equals(actionType)){
                addList.add(progProjectDetail);
            }else if(ActionType.edit.toString().equals(actionType)){//修改的项目
                // 如果修改了数据来源、核算项、是否继承上月，则删除再添加
                ProgProjectDetail oldDetail = progProjectDetailService.getById(progProjectDetail.getId());
                if (changeNeedInit(oldDetail, progProjectDetail)) {
                    addList.add(progProjectDetail);
                    delList.add(progProjectDetail);
                } else {
                    updateList.add(progProjectDetail);
                }

            }else  if(ActionType.del.toString().equals(actionType)){//删除的项目
                delList.add(progProjectDetail);
            }
        }

        //先处理删除
        if(!delList.isEmpty()) {
            this.delByUnitTaskProject(unitTask, unitTaskProject, delList);
        }
        //处理新增
        if(!addList.isEmpty()) {
            this.addByUnitTaskProject(unitTask, unitTaskProject, addList);
        }
        // 处理更新
        if (!updateList.isEmpty()) {
            this.updateByUnitTaskProject(unitTask, unitTaskProject, updateList);
        }

        //计算projectCount
        unitTaskProjectCountService.doCount(unitTaskId);
    }

    private void updateByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, List<ProgProjectDetail> updateList) {
        Long unitTaskId = unitTask.getId();
        // 更新方案detail
        progProjectDetailService.updateBatchById(updateList);

        // 更新任务detail
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId, unitTaskId));
        this.updateByProgDetailListFromUserTask(unitTaskProject, userList, updateList);
    }

    private void updateByProgDetailListFromUserTask(UnitTaskProject unitTaskProject, List<UnitTaskUser> userList, List<ProgProjectDetail> updateList) {

        for (ProgProjectDetail progProjectDetail : updateList) {// 先循环item
            Long progProjectDetailId = progProjectDetail.getId();
            // 获取progDetailItem下所有UnitTaskDetailItem
            List<UnitTaskProjectDetail> list = this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getProgProjectId, progProjectDetail.getProgProjectId())
                    .eq(UnitTaskProjectDetail::getProgProjectDetailId, progProjectDetailId));
            for (UnitTaskProjectDetail unitTaskDetail : list) {
                // 工作量 类型的 progDetailItem 更新，需要联动更新 unitTaskDetailItem 的值
                unitTaskDetail.setModeType(progProjectDetail.getModeType());
                unitTaskDetail.setPriceValue(progProjectDetail.getPriceValue());
                unitTaskDetail.setInputType(progProjectDetail.getInputType());
                unitTaskDetail.setIfExtendLast(progProjectDetail.getIfExtendLast());
                unitTaskDetail.setName(progProjectDetail.getName());
                this.setDetailAmt(progProjectDetail.getModeType(), unitTaskDetail, unitTaskProject.getCarryRule());
            }
            if (CollUtil.isNotEmpty(list)) {
                this.updateBatchById(list);
            }
        }
    }

    private boolean changeNeedInit(ProgProjectDetail oldDetail, ProgProjectDetail newDetail) {
        log.info("UnitTaskProjectDetailService changeNeedInit oldDetail:{}", JSON.toJSONString(oldDetail));
        log.info("UnitTaskProjectDetailService changeNeedInit newDetail{}", JSON.toJSONString(newDetail));
        boolean equals1 = Objects.equals(CommonUtils.getDicVal(oldDetail.getInputType()), CommonUtils.getDicVal(newDetail.getInputType()));
        boolean equals2 = Objects.equals(oldDetail.getAccountItemId(), newDetail.getAccountItemId());
        boolean equals3 = Objects.equals(oldDetail.getAccountItemCode(), newDetail.getAccountItemCode());
        // boolean equals3 = StrUtil.equals(oldItem.getIfExtendLast(), newItem.getIfExtendLast());
        log.info("UnitTaskProjectDetailService changeNeedInit changeNeedInit {},{}", equals1, equals2);
        // 有一个不同则更新detail
        return !equals1 || !equals2 || !equals3;
    }

    // @Override
    // @Transactional(rollbackFor = Exception.class)
    // public void updateProgDetailIndex(ProgProjectDetailSaveDTO progProjectDetailSaveDTO) {
    //     for (int i = 0; i < progProjectDetailSaveDTO.getDetailList().size(); i++) {
    //         progProjectDetailSaveDTO.getDetailList().get(i).setSortNum(i + 1);
    //     }
    //     progProjectDetailService.updateBatchById(progProjectDetailSaveDTO.getDetailList());
    // }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delByProjectId(List<Long> projectIds) {
        this.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().in(UnitTaskProjectDetail::getUnitTaskProjectId,projectIds));
        unitTaskDetailItemService.delByProjectId(projectIds);
        unitTaskDetailItemWorkService.delByProjectId(projectIds);
    }

    @Override
    public void syncByProject(String cycle,UnitTaskProject unitTaskProject) {
        String projectType = CommonUtils.getDicVal(unitTaskProject.getProjectType());
        if(ProjectType.erci.toString().equals(projectType)){//科室二次分配，继续往下同步
            this.syncByProjectErci(cycle,unitTaskProject);
        }else{//其他 同步detail 结束
            this.syncByProjectOther(unitTaskProject);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProgDetail2(UnitTaskProjectDetail unitTaskProjectDetail) {
        UnitTaskProject taskProject = unitTaskProjectMapper.selectById(unitTaskProjectDetail.getUnitTaskProjectId());
        Long progProjectId = taskProject.getProgProjectId();

        //新增方案detail
        ProgProjectDetail progProjectDetail = new ProgProjectDetail();
        BeanUtils.copyProperties(unitTaskProjectDetail, progProjectDetail);
        progProjectDetail.setId(null);
        progProjectDetail.setProgProjectId(progProjectId);
        progProjectDetailService.save(progProjectDetail);

        //新增任务detail
        unitTaskProjectDetail.setProgProjectDetailId(progProjectDetail.getId());
        this.save(unitTaskProjectDetail);

        String modeTpe = CommonUtils.getDicVal(unitTaskProjectDetail.getModeType());
        // 如果是工作量项目，则需要新增工作量系数数据
        if (Objects.equals(ModeType.work.toString(), modeTpe)) {
            unitTaskDetailItemWorkService.initUserData(taskProject.getUnitTaskId(), unitTaskProjectDetail, null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delProgDetail2(Long taskDetailId) {
        UnitTaskProjectDetail taskDetail = this.getById(taskDetailId);
        UnitTaskProject taskProject = unitTaskProjectMapper.selectById(taskDetail.getUnitTaskProjectId());
        Long progDetailtId = taskDetail.getProgProjectDetailId();

        //删除方案detail
        progProjectDetailService.delErciById(progDetailtId);

        //新增任务detail
        this.removeById(taskDetailId);
        unitTaskDetailItemService.remove(Wrappers.<UnitTaskDetailItem>lambdaQuery().eq(UnitTaskDetailItem::getUnitTaskProjectDetailId,taskDetailId));
        unitTaskDetailItemWorkService.remove(Wrappers.<UnitTaskDetailItemWork>lambdaQuery().eq(UnitTaskDetailItemWork::getUnitTaskProjectDetailId, taskDetailId));

        //计算projectCount
        unitTaskProjectCountService.doCount(taskProject.getUnitTaskId());

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addProgDetail2Batch(ProgProjectDetail2SaveBatchDTO progProjectDetail2SaveBatchDTO) {
        for (UnitTaskProjectDetail unitTaskProjectDetail : progProjectDetail2SaveBatchDTO.getDetailList()){
            this.addProgDetail2(unitTaskProjectDetail);
        }
    }

    @Override
    public void exportDanxiang(Long unitTaskProjectId,HttpServletResponse response){
        try(OutputStream out = response.getOutputStream();){
            UnitTaskProject project = unitTaskProjectMapper.selectById(unitTaskProjectId);
            Long progProjectId = project.getProgProjectId();
            List<ProgProjectDetail> progDetailList = progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId,progProjectId));
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(project.getName());
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            //查全部
            table.setHead(getHead(progDetailList));
            // 写数据
            List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,project.getUnitTaskId()));
            writer.write(getContent(userList), sheet1, table);
            writer.finish();
        }catch (Exception e){

        }

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVo importDanxiang(Long unitTaskProjectId, String[][] xlsDataArr) {
        UnitTaskProject project = unitTaskProjectMapper.selectById(unitTaskProjectId);
        List<ProgProjectDetail> progDetailList = progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery()
                .eq(ProgProjectDetail::getProgProjectId,project.getProgProjectId())
                .orderByAsc(ProgProjectDetail::getId));
        List<UnitTaskUser> userList = unitTaskUserService.listByTaskId(project.getUnitTaskId());
        Map<String,UnitTaskUser> userMap = userList.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode,item->item, (v1, v2) -> v2));

        //逐条检验，成功的落库 和 失败的入redis
        List<String> errorList = new ArrayList<>();
        //key  empCode-detailName  ,value = qty
        Map<String,String> dataMap = new HashMap<>();
        for (int row=0;row< xlsDataArr.length;row++){
            String[] data = xlsDataArr[row];
            //姓名
            //String name = data[0];
            //工号
            String empCode = data[1];
            //校验row人员是否存在
            this.importDanxiangUserValid(errorList,empCode,row,"工号",userMap);
            int colIndex = 2;
            for (ProgProjectDetail progDetail : progDetailList){
                //跳过采集型
                /*if(InputType.auto.toString().equals(CommonUtils.getDicVal(progDetail.getInputType()))){
                    continue;
                }*/
                String value = data[colIndex++];
                String detailName = progDetail.getName();
                //校验是否数字
                if(this.importDanxiangValid(errorList,value,row,detailName)){
                    dataMap.put(empCode+"-"+detailName,value);
                }
            }
        }

        //导入数据，找不到就赋0
        List<UnitTaskProjectDetail> detailList = this.listByPid(unitTaskProjectId);
        for (UnitTaskProjectDetail detail : detailList){
            //跳过采集型
            /*if(InputType.auto.toString().equals(CommonUtils.getDicVal(detail.getInputType()))){
                continue;
            }*/
            String empCode = detail.getEmpCode();
            String detailName = detail.getName();
            String value = dataMap.get(empCode+"-"+detailName);
            if(value == null){
                detail.setQty(BigDecimal.ZERO);
            }else {
                detail.setQty(new BigDecimal(value));
            }
            //设置金额
            this.setDetailAmt(detail.getModeType(),detail,project.getCarryRule());
        }
        //批量修改
        this.updateBatchById(detailList);

        //计算projectCount
        unitTaskProjectCountService.doCount(project.getUnitTaskId());

        //错误消息入redis
        if(!errorList.isEmpty()){
            redisUtil.set(CacheConstants.SEC_IMPORT_ERRLOG +unitTaskProjectId,errorList,30, TimeUnit.MINUTES);
        }

        //导入消息
        ImportResultVo vo = new ImportResultVo();
        vo.setTotalCount(xlsDataArr.length);
        if(!errorList.isEmpty()){
            vo.setErrorCount(errorList.size());
        }
        return vo;
    }

    @Override
    public List<UnitTaskProjectDetail> listByUnitTask(Long unitTaskId) {
        //获得所有project
        List<UnitTaskProject> projectList = unitTaskProjectService.listByUnitTask(unitTaskId);
        if(projectList.isEmpty()){
            return new ArrayList<>();
        }
        List<Long> projectIds = projectList.stream().map(UnitTaskProject::getId).collect(Collectors.toList());

        return this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery()
                .in(UnitTaskProjectDetail::getUnitTaskProjectId, projectIds)
                .isNull(UnitTaskProjectDetail::getEmpCode)
        );
    }

    @Override
    public void removeByTaskProjectIds(List<Long> unitTaskProjectIds) {
        remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().in(UnitTaskProjectDetail::getUnitTaskProjectId, unitTaskProjectIds));
    }

    @Override
    public List<ProgProjectDetail> getProgDetailList(Long unitTaskId, Long unitProjectId) {
        List<ProgProjectDetail> rtn = new ArrayList<>();
        List<UnitTaskProjectDetail> detailList = this.listByPid(unitProjectId);
        Map<Long,ProgProjectDetail> distinctMap = new HashMap<>();
        for (UnitTaskProjectDetail detail : detailList){
            Long progProjectDetailId = detail.getProgProjectDetailId();
            if (distinctMap.get(progProjectDetailId) != null){
                continue;
            }
            ProgProjectDetail progProjectDetail = new ProgProjectDetail();
            BeanUtils.copyProperties(detail, progProjectDetail);
            progProjectDetail.setId(progProjectDetailId);
            rtn.add(progProjectDetail);
            distinctMap.put(progProjectDetailId,progProjectDetail);
        }
        return rtn;
    }

    private List<UnitTaskProjectDetail> listByPid(Long unitTaskProjectId) {
        return this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,unitTaskProjectId)
                .orderByAsc(UnitTaskProjectDetail::getSortNum,UnitTaskProjectDetail::getProgProjectDetailId));
    }

    private Boolean importDanxiangUserValid(List<String> errorList, String empCode, int row, String colName, Map<String, UnitTaskUser> userMap) {
        row++;
        if(userMap.get(empCode) == null){
            errorList.add("原文件第"+row+"行，"+colName+"["+empCode+"]不存在或匹配不上");
            return false;
        }
        return true;
    }

    private Boolean importDanxiangValid(List<String> errorList, String qty,int row,String colName) {
        row++;
        if(StringUtils.isBlank(qty)){
            errorList.add("原文件第"+row+"行，"+colName+"[未填写]错误");
            return false;
        }
        try {
            new BigDecimal(qty);
        }catch (Exception e){
            errorList.add("原文件第"+row+"行，"+colName+"["+qty+"]错误");
            return false;
        }
        return true;
    }

    /**
     * 返回内容
     *
     * @param
     * @return 参数
     */
    private List<List<Object>> getContent(List<UnitTaskUser> userList){
        List<List<Object>> totalContent = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList) {
            List<Object> list = new ArrayList<>();
            list.add(unitTaskUser.getEmpName());
            list.add(unitTaskUser.getEmpCode());
            totalContent.add(list);
        }
        return totalContent;
    }

    /**
     * 表头
     *
     * @param progDetailList
     * @return
     */
    public List<List<String>> getHead(List<ProgProjectDetail> progDetailList) {
        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> name = new ArrayList<>();
        name.add("姓名");
        List<String> empCode = new ArrayList<>();
        empCode.add("工号");
        total.add(name);
        total.add(empCode);

        //指标
        int i = 0;
        for (ProgProjectDetail detail : progDetailList) {
            //跳过采集型
            if(InputType.auto.toString().equals(CommonUtils.getDicVal(detail.getInputType()))){
                continue;
            }
            i++;
            String modeType = CommonUtils.getDicVal(detail.getModeType());

            List<String> detailHead = new ArrayList<>();
            detailHead.add(detail.getName());
            if(ModeType.qtyxprice.toString().equals(modeType)){
                detailHead.add(i%2==0?"数量":"数 量");
            }else{
                detailHead.add(i%2==0?"合计（元）":"合计 （元）");
            }
            total.add(detailHead);
        }

        return total;
    }

    private void syncByProjectOther(UnitTaskProject unitTaskProject) {
//        if(unitTaskProject.getId() == 1772926364813529090L){
//            System.out.println(111);
//        }
        //非科室二次分配使用，每个人每个detail 都有一笔数据
        //progDetail 和 taskDetail（每个人都有一笔） 对比，根据增删改分类
        Long progProjectId = unitTaskProject.getProgProjectId();
        Long taskProjectId = unitTaskProject.getId();
        Long unitTaskId = unitTaskProject.getUnitTaskId();
        List<ProgProjectDetail> progDetailList = progProjectDetailService.list(Wrappers.<ProgProjectDetail>lambdaQuery().eq(ProgProjectDetail::getProgProjectId,progProjectId));
        List<UnitTaskProjectDetail> taskDetailList = this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,taskProjectId));
        //从taskDetailList  找出 现在在用的 detail，将其中方案detail的冗余字段组成一个ProgProjectDetail，再和progDetailList进行比较，找出增删改集合
        List<ProgProjectDetail> currentProgDetailList = this.task2progDetail(taskDetailList);

        //新增的集合，为每个人增出detail
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        List<ProgProjectDetail> addList = progDetailList.stream()
                .filter(progDetail -> currentProgDetailList.stream().map(ProgProjectDetail::getId).noneMatch(progDetailId -> Objects.equals(progDetailId,progDetail.getId())))
                .collect(Collectors.toList());
        //this.addByProgDetailList(unitTaskProject,userList,addList); 和 修改集合合并后执行

        //删除的集合
        List<ProgProjectDetail> delList = currentProgDetailList.stream()
                .filter(progDetail -> progDetailList.stream().map(ProgProjectDetail::getId).noneMatch(id -> Objects.equals(id,progDetail.getId())))
                .collect(Collectors.toList());
        List<Long> delProgDetailIds = delList.stream().map(ProgProjectDetail::getId).collect(Collectors.toList());
        if (!delProgDetailIds.isEmpty()) {
            this.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId, taskProjectId).in(UnitTaskProjectDetail::getProgProjectDetailId, delProgDetailIds));
        }
        //修改的集合 ,比较任务数据中冗余的方案字段  和 方案字段是否相同。只要有一个不同，则重新获取数据(删除再创建)；都相同 则跳过
        List<ProgProjectDetail> editList = this.getSyncByProjectOtherEditList(progDetailList,currentProgDetailList);
        List<Long> editProgDetailIds = editList.stream().map(ProgProjectDetail::getId).collect(Collectors.toList());
        if(!editProgDetailIds.isEmpty()){
            this.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,taskProjectId).in(UnitTaskProjectDetail::getProgProjectDetailId,editProgDetailIds));
        }
        List<ProgProjectDetail> mergedList = new ArrayList<>(addList);
        mergedList.addAll(editList);
        this.addByProgDetailList(unitTaskProject,userList,mergedList);

    }

    private List<ProgProjectDetail> getSyncByProjectOtherEditList(List<ProgProjectDetail> progDetailList, List<ProgProjectDetail> currentProgDetailList) {
        List<ProgProjectDetail> editList = new ArrayList<>();
        for (ProgProjectDetail progProjectDetail : progDetailList) {
            for (ProgProjectDetail currentProgProjectDetail : currentProgDetailList) {
                System.out.println(progProjectDetail.getId()+"  "+currentProgProjectDetail.getId());
                System.out.println(Objects.equals(progProjectDetail.getId(), currentProgProjectDetail.getId()));
                if (Objects.equals(progProjectDetail.getId(), currentProgProjectDetail.getId()) && !ifProgDetailEquals(progProjectDetail,currentProgProjectDetail)) {
                    editList.add(progProjectDetail);
                    break;
                }
            }
        }
        return editList;
    }

    private Boolean ifProgDetailEquals(ProgProjectDetail progProjectDetail, ProgProjectDetail currentProgProjectDetail) {
        if(!Objects.equals(progProjectDetail.getName(), currentProgProjectDetail.getName())
                || !Objects.equals(progProjectDetail.getPriceValue(), currentProgProjectDetail.getPriceValue())
                || !Objects.equals(progProjectDetail.getInputType(), currentProgProjectDetail.getInputType())
                || !Objects.equals(progProjectDetail.getAccountItemId(), currentProgProjectDetail.getAccountItemId())
                || !Objects.equals(progProjectDetail.getIfExtendLast(), currentProgProjectDetail.getIfExtendLast())
            ){
            return false;
        }
        return true;
    }

    private List<ProgProjectDetail> task2progDetail(List<UnitTaskProjectDetail> taskDetailList) {
        Map<Long,ProgProjectDetail> map = new HashMap<>();//key : progDetailItemId
        for (UnitTaskProjectDetail taskProjectDetail : taskDetailList){
            Long progDetailId = taskProjectDetail.getProgProjectDetailId();
            if(map.get(progDetailId) == null){
                ProgProjectDetail progDetail = new ProgProjectDetail();
                BeanUtils.copyProperties(taskProjectDetail, progDetail);
                progDetail.setId(taskProjectDetail.getProgProjectDetailId());
                map.put(progDetailId,progDetail);
            }
        }
        return new ArrayList<>(map.values());
    }

    private void syncByProjectErci(String cycle,UnitTaskProject unitTaskProject) {
        //progDetail 和 taskDetail 对比，根据增删改分类
        Long progProjectId = unitTaskProject.getProgProjectId();
        Long taskProjectId = unitTaskProject.getId();

        List<ProgProjectDetail> progDetailList = progProjectDetailService.listByPidCache(cycle,progProjectId);
        List<UnitTaskProjectDetail> taskDetailList = this.list(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,taskProjectId));

        //新增的集合
        List<ProgProjectDetail> addList = progDetailList.stream()
                .filter(progDetail -> taskDetailList.stream().map(UnitTaskProjectDetail::getProgProjectDetailId).noneMatch(progDetailId -> Objects.equals(progDetailId,progDetail.getId())))
                .collect(Collectors.toList());
        String projectType = CommonUtils.getDicVal(unitTaskProject.getProjectType());
        this.createByProgDetailList(cycle, unitTaskProject.getUnitTaskId(),taskProjectId, addList,projectType);

        //删除的集合
        List<UnitTaskProjectDetail> delList = taskDetailList.stream()
                .filter(taskDetail -> progDetailList.stream().map(ProgProjectDetail::getId).noneMatch(id -> Objects.equals(id,taskDetail.getProgProjectDetailId())))
                .collect(Collectors.toList());
        this.delByDetailId(delList);

        //修改的集合 ,往下处理 detail
        List<UnitTaskProjectDetail> updateList = taskDetailList.stream()
                .filter(taskDetail -> delList.stream().noneMatch(delDetail -> Objects.equals(delDetail.getId(),taskDetail.getId())))
                .collect(Collectors.toList());
        this.updateByProgDetailList(progDetailList,updateList);
        updateList.forEach(o->unitTaskDetailItemService.syncByDetail(cycle,o));
    }

    private void updateByProgDetailList(List<ProgProjectDetail> comList, List<UnitTaskProjectDetail> updateList) {
        Map<Long,ProgProjectDetail> comMap = comList.stream().collect(Collectors.toMap(ProgProjectDetail::getId, item->item, (v1, v2) -> v2));
        for (UnitTaskProjectDetail detail : updateList){
            Long commonId = detail.getProgProjectDetailId();
            ProgProjectDetail comDetail = comMap.get(commonId);
            detail.setName(comDetail.getName());
            detail.setModeType(comDetail.getModeType());
            detail.setPriceValue(comDetail.getPriceValue());
            detail.setInputType(comDetail.getInputType());
            detail.setAccountItemId(comDetail.getAccountItemId());
            detail.setAccountItemCode(comDetail.getAccountItemCode());
            detail.setAccountItemName(comDetail.getAccountItemName());
            detail.setIfExtendLast(comDetail.getIfExtendLast());
            detail.setErciRate(comDetail.getErciRate());
            detail.setIfCareWorkdays(comDetail.getIfCareWorkdays());
            detail.setIfParentItemValueAdd(comDetail.getIfParentItemValueAdd());
            detail.setIfItemValueAdd(comDetail.getIfItemValueAdd());
        }
        if(!updateList.isEmpty()){
            this.updateBatchById(updateList);
        }
    }

    private void delByDetailId(List<UnitTaskProjectDetail> delList) {
        List<Long> detailIds = delList.stream().map(UnitTaskProjectDetail::getId).collect(Collectors.toList());
        if(!detailIds.isEmpty()) {
            this.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().in(UnitTaskProjectDetail::getId, detailIds));
        }
        unitTaskDetailItemService.delByDetailId(detailIds);
        unitTaskDetailItemWorkService.delByDetailId(detailIds);
    }

    private void createByProgDetailList(String cycle, Long taskId, Long taskProjectId, List<ProgProjectDetail> addList, String projectType) {
        for (ProgProjectDetail progProjectDetail : addList) {
            UnitTaskProjectDetail unitTaskProjectDetail = new UnitTaskProjectDetail();
            BeanUtils.copyProperties(progProjectDetail, unitTaskProjectDetail);

            unitTaskProjectDetail.setId(null);
            unitTaskProjectDetail.setUnitTaskProjectId(taskProjectId);
            unitTaskProjectDetail.setProgProjectDetailId(progProjectDetail.getId());
            //addList.add(unitTaskProjectDetail);
            this.save(unitTaskProjectDetail);

            if (ProjectType.erci.toString().equals(projectType)) { //科室二次分配新增 为user增出 detailItem
                unitTaskDetailItemService.initUserData(taskId, unitTaskProjectDetail,null);
                unitTaskDetailItemWorkService.initUserData(taskId, unitTaskProjectDetail, null);
            }
        }
    }

    private void addByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, List<ProgProjectDetail> addList) {
        Long unitTaskId = unitTask.getId();
        //新增方案detail
        Long progProjectId = unitTaskProject.getProgProjectId();
        for (ProgProjectDetail progProjectDetail: addList){
            progProjectDetail.setId(null);
            progProjectDetail.setProgProjectId(progProjectId);
        }
        progProjectDetailService.saveBatch(addList);

        //新增任务detail
        List<UnitTaskUser> userList = unitTaskUserService.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        this.addByProgDetailList(unitTaskProject,userList,addList);

    }

    private void delByUnitTaskProject(UnitTask unitTask, UnitTaskProject unitTaskProject, List<ProgProjectDetail> progDelList) {
        Long unitTaskProjectId = unitTaskProject.getId();
        //删除任务detail
        List<Long> progDetailIdList = progDelList.stream()
                .map(ProgProjectDetail::getId)
                .collect(Collectors.toList());
        this.remove(Wrappers.<UnitTaskProjectDetail>lambdaQuery().eq(UnitTaskProjectDetail::getUnitTaskProjectId,unitTaskProjectId).in(UnitTaskProjectDetail::getProgProjectDetailId,progDetailIdList));

        //删除方案detail
        progProjectDetailService.removeBatchByIds(progDelList);
    }


}
