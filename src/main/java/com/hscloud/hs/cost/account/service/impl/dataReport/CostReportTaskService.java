package com.hscloud.hs.cost.account.service.impl.dataReport;

import cn.hutool.json.JSONArray;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.enums.dataReport.ReportRecordTypeEnum;
import com.hscloud.hs.cost.account.mapper.dataReport.CostReportTaskMapper;
import com.hscloud.hs.cost.account.model.dto.dataReport.CostReportItemDto;
import com.hscloud.hs.cost.account.model.dto.dataReport.DataReportUserDto;
import com.hscloud.hs.cost.account.model.entity.dataReport.CostReportTask;
import com.hscloud.hs.cost.account.service.dataReport.ICostReportTaskService;
import com.hscloud.hs.cost.account.utils.DataProcessUtil;
import com.hscloud.hs.cost.account.utils.report.CircleUtil;
import com.pig4cloud.pigx.admin.api.entity.DeptThird;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
* 上报任务 服务实现类
*
*/
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CostReportTaskService extends ServiceImpl<CostReportTaskMapper, CostReportTask> implements ICostReportTaskService {

    private final DataProcessUtil dataProcessUtil;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object insert(CostReportTask costReportTask) {
        // 校验核算项是否已经添加在其他任务中
        if(ReportRecordTypeEnum.DEPT_COST.getVal().equals(costReportTask.getType())) {
            if(checkItemList(costReportTask)) {
                log.error("当前新增任务中的上报项存在于其他任务中，当前提交任务信息:{}", JSON.toJSONString(costReportTask));
                throw new BizException("当前新增任务中的上报项存在于其他任务中！");
            }
        }
        //拿到数据后进行处理
        processTaskList(costReportTask);
        return save(costReportTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Object updateTask(CostReportTask costReportTask) {
        // 校验核算项是否已经添加在其他任务中
        if(ReportRecordTypeEnum.DEPT_COST.getVal().equals(costReportTask.getType())) {
            if(checkItemList(costReportTask)) {
                log.error("当前更新任务中的上报项存在于其他任务中，当前提交任务信息:{}", JSON.toJSONString(costReportTask));
                throw new BizException("当前更新任务中的上报项存在于其他任务中！");
            }
        }
        //拿到数据后进行处理
        processTaskList(costReportTask);
        return updateById(costReportTask);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean initiate() {
        List<CostReportTask> costReportTaskList = list();
        for (CostReportTask task : costReportTaskList){
            task.setInitialized("Y");
        }
        return updateBatchById(costReportTaskList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean activate(CostReportTask costReportTask) {
        CostReportTask task = getById(costReportTask);
        if (task != null) {
            task.setStatus(costReportTask.getStatus());
            return updateById(task);
        }
        return false;
    }

    @Override
    public void updateNextCalculateCircle(CostReportTask costReportTask) {
        log.info("initiateJobHandler updateNextCalculateCircle ：{} ：{}", costReportTask.getId(), costReportTask.getTaskName());
        String calculateCircle = costReportTask.getCalculateCircle();
        String frequencyTypeValue = JSON.parseObject(costReportTask.getFrequencyType()).getString("value");
        YearMonth nextYearMonth = CircleUtil.getNextYearMonth(frequencyTypeValue, calculateCircle);
        costReportTask.setCalculateCircle(nextYearMonth.toString());
        super.updateById(costReportTask);
    }


    private void processTaskList(CostReportTask costReportTask) {
        // Process itemList
        List<CostReportItemDto> itemVoList = costReportTask.getItemVoList();
        JSONArray itemArray = new JSONArray();
        for (CostReportItemDto costReportItem : itemVoList) {
            JSONObject itemObject = new JSONObject();
            itemObject.put("id", costReportItem.getId());
            itemObject.put("name", costReportItem.getItemName());
            itemObject.put("dataType", costReportItem.getDataType());
            itemObject.put("measureUnit", costReportItem.getMeasureUnit());
            itemObject.put("isDeptDistinguished", costReportItem.getIsDeptDistinguished());
            itemArray.put(itemObject);
        }
        costReportTask.setItemList(itemArray.toString());
        // Process deptList
        List<DeptThird> deptVoList = Optional.ofNullable(costReportTask.getDeptVoList()).map(r -> r.getDeptList())
                .orElse(Collections.emptyList());
        costReportTask.setReportDeptList(dataProcessUtil.processList(deptVoList));
        // Process userList
        List<DataReportUserDto> userVoList = Optional.ofNullable(costReportTask.getUserVoList()).map(r -> r.getUserList())
                .orElse(Collections.emptyList());
        costReportTask.setUserList(dataProcessUtil.processList(userVoList));
    }

    /**
     * 校验当前任务中的上报项是否已经在其他任务中添加
     * @param costReportTask
     */
    private Boolean checkItemList(CostReportTask costReportTask) {
        // 判断当前核算项是否使用在运行中的任务中
        Boolean ifUsed = false;

        // 当前任务的上报项id
        List<CostReportItemDto> itemVoList = costReportTask.getItemVoList();
        Set<String> itemSet = Optional.ofNullable(itemVoList).map(r -> r.stream().map(itemVo -> itemVo.getId())
                        .collect(Collectors.toSet())).orElse(Collections.emptySet());

        // 获取所有启用中的任务 （不包含自己，更新的时候）
        List<CostReportTask> costReportTasks = list(Wrappers.<CostReportTask>lambdaQuery()
                .eq(CostReportTask::getStatus, "0")
                .eq(CostReportTask::getType, costReportTask.getType())
                .ne(!Objects.isNull(costReportTask.getId()), CostReportTask::getId, costReportTask.getId()));

        // 获取所有的上报项并合并去重
        ifUsed = costReportTasks.stream()
                .map(r -> r.queryItemVoList()).collect(Collectors.toList())
                .stream().flatMap(List::stream).distinct().collect(Collectors.toList())
                .stream().anyMatch(r -> itemSet.contains(r.getId()));

        return ifUsed;
    }

}
