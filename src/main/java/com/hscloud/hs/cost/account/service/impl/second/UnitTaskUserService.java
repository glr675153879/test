package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.second.UserType;
import com.hscloud.hs.cost.account.mapper.second.ProgrammeMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskMapper;
import com.hscloud.hs.cost.account.mapper.second.UnitTaskUserMapper;
import com.hscloud.hs.cost.account.model.dto.second.DmoUserPageDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserAddBatchDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskUserEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UserTypeVo;
import com.hscloud.hs.cost.account.service.ICostUnitRelateInfoService;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.second.*;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
* 发放单元任务人员 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UnitTaskUserService extends ServiceImpl<UnitTaskUserMapper, UnitTaskUser> implements IUnitTaskUserService {

    private final IGrantUnitService grantUnitService;

    private final ICostUnitRelateInfoService costUnitRelateInfoService;
    private final IAttendanceService attendanceService;

    private final UnitTaskMapper unitTaskMapper;
    private final ProgrammeMapper programmeMapper;

    private final UnitTaskUserOperateService unitTaskUserOperateService;

    @Lazy
    @Autowired
    private UnitTaskService unitTaskService;

    private final DmoUtil dmoUtil;
    private final SecondKpiService secondKpiService;
    @Lazy
    @Autowired
    private IUnitTaskProjectService unitTaskProjectService;

    @Lazy
    @Autowired
    private IUnitTaskProjectCountService unitTaskProjectCountService;

    @Lazy
    @Autowired
    private IUnitTaskCountService unitTaskCountService;

    @Lazy
    @Autowired
    private ISecondTaskCountService secondTaskCountService;


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInit(UnitTask unitTask) {
        //中台获取 科室单元下的科室下的人员   todo 科室单元下的人员怎么办？
        List<UnitTaskUser> dmoUserList = this.getDmoUserList(unitTask);
        //继承上个月增删人员
        dmoUserList =  unitTaskUserOperateService.listByTaskIdCycleUnitId(unitTask,dmoUserList);
        this.createInitBatch(unitTask,dmoUserList);
    }

    private List<UnitTaskUser> getDmoUserList(UnitTask unitTask) {
        //获取发放单元下的 发放科室
        Long grantUnitId = unitTask.getGrantUnitId();
        GrantUnit grantUnit = grantUnitService.getById(grantUnitId);
        String deptIds = grantUnit.getKsUnitIds();
        //不含编外的科室
        String deptIdsNonStaff = grantUnit.getKsUnitIdsNonStaff();
        //额外人员
        String extraUserIds = grantUnit.getExtraUserIds();

        //找方案
        Long programmeId = unitTask.getProgrammeId();
        Programme programme = programmeMapper.selectById(programmeId);
        //是否剔除管理层
        List<String> leaderIds = new ArrayList<>();
        String ifContainLeader = programme.getIfContainLeader();
        if(Objects.equals(ifContainLeader,"0")){
            //管理层
            try {
                List<UnitTaskUser> leaderList = new ArrayList<>();
                if(deptIds != null){
                    List<UnitTaskUser> leaderList1 = secondKpiService.deptLeaderList(unitTask.getCycle(),deptIds);
                    if(!CollectionUtils.isEmpty(leaderList1)) {
                        leaderList.addAll(leaderList1);
                    }
                }
                if (deptIdsNonStaff != null){
                   List<UnitTaskUser> leaderNonStaffList = secondKpiService.deptLeaderList(unitTask.getCycle(),deptIdsNonStaff);
                    if(!CollectionUtils.isEmpty(leaderNonStaffList)) {
                        leaderList.addAll(leaderNonStaffList);
                    }
                }
                leaderIds = leaderList.stream().map(UnitTaskUser::getUserId).collect(Collectors.toList());
            }catch (Exception e){
                log.error("获取管理层人失败",e);
            }

        }
        String cycle = unitTask.getCycle();
        cycle = cycle.replace("-","");
        List<Attendance> attendanceList = new ArrayList<>();
        //查询科室含编外人员信息
        if(StrUtil.isNotBlank(deptIds)) {
            List<String> deptIdList = Arrays.stream(deptIds.split(",")).collect(Collectors.toList());
            List<Attendance> attendanceList1 = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                    .in(Attendance::getAccountUnitId, deptIdList)
                    .eq(Attendance::getCycle, cycle));
            if(!CollectionUtils.isEmpty(attendanceList1)) {
                attendanceList.addAll(attendanceList1);
            }
        }
        //查询科室不含编外人员信息，踢掉编外人员
        if(StrUtil.isNotBlank(deptIdsNonStaff)){
            //编外人员
            List<UserTypeVo> userInStaffVos = secondKpiService.queryUserWorkTypeList(unitTask.getCycle(), UserType.Y.getCode(),deptIdsNonStaff);
            List<String> userInStaffIds = userInStaffVos.stream().map(UserTypeVo::getUserId).collect(Collectors.toList());
            List<String> deptIdInStaffList = Arrays.stream(deptIdsNonStaff.split(",")).collect(Collectors.toList());
            List<Attendance> attendanceNoStaffList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                    .in(Attendance::getAccountUnitId,deptIdInStaffList)
                    .eq(Attendance::getCycle,cycle)
                    .notIn(!CollectionUtils.isEmpty(userInStaffIds),Attendance::getUserId,userInStaffIds));
            if(!CollectionUtils.isEmpty(attendanceNoStaffList)){
                //加入科室编内人员的信息
                attendanceList.addAll(attendanceNoStaffList);
            }
        }
        //加入额外人员
        if(StrUtil.isNotBlank(extraUserIds)){
            List<String> extraUserIdList = Arrays.stream(extraUserIds.split(",")).collect(Collectors.toList());
            List<Attendance> extraUserList = attendanceService.list(Wrappers.<Attendance>lambdaQuery()
                    .in(Attendance::getUserId,extraUserIdList)
                    .eq(Attendance::getCycle,cycle));
            if(!CollectionUtils.isEmpty(extraUserList)){
                //加入额外人员
                attendanceList.addAll(extraUserList);
            }
        }

        List<UnitTaskUser> userList = new ArrayList<>();
        Map<Long,UnitTaskUser> unitTaskUserMap = new HashMap<>();
        for (Attendance attendance : attendanceList){
            if(Objects.equals(ifContainLeader,"0")){
                if(leaderIds.contains(attendance.getUserId()+"")){
                    continue;
                }
            }
            UnitTaskUser unitTaskUser1 = unitTaskUserMap.get(attendance.getUserId());
            if (unitTaskUser1 != null){
                log.info("人员重复：{}",attendance.getEmpName()+attendance.getEmpCode());
                BigDecimal workDays1 = unitTaskUser1.getWorkdays();
                BigDecimal workDays = attendance.getWorkdays() == null?BigDecimal.ZERO:new BigDecimal(attendance.getWorkdays());
                unitTaskUser1.setWorkdays(workDays1.add(workDays));
                continue;
            }
            UnitTaskUser unitTaskUser = this.attendance2unitTaskUser(attendance);
            userList.add(unitTaskUser);
            unitTaskUserMap.put(attendance.getUserId(),unitTaskUser);
        }
        return userList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void createInitBatch(UnitTask unitTask, List<UnitTaskUser> addList) {
        //获取发放单元下的 发放科室
        Long unitTaskId = unitTask.getId();
        Long grantUnitId = unitTask.getGrantUnitId();
        GrantUnit grantUnit = grantUnitService.getById(grantUnitId);

        //获取上一次 绩效倍数
        Map<String, UnitTaskUser> lastUserMap = new HashMap<>();
        UnitTask lastUnitTask = unitTaskService.getLastUnitTask(unitTaskId,grantUnitId);
        if(lastUnitTask != null){
            List<UnitTaskUser> lastUserList = this.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getGrantUnitId,lastUnitTask.getGrantUnitId()));
            lastUserMap = lastUserList.stream().collect(Collectors.toMap(UnitTaskUser::getUserId, item -> item, (v1, v2) -> v2));
        }

        for (UnitTaskUser unitTaskUser : addList){
            unitTaskUser.setId(null);
            unitTaskUser.setGrantUnitId(grantUnitId);
            unitTaskUser.setGrantUnitName(grantUnit.getName());
            unitTaskUser.setUnitTaskId(unitTask.getId());
            //继承数据（绩效倍数）
            UnitTaskUser lastUser = lastUserMap.get(unitTaskUser.getUserId());
            if(lastUser != null){
                unitTaskUser.setAvgRate(lastUser.getAvgRate());
                unitTaskUser.setSortNum(lastUser.getSortNum());
            }
        }
        if(!addList.isEmpty()){
            this.saveBatch(addList);
        }

        //新增人员考核数据(初始化)
        unitTaskProjectService.initUserData(unitTask,addList);
    }


    private UnitTask getLastUnitTask(Long unitTaskId, Long grantUnitId) {
        return unitTaskMapper.selectOne(Wrappers.<UnitTask>lambdaQuery().eq(UnitTask::getGrantUnitId,grantUnitId).ne(UnitTask::getId,unitTaskId).orderByDesc(UnitTask::getCreateTime).last("limit 1"));
    }
    @Override
    public List<UnitTaskUser> listByTaskId(Long unitTaskId) {
        return this.listByTaskId(unitTaskId,null);
    }

    @Override
    public List<UnitTaskUser> listByTaskId(Long unitTaskId,String empCode) {
        return this.list(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId)
                .eq(empCode != null, UnitTaskUser::getEmpCode, empCode).orderByAsc(UnitTaskUser::getSortNum));
    }

    @Override
    @Transactional
    public void addBatch(UnitTaskUserAddBatchDTO unitTaskUserAddBatchDTO) {
        // TODO:检查新增的人是否已经存在，如果已经存在，那么就不能反复添加（多端同时操作时，可能会重复传入新增人员）
        // checkUser(unitTaskUserAddBatchDTO);
        Long unitTaskId = unitTaskUserAddBatchDTO.getUnitTaskId();
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
        String cycle = unitTask.getCycle();
        Long grantUnitId = unitTask.getGrantUnitId();
        String deptIds = grantUnitService.getDeptIds(grantUnitId);
        if (deptIds == null){
            throw new BizException("请先配置发放单元下的发放科室");
        }
        List<Attendance> attendanceList = unitTaskUserAddBatchDTO.getUserList();
        List<UnitTaskUser> userList = new ArrayList<>();
        for (Attendance attendance : attendanceList){
            UnitTaskUser unitTaskUser = this.attendance2unitTaskUser(attendance);
            userList.add(unitTaskUser);
        }
        //获取考勤数据
        this.getAttendanceData(cycle,userList, CommonUtils.str2List(deptIds));

        this.createInitBatch(unitTask,userList);

        //计算projectCount
        unitTaskProjectCountService.doCount(unitTaskId);
    }

    private void checkUser(UnitTaskUserAddBatchDTO unitTaskUserAddBatchDTO) {
        if (CollUtil.isEmpty(unitTaskUserAddBatchDTO.getUserList())) {
            return;
        }
        Long unitTaskId = unitTaskUserAddBatchDTO.getUnitTaskId();
        List<Attendance> userList = unitTaskUserAddBatchDTO.getUserList();
        List<UnitTaskUser> unitTaskUsers = listByTaskId(unitTaskId);
        // 存在重复人员则抛出异常
        List<String> userIdList = userList.stream().map(Attendance::getEmpCode).collect(Collectors.toList());
        List<String> repeatList = unitTaskUsers.stream().filter(user -> userIdList.contains(user.getEmpCode())).map(UnitTaskUser::getEmpName).collect(Collectors.toList());
        if (CollUtil.isNotEmpty(repeatList)) {
            throw new BizException("存在已添加人员：" + StrUtil.join(",", repeatList) + ",请刷新页面重试");
        }
    }

    @Override
    public List<UnitTaskUser> getAttendanceData(String cycle, List<UnitTaskUser> taskUserList, List<String> accountUnitIdList) {
        if (StrUtil.isNotBlank(cycle)) {
            cycle = StrUtil.replace(cycle, "-", "");
        }
        //人
        List<Long> userIdList = taskUserList.stream().map(user->Long.parseLong(user.getUserId())).collect(Collectors.toList());

        //获取 考勤表数据
        List<Attendance> attendanceListDB = attendanceService.getByCycleUserUnit(cycle,userIdList,accountUnitIdList);
        Map<Long, Attendance> attendanceMap = attendanceListDB.stream().collect(Collectors.toMap(Attendance::getUserId, item -> item, (v1, v2) -> v2));
        for (UnitTaskUser unitTaskUser : taskUserList){
            Long userId = Long.parseLong(unitTaskUser.getUserId());
            Attendance attendance = attendanceMap.get(userId);
            if (Objects.nonNull(attendance)) {
                BeanUtils.copyProperties(attendance, unitTaskUser);
                unitTaskUser.setId(null);
                unitTaskUser.setPostName(attendance.getTitle());
                unitTaskUser.setUserId(attendance.getUserId() + "");
            }
            this.setIfGetAmt(unitTaskUser, attendance);
        }
        return taskUserList;
    }

    @Override
    public void setIfGetAmt(UnitTaskUser unitTaskUser, Attendance attendance) {
        if(attendance == null){
            unitTaskUser.setIfGetAmt("");
            unitTaskUser.setUserRate(BigDecimal.ONE);
            unitTaskUser.setWorkdays(BigDecimal.ZERO);
            return;
        }
        unitTaskUser.setIfGetAmt(Objects.equals(attendance.getIfGetAmt(),"否")?"0":"1");
        if (Objects.equals("0",unitTaskUser.getIfGetAmt())){
            unitTaskUser.setUserRate(BigDecimal.ZERO);
            unitTaskUser.setWorkdays(BigDecimal.ZERO);
        }else{
            unitTaskUser.setUserRate(BigDecimal.ONE);
            unitTaskUser.setWorkdays(attendance.getWorkdayszl() == null?BigDecimal.ZERO:new BigDecimal(attendance.getWorkdayszl()));
        }
    }

    private UnitTaskUser attendance2unitTaskUser(Attendance attendance) {
        UnitTaskUser unitTaskUser = new UnitTaskUser();
        BeanUtils.copyProperties(attendance,unitTaskUser);
        unitTaskUser.setId(null);
        unitTaskUser.setPostName(attendance.getTitle());
        unitTaskUser.setUserId(attendance.getUserId()+"");
        this.setIfGetAmt(unitTaskUser, attendance);
        unitTaskUser.setSortNum(attendance.getSortNum()==null?0:Float.valueOf(attendance.getSortNum()));
        return unitTaskUser;
    }

    @Override
    public IPage<UnitTaskUser> userPage(DmoUserPageDTO dmoUserPageDTO) {
        Page<Attendance> attendanceList = attendanceService.page(dmoUserPageDTO);
        List<UnitTaskUser> userList = new ArrayList<>();
        for (Attendance attendance : attendanceList.getRecords()){
            UnitTaskUser unitTaskUser = this.attendance2unitTaskUser(attendance);
            userList.add(unitTaskUser);
        }
        Page<UnitTaskUser> page = new Page<>(dmoUserPageDTO.getCurrent(),dmoUserPageDTO.getSize(),attendanceList.getTotal());
        page.setRecords(userList);
        return page;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editBatch(UnitTaskUserEditBatchDTO unitTaskUserEditBatchDTO) {
        List<UnitTaskUser> userList = unitTaskUserEditBatchDTO.getUserList();
        if(userList.isEmpty()) return;

        this.updateBatchById(unitTaskUserEditBatchDTO.getUserList());

        UnitTaskUser unitTaskUser = userList.get(0);
        unitTaskProjectCountService.doCount(unitTaskUser.getUnitTaskId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delById(Long id) {
        UnitTaskUser unitTaskUser = this.getById(id);
        if(unitTaskUser == null) {
            return;
        }
        Long unitTaskId = unitTaskUser.getUnitTaskId();
        if(unitTaskId == null) {
            return;
        }

        //删除人员count数据
        unitTaskCountService.removeCountByUser(unitTaskUser);


        //删除人员  业务数据，detail  和 detail item
        unitTaskProjectService.removeByUserId(unitTaskUser);

        //删除同工号的人员
        String empCode = unitTaskUser.getEmpCode();
        if (StrUtil.isNotBlank(empCode)){
            List<UnitTaskUser> delList = this.listByTaskId(unitTaskId, empCode);
            this.removeBatchByIds(delList);
        }else{
            this.removeById(id);
        }


        //重新计算
        unitTaskProjectCountService.doCount(unitTaskUser.getUnitTaskId());

        //单独处理secCount
        UnitTask unitTask = unitTaskService.getById(unitTaskId);
        Long secondTaskId = unitTask.getSecondTaskId();
        secondTaskCountService.doCountByEmpCode(secondTaskId,unitTaskUser.getEmpCode());

    }

    @Override
    public List<UnitTaskUser> listByTaskUser(UnitTask unitTask, List<String> userIds) {
        return this.list(Wrappers.<UnitTaskUser>lambdaQuery()
                .in(UnitTaskUser::getUserId,userIds)
                .eq(UnitTaskUser::getUnitTaskId,unitTask.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delBatchByIds(List<Long> idsList) {
        Long unitTaskId = null;
        Set<String> empCodeSet = new HashSet<>();
        for (Long id : idsList) {
            UnitTaskUser unitTaskUser = this.getById(id);
            if (unitTaskUser == null) {
                return;
            }
            empCodeSet.add(unitTaskUser.getEmpCode());
            unitTaskId = unitTaskUser.getUnitTaskId();

            //删除人员count数据
            unitTaskCountService.removeCountByUser(unitTaskUser);

            //删除人员  业务数据，detail  和 detail item
            unitTaskProjectService.removeByUserId(unitTaskUser);

            this.removeById(id);
        }
        if (unitTaskId != null){
            //重新计算
            unitTaskProjectCountService.doCount(unitTaskId);
        }

        //单独处理secCount
        UnitTask unitTask = unitTaskService.getById(unitTaskId);
        Long secondTaskId = unitTask.getSecondTaskId();

        for (String empCode : empCodeSet){
            secondTaskCountService.doCountByEmpCode(secondTaskId,empCode);
        }
    }


}
