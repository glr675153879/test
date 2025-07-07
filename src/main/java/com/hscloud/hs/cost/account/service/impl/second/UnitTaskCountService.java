package com.hscloud.hs.cost.account.service.impl.second;

import cn.hutool.core.comparator.CompareUtil;
import com.alibaba.excel.EasyExcelFactory;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.metadata.WriteTable;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.mapper.second.*;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskCountEditBatchDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskCountVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.impl.second.kpi.SecondKpiService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.ISecondTaskCountService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskCountService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskUserService;
import com.hscloud.hs.cost.account.utils.DmoUtil;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.media.service.DingRobotService;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static org.apache.commons.codec.CharEncoding.UTF_8;

/**
* 发放单元分配结果按人汇总 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnitTaskCountService extends ServiceImpl<UnitTaskCountMapper, UnitTaskCount> implements IUnitTaskCountService {

    private final UnitTaskProjectCountMapper unitTaskProjectCountMapper;
    private final UnitTaskDetailCountMapper unitTaskDetailCountMapper;
    private final ISecondTaskCountService secondTaskCountService;
    private final IGrantUnitService grantUnitService;
    private final UnitTaskMapper unitTaskMapper;
    private final UnitTaskUserMapper unitTaskUserMapper;
    private final RedisUtil redisUtil;
    private final SecondKpiService secondKpiService;
    @Resource
    private DingRobotService dingRobotService;
    @Lazy
    @Autowired
    private IUnitTaskUserService unitTaskUserService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void doCount(UnitTask unitTask) {
        if(!"0".equals(unitTask.getIfUpload())) {
            Long unitTaskId = unitTask.getId();
            List<UnitTaskCount> unitTaskCountList = this.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId, unitTaskId));
            Map<String, UnitTaskCount> taskCountMap = new HashMap<>();
            for (UnitTaskCount unitTaskCount : unitTaskCountList) {
                String empCode = unitTaskCount.getEmpCode();
                taskCountMap.put(empCode, unitTaskCount);
            }

            List<UnitTaskProjectCount> projectCountList = unitTaskProjectCountMapper.selectList(Wrappers.<UnitTaskProjectCount>lambdaQuery().eq(UnitTaskProjectCount::getUnitTaskId, unitTaskId));

            List<UnitTaskCount> addList = new ArrayList<>();
            //每个人的 projectCount合计,key:empcode
            Map<String, BigDecimal> projectCountAmtMap = new HashMap<>();
            for (UnitTaskProjectCount projectCount : projectCountList) {
                String empCode = projectCount.getEmpCode();
                BigDecimal count = projectCountAmtMap.computeIfAbsent(empCode, k -> BigDecimal.ZERO);
                count = count.add(projectCount.getAmt());
                projectCountAmtMap.put(empCode, count);

                UnitTaskCount unitTaskCount = taskCountMap.get(empCode);
                if (unitTaskCount == null) {
                    unitTaskCount = new UnitTaskCount();
                    unitTaskCount.setUnitTaskId(unitTaskId);
                    unitTaskCount.setSecondTaskId(unitTask.getSecondTaskId());
                    unitTaskCount.setEmpCode(projectCount.getEmpCode());
                    unitTaskCount.setUserId(projectCount.getUserId());
                    unitTaskCount.setEmpName(projectCount.getEmpName());
                    unitTaskCountList.add(unitTaskCount);
                    taskCountMap.put(empCode, unitTaskCount);

                    addList.add(unitTaskCount);
                }
            }

            BigDecimal countAmt = BigDecimal.ZERO;
            for (UnitTaskCount unitTaskCount : unitTaskCountList) {
                String empCode = unitTaskCount.getEmpCode();
                BigDecimal count = projectCountAmtMap.get(empCode).setScale(2, RoundingMode.DOWN);
                unitTaskCount.setAmt(count);

                countAmt = countAmt.add(count);
            }

            //计算一分钱，先找领导，找不到领导就算给第一个amt>0的人
            BigDecimal ksAmt = unitTask.getKsAmt();
            if(countAmt.compareTo(ksAmt) != 0){
                BigDecimal oneFen = ksAmt.subtract(countAmt);
                this.setOneFen(unitTask,unitTaskCountList,oneFen);
            }

            if (!addList.isEmpty()){
                this.saveBatch(addList);
            }
            this.updateBatchById(unitTaskCountList);
        }

        //计算 secondTaskCount
        //secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTask.getId());
    }

    private void setOneFen(UnitTask unitTask,List<UnitTaskCount> unitTaskCountList, BigDecimal oneFen) {
        if(!unitTaskCountList.isEmpty()){
            String cycle = unitTask.getCycle();
            String deptIds = grantUnitService.getDeptIds(unitTask.getGrantUnitId());
            List<UnitTaskUser> leaderList = secondKpiService.deptLeaderList(cycle, deptIds);
            List<String> leaderIds = leaderList.stream().map(UnitTaskUser::getUserId).collect(Collectors.toList());
            //一分钱优先加到领导头上
            if(!leaderIds.isEmpty()){
                for (UnitTaskCount unitTaskCount : unitTaskCountList){
                    Long userId = unitTaskCount.getUserId();
                    if(leaderIds.contains(userId+"")){
                        unitTaskCount.setAmt(unitTaskCount.getAmt().add(oneFen));
                        return;
                    }
                }
            }
            List<UnitTaskUser> unitTaskUsers = unitTaskUserService.listByTaskId(unitTask.getId());
            Map<String, Float> collect = unitTaskUsers.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode, UnitTaskUser::getSortNum, (v1, v2) -> v1));
            unitTaskCountList.sort((o1, o2) -> {
                Float sortNum1 = collect.get(o1.getEmpCode());
                Float sortNum2 = collect.get(o2.getEmpCode());
                return CompareUtil.compare(sortNum1, sortNum2);
            });
            //加到第一个amt!=0的人头上，总会有的
            for (UnitTaskCount unitTaskCount : unitTaskCountList){
                BigDecimal amt = unitTaskCount.getAmt();
                if(amt != null && amt.compareTo(BigDecimal.ZERO) > 0){
                    unitTaskCount.setAmt(amt.add(oneFen));
                    return;
                }
            }
        }
    }

    public static void main(String[] args) {
        List<UnitTaskUser> userList = new ArrayList<>();
        List<String> collect = userList.stream().map(UnitTaskUser::getUserId).collect(Collectors.toList());
        System.out.println(collect);
    }

    @Override
    public void exportCount(Long unitTaskId, HttpServletResponse response) {
        try(OutputStream out = response.getOutputStream();){
            UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding(UTF_8);
            ExcelWriter writer = EasyExcelFactory.write(out)//.automaticMergeHead(false)
                    .build();
            // 动态添加表头，适用一些表头动态变化的场景
            WriteSheet sheet1 = new WriteSheet();
            sheet1.setSheetName(unitTask.getName());
            sheet1.setSheetNo(0);
            // 创建一个表格，用于 Sheet 中使用
            WriteTable table = new WriteTable();
            table.setTableNo(1);
            //查全部
            table.setHead(this.getHead());
            // 写数据
            List<UnitTaskUser> userList = unitTaskUserMapper.selectList(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
            writer.write(this.getContent(userList), sheet1, table);
            writer.finish();
        }catch (Exception e){

        }
    }

    private Collection<?> getContent(List<UnitTaskUser> userList) {
        List<List<Object>> totalContent = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList) {
            List<Object> list = new ArrayList<>();
            list.add(unitTaskUser.getEmpName());
            list.add(unitTaskUser.getEmpCode());
            totalContent.add(list);
        }
        return totalContent;
    }

    private List<List<String>> getHead() {
        List<List<String>> total = new ArrayList<>();
        //人员信息
        List<String> name = new ArrayList<>();
        name.add("姓名");
        total.add(name);

        List<String> empCode = new ArrayList<>();
        empCode.add("工号");
        total.add(empCode);

        List<String> amt = new ArrayList<>();
        amt.add("绩效金额");
        total.add(amt);

        return total;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ImportResultVo importCount(Long unitTaskId, String[][] xlsDataArr) {
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
        List<UnitTaskUser> userList = unitTaskUserMapper.selectList(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        Map<String,UnitTaskUser> userMap = userList.stream().collect(Collectors.toMap(UnitTaskUser::getEmpCode,item->item, (v1, v2) -> v2));

        List<UnitTaskCount> unitTaskCountList = this.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId,unitTaskId));
        Map<String,UnitTaskCount> taskCountMap = unitTaskCountList.stream().collect(Collectors.toMap(UnitTaskCount::getEmpCode,item->item, (v1, v2) -> v2));


        //逐条检验，成功的落库 和 失败的入redis
        List<String> errorList = new ArrayList<>();
        //key  empCode+amtType  ,value = amt
        Map<String,String> dataMap = new HashMap<>();
        for (int row=0;row< xlsDataArr.length;row++){
            String[] data = xlsDataArr[row];
            //姓名
            //String name = data[0];
            //工号
            String empCode = data[1];
            //校验row人员是否存在
            this.importCountUserValid(errorList,empCode,row,"工号",userMap);
            //绩效金额
            String amt = data[2];

            if(this.importCountValid(errorList,amt,row, "绩效金额")){
                dataMap.put(empCode,amt);
            }
        }

        //导入数据，找不到就赋0
        for (UnitTaskUser unitTaskUser : userList){
            String empCode = unitTaskUser.getEmpCode();
            String amt = dataMap.get(empCode);

            UnitTaskCount unitTaskCount = taskCountMap.get(empCode);
            if(unitTaskCount == null){
                unitTaskCount = new UnitTaskCount();
                unitTaskCount.setUnitTaskId(unitTaskId);
                unitTaskCount.setSecondTaskId(unitTask.getSecondTaskId());
                unitTaskCount.setEmpCode(unitTaskUser.getEmpCode());
                unitTaskCount.setUserId(Long.parseLong(unitTaskUser.getUserId()));
                unitTaskCount.setEmpName(unitTaskUser.getEmpName());
                unitTaskCountList.add(unitTaskCount);
                taskCountMap.put(empCode,unitTaskCount);
            }
            unitTaskCount.setAmt(amt == null?BigDecimal.ZERO:new BigDecimal(amt));
        }
        //批量修改
        this.saveOrUpdateBatch(unitTaskCountList);

//        //计算projectCount
//        this.doCount(unitTask);
        //计算 secondTaskCount
        //secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTaskId);

        //错误消息入redis
        redisUtil.set(CacheConstants.SEC_IMPORT_ERRLOG +unitTaskId,errorList,30, TimeUnit.MINUTES);
        //导入消息
        ImportResultVo vo = new ImportResultVo();
        vo.setTotalCount(xlsDataArr.length);
        if(!errorList.isEmpty()){
            vo.setErrorCount(errorList.size());
        }
        return vo;
    }

    @Override
    public List<UnitTaskCountVo> userList(Long unitTaskId) {
        List<UnitTaskUser> userList = unitTaskUserMapper.selectList(Wrappers.<UnitTaskUser>lambdaQuery().eq(UnitTaskUser::getUnitTaskId,unitTaskId));
        List<UnitTaskCount> unitCountList = this.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId,unitTaskId));
        //根据人员分组 unitCountMap ，key：empcode
        Map<String,UnitTaskCount> unitCountMap = new HashMap<>();
        for (UnitTaskCount unitCount : unitCountList){
            String key = unitCount.getEmpCode();
            unitCountMap.put(key,unitCount);
        }
        List<UnitTaskCountVo> rtnList = new ArrayList<>();
        for (UnitTaskUser unitTaskUser : userList){
            String empCode = unitTaskUser.getEmpCode();
            UnitTaskCountVo vo = new UnitTaskCountVo();
            BeanUtils.copyProperties(unitTaskUser, vo);

            UnitTaskCount unitTaskCount = unitCountMap.get(empCode);
            if(unitTaskCount != null){
                vo.setAmt(unitTaskCount.getAmt());
            }
            rtnList.add(vo);
        }
        return rtnList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void editBatch(UnitTaskCountEditBatchDTO unitTaskCountEditBatchDTO) {
        Long unitTaskId = unitTaskCountEditBatchDTO.getUnitTaskId();
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
        List<UnitTaskCount> unitTaskCountList = this.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId, unitTaskId));
        Map<String, UnitTaskCount> taskCountMap = new HashMap<>();
        for (UnitTaskCount unitTaskCount : unitTaskCountList) {
            String empCode = unitTaskCount.getEmpCode();
            taskCountMap.put(empCode, unitTaskCount);
        }
        for (UnitTaskCount unitTaskCount : unitTaskCountEditBatchDTO.getUnitTaskCountList()){
            String empCode = unitTaskCount.getEmpCode();
            if(StringUtils.isBlank(empCode) ){
                continue;
            }
            BigDecimal amt = unitTaskCount.getAmt();
            if(amt == null){
                amt = BigDecimal.ZERO;
            }
            UnitTaskCount unitTaskCountDB = taskCountMap.get(empCode);
            if (unitTaskCountDB == null) {
                unitTaskCountDB = new UnitTaskCount();
                unitTaskCountDB.setUnitTaskId(unitTaskId);
                unitTaskCountDB.setSecondTaskId(unitTask.getSecondTaskId());
                unitTaskCountDB.setEmpCode(unitTaskCount.getEmpCode());
                unitTaskCountDB.setUserId(unitTaskCount.getUserId());
                unitTaskCountDB.setEmpName(unitTaskCount.getEmpName());
                unitTaskCountList.add(unitTaskCountDB);
                taskCountMap.put(empCode, unitTaskCount);
            }
            unitTaskCountDB.setAmt(amt);

        }
        this.saveOrUpdateBatch(unitTaskCountList);

        //计算 secondTaskCount
        //secondTaskCountService.doCount(unitTask.getSecondTaskId(),unitTaskId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeCountByUser(UnitTaskUser unitTaskUser) {
        Long unitTaskId = unitTaskUser.getUnitTaskId();
        Long userId = Long.parseLong(unitTaskUser.getUserId());
        if(unitTaskId != null){
            //删除detailCount
            unitTaskDetailCountMapper.delete(Wrappers.<UnitTaskDetailCount>lambdaQuery()
                    .eq(UnitTaskDetailCount::getUnitTaskId,unitTaskId)
                    .eq(UnitTaskDetailCount::getUserId,userId));

            //删除projectCount
            unitTaskProjectCountMapper.delete(Wrappers.<UnitTaskProjectCount>lambdaQuery()
                    .eq(UnitTaskProjectCount::getUnitTaskId,unitTaskId)
                    .eq(UnitTaskProjectCount::getUserId,userId));

            //删除taskCount
            this.remove(Wrappers.<UnitTaskCount>lambdaQuery()
                    .eq(UnitTaskCount::getUnitTaskId,unitTaskId)
                    .eq(UnitTaskCount::getUserId,userId));
        }

    }

    @Override
    public List<UnitTaskCount> listByTaskId(Long unitTaskId) {
        return this.list(Wrappers.<UnitTaskCount>lambdaQuery().eq(UnitTaskCount::getUnitTaskId,unitTaskId));
    }

    @Override
    public Boolean checkAmt(Long unitTaskId) {
        UnitTask unitTask = unitTaskMapper.selectById(unitTaskId);
        if(unitTask == null){
            throw new BizException("任务不存在");
        }
        //每个人的绩效合计不能小于0
        List<UnitTaskCount> unitTaskCountList = this.listByTaskId(unitTaskId);
        for (UnitTaskCount unitTaskCount : unitTaskCountList){
            BigDecimal amt = unitTaskCount.getAmt();
            if(amt == null || amt.compareTo(BigDecimal.ZERO) < 0){
                return false;
            }
        }

        //每个人的taskCount合计 要等于 一次分配金额
        BigDecimal sumAmt = unitTaskCountList.stream().map(UnitTaskCount::getAmt).reduce(BigDecimal.ZERO, BigDecimal::add);
        if(unitTask.getKsAmt().compareTo(sumAmt) != 0){
            String msg = unitTask.getGrantUnitName()+" 每个人的合计金额"+sumAmt+" 不等于 一次分配金额"+unitTask.getKsAmt();
            dingRobotService.sendMsg2DingRobot(SecurityUtils.getUser().getTenantId() + "", "二次分配", msg);
            throw new BizException(msg);
        }

        return true;
    }

    private boolean importCountValid(List<String> errorList, String qty, int row, String colName) {
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

    private Boolean importCountUserValid(List<String> errorList, String empCode, int row, String colName, Map<String, UnitTaskUser> userMap) {
        row++;
        if(userMap.get(empCode) == null){
            errorList.add("原文件第"+row+"行，"+colName+"["+empCode+"]不存在或匹配不上");
            return false;
        }
        return true;
    }
}
