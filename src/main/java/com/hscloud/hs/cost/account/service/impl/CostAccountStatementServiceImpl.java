package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.google.gson.Gson;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.JsonObject;
import com.hscloud.hs.cost.account.constant.enums.AccountTaskStatus;
import com.hscloud.hs.cost.account.constant.enums.DimensionEnum;
import com.hscloud.hs.cost.account.constant.enums.RationStatusNum;
import com.hscloud.hs.cost.account.mapper.CostAccountStatementMapper;
import com.hscloud.hs.cost.account.model.dto.AccountStatementDetailQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountStatementQueryDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.MonthRatio;
import com.hscloud.hs.cost.account.model.pojo.QuarterInfo;
import com.hscloud.hs.cost.account.model.pojo.YearRatio;
import com.hscloud.hs.cost.account.model.vo.*;
import com.hscloud.hs.cost.account.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.hscloud.hs.cost.account.utils.TimeUtils.*;
import static com.hscloud.hs.cost.account.utils.TimeUtils.parseQuarterData;

/**
 * @author 小小w
 * @date 2023/9/21 16:29
 */
@Service
@RequiredArgsConstructor
public class CostAccountStatementServiceImpl extends ServiceImpl<CostAccountStatementMapper, CostTaskExecuteResult> implements CostAccountStatementService {

    private final CostTaskExecuteResultService costTaskExecuteResultService;

    private final CostAccountTaskService costAccountTaskService;

    private final CostTaskExecuteResultIndexService costTaskExecuteResultIndexService;

    private final CostAccountUnitService costAccountUnitService;

    private final CostTaskExecuteResultItemService costTaskExecuteResultItemService;

    private final static Gson gson = new Gson();


    @Override
    public List<CostResultStatementVo> resultStatementDetail(AccountStatementDetailQueryDto queryDto) {

        //根据时间生成一个voList
        List<CostResultStatementVo> voList = generateVoList(queryDto);

        //此任务的信息
        CostAccountTask task = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getId, queryDto.getTaskId()));

        List<CostResultStatementVo> result = voList.stream().map(vo -> {
            CostAccountTask one = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                    .eq(CostAccountTask::getAccountType, task.getAccountType())
                    .eq(CostAccountTask::getDimension, task.getDimension())
                    .eq(CostAccountTask::getAccountStartTime, vo.getAccountStartTime())
                    .eq(CostAccountTask::getAccountEndTime, vo.getAccountEndTime())
                    .last("limit 1"));
            BeanUtil.copyProperties(one, vo);
            if(one==null){
                return vo;
            }
            return getOneResult(one);
        }).collect(Collectors.toList());

        return result;
    }

    private CostResultStatementVo getOneResult(CostAccountTask task){
        CostResultStatementVo vo = BeanUtil.copyProperties(task, CostResultStatementVo.class);
        //查询此任务的核算总值
        List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                .eq(CostTaskExecuteResult::getTaskId, task.getId()));
        BigDecimal totalCount = BigDecimal.ZERO;
        for (CostTaskExecuteResult result:results){
            totalCount=totalCount.add(result.getTotalCount());
        }
        vo.setTotalCount(totalCount);
        return getRatio(vo, task,"RESULT",null,null,null);
    }

    @Override
    public List<ResultExcelVo> exportResult() {
        List<CostResultStatementVo> list = resultStatementPage(new CostAccountStatementQueryDto()).getRecords();
        List<ResultExcelVo> excelVos = list.stream().map(vo ->{
            ResultExcelVo v = new ResultExcelVo();
            v.setAccountTaskName(vo.getAccountTaskName());
            setExcelVo(vo,v);
            return v;
        }).collect(Collectors.toList());
        return excelVos;
    }

    @Override
    public IPage<CostResultStatementVo> resultStatementPage(CostAccountStatementQueryDto queryDto){

        Page<CostAccountTask> page = costAccountTaskService.page(new Page<>(queryDto.getCurrent(), queryDto.getSize()),Wrappers.<CostAccountTask>lambdaQuery()
                .eq(queryDto.getTaskId()!=null,CostAccountTask::getId,queryDto.getTaskId())
                .eq(StrUtil.isNotBlank(queryDto.getDimension()),CostAccountTask::getDimension,queryDto.getDimension())
                .eq(CostAccountTask::getStatus,AccountTaskStatus.COMPLETED.getCode())
                .eq(CostAccountTask::getSupportStatistics,1));
        List<CostAccountTask> list = page.getRecords();

        List<CostResultStatementVo> voList = list.stream().map(task -> {
            CostResultStatementVo vo = BeanUtil.copyProperties(task, CostResultStatementVo.class);
            //查询此任务的核算总值
            List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                    .eq(CostTaskExecuteResult::getTaskId, task.getId()));
            BigDecimal totalCount = BigDecimal.ZERO;
            for (CostTaskExecuteResult result:results){
                totalCount=totalCount.add(result.getTotalCount());
            }
            vo.setTotalCount(totalCount);
            return getRatio(vo, task,"RESULT",null,null,null);
        }).collect(Collectors.toList());

        //筛选搜索条件
        List<CostResultStatementVo> result = voList.stream()
                .filter(vo -> isValidPeriod(queryDto).test(vo))
                .filter(vo -> isValidMonthOverMonth(queryDto).test(vo))
                .filter(vo -> isValidYearOverYear(queryDto).test(vo))
                .filter(vo -> isValidTotalCount(queryDto).test(vo))
                .collect(Collectors.toList());

        int total = result.size();

        long startIndex = (queryDto.getCurrent()-1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex <result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }

        return new Page<CostResultStatementVo>(queryDto.getCurrent(),queryDto.getSize()).setTotal(total).setRecords(result);
    }

    @Override
    public IPage<CostResultStatementVo> groupStatementPage(CostAccountStatementQueryDto queryDto){


        List<CostAccountTask> list = costAccountTaskService.list(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(queryDto.getTaskId()!=null,CostAccountTask::getId,queryDto.getTaskId())
                .eq(StrUtil.isNotBlank(queryDto.getDimension()),CostAccountTask::getDimension,queryDto.getDimension())
                .eq(CostAccountTask::getStatus,AccountTaskStatus.COMPLETED.getCode())
                .eq(CostAccountTask::getSupportStatistics,1));

        List<CostResultStatementVo> voList = new ArrayList<>();

        list.stream().forEach(task -> {
            QueryWrapper<CostTaskExecuteResult> queryWrapper = Wrappers.query();
            queryWrapper.eq("task_id",task.getId())
                    .select("task_id", "group_id", "any_value(group_name) as group_name","SUM(total_count) as totalCount")
                    .groupBy("task_id", "group_id");
            List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(queryWrapper);
            //遍历每个分组
            List<CostResultStatementVo> vos = results.stream().map(r -> {
                CostResultStatementVo vo = BeanUtil.copyProperties(task, CostResultStatementVo.class);
                //设置分组
                vo.setGroupId(r.getGroupId());
                vo.setTotalCount(r.getTotalCount());
                return getRatio(vo, task, "GROUP",vo.getGroupId(),null,null);
            }).collect(Collectors.toList());
            voList.addAll(vos);
        });

        //筛选搜索条件
        List<CostResultStatementVo> result = voList.stream()
                .filter(vo -> isValidPeriod(queryDto).test(vo))
                .filter(vo -> isValidMonthOverMonth(queryDto).test(vo))
                .filter(vo -> isValidYearOverYear(queryDto).test(vo))
                .filter(vo -> isValidTotalCount(queryDto).test(vo))
                .filter(vo->isValidGroup(queryDto).test(vo))
                .collect(Collectors.toList());

        int total = result.size();

        long startIndex = (queryDto.getCurrent()-1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex <result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }

        return new Page<CostResultStatementVo>(queryDto.getCurrent(),queryDto.getSize()).setTotal(total).setRecords(result);
    }

    @Override
    public List<CostResultStatementVo> groupStatementDetail(AccountStatementDetailQueryDto queryDto) {

        //根据时间生成一个voList
        List<CostResultStatementVo> voList = generateVoList(queryDto);

        //根据taskId获取任务再获取groupId、dimension、accountType
        CostAccountTask task = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getId, queryDto.getTaskId()));

        List<CostResultStatementVo> results = voList.stream().map(result -> {

            //查询是否存在accountType、dimension相同，且时间对应的任务
            CostAccountTask one = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                    .eq(CostAccountTask::getAccountType, task.getAccountType())
                    .eq(CostAccountTask::getDimension, task.getDimension())
                    .eq(CostAccountTask::getAccountStartTime, result.getAccountStartTime())
                    .eq(CostAccountTask::getAccountEndTime, result.getAccountEndTime())
                    .last("limit 1"));
            if(one==null){
                return result;
            }
            QueryWrapper<CostTaskExecuteResult> queryWrapper = Wrappers.query();
            queryWrapper.eq("task_id", one.getId())
                    .eq("group_id",queryDto.getGroupId())
                    .select("task_id", "group_id", "any_value(group_name) as group_name", "SUM(total_count) as totalCount")
                    .groupBy("task_id", "group_id");
            CostTaskExecuteResult r = costTaskExecuteResultService.getOne(queryWrapper);

            CostResultStatementVo v = BeanUtil.copyProperties(one, CostResultStatementVo.class);
            if(r==null){
                return v;
            }
            //设置分组
            v.setGroupId(r.getGroupId());
            v.setTotalCount(r.getTotalCount());
            return getRatio(v, one, "GROUP", v.getGroupId(),null,null);
        }).collect(Collectors.toList());
        return results;
    }

    @Override
    public List<GroupExcelVo> exportGroup() {
        List<CostResultStatementVo> list = groupStatementPage(new CostAccountStatementQueryDto()).getRecords();
        List<GroupExcelVo> excelVos = list.stream().map(vo ->{

            GroupExcelVo v = new GroupExcelVo();
            //设置核算分组
            java.util.Map<String, String> jsonMap = gson.fromJson(vo.getGroupId(), java.util.Map.class);
            v.setGroupId(jsonMap.get("label"));
            v.setAccountTaskName(vo.getAccountTaskName());
            setExcelVo(vo,v);
            return v;
        }).collect(Collectors.toList());
        return excelVos;
    }

    @Override
    public IPage<CostResultStatementVo> unitStatementPage(CostAccountStatementQueryDto queryDto) {

        List<CostAccountTask> list = costAccountTaskService.list(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(queryDto.getTaskId()!=null,CostAccountTask::getId,queryDto.getTaskId())
                .eq(StrUtil.isNotBlank(queryDto.getDimension()),CostAccountTask::getDimension,queryDto.getDimension())
                .eq(CostAccountTask::getStatus,AccountTaskStatus.COMPLETED.getCode())
                .eq(CostAccountTask::getSupportStatistics,1));


        List<CostResultStatementVo> voList = new ArrayList<>();

        list.stream().forEach(task->{
            List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                    .eq(CostTaskExecuteResult::getTaskId, task.getId()));
            results.stream().forEach(u->{
                CostResultStatementVo vo = BeanUtil.copyProperties(task, CostResultStatementVo.class);
                vo.setUnitId(u.getUnitId());
                vo.setUnitName(u.getUnitName());
                vo.setTotalCount(u.getTotalCount());
                getRatio(vo, task, "UNIT", null,u.getUnitId(),null);
                voList.add(vo);
            });
        });

        //筛选搜索条件
        List<CostResultStatementVo> result = voList.stream()
                .filter(vo -> isValidPeriod(queryDto).test(vo))
                .filter(vo -> isValidMonthOverMonth(queryDto).test(vo))
                .filter(vo -> isValidYearOverYear(queryDto).test(vo))
                .filter(vo -> isValidTotalCount(queryDto).test(vo))
                .filter(vo->isValidUnit(queryDto).test(vo))
                .collect(Collectors.toList());

        int total = result.size();

        long startIndex = (queryDto.getCurrent()-1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex <result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }

        return new Page<CostResultStatementVo>(queryDto.getCurrent(),queryDto.getSize()).setTotal(total).setRecords(result);
    }

    @Override
    public IPage<CostResultStatementVo> indexStatementPage(CostAccountStatementQueryDto queryDto) {


        List<CostAccountTask> list = costAccountTaskService.list(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(queryDto.getTaskId()!=null,CostAccountTask::getId,queryDto.getTaskId())
                .eq(StrUtil.isNotBlank(queryDto.getDimension()),CostAccountTask::getDimension,queryDto.getDimension())
                .eq(CostAccountTask::getStatus,AccountTaskStatus.COMPLETED.getCode())
                .eq(CostAccountTask::getSupportStatistics,1));


        List<CostResultStatementVo> voList = new ArrayList<>();

        list.stream().forEach(task->{

            costTaskExecuteResultIndexService.list(Wrappers.<CostTaskExecuteResultIndex>lambdaQuery()
                    .eq(CostTaskExecuteResultIndex::getTaskId,task.getId()));
            QueryWrapper<CostTaskExecuteResultIndex> queryWrapper = Wrappers.query();
            queryWrapper.eq("task_id",task.getId())
                    .select("any_value(id) as id","index_id","any_value(unit_id) as unit_id","any_value(index_name) as index_name","parent_id","SUM(index_count) as indexCount")
                    .groupBy("index_id","unit_id","parent_id");

            List<CostTaskExecuteResultIndex> indexList = costTaskExecuteResultIndexService.list(queryWrapper);

            indexList.stream().forEach(i->{
                CostResultStatementVo vo = BeanUtil.copyProperties(task, CostResultStatementVo.class);
                vo.setTotalCount(i.getIndexCount());
                vo.setIndexId(i.getIndexId());
                vo.setIndexName(i.getIndexName());
                vo.setParentId(i.getParentId());
                vo.setUnitId(i.getUnitId());
                CostAccountUnit unit = costAccountUnitService.getById(i.getUnitId());
                vo.setUnitName(unit.getName());
                vo.setGroupId(unit.getAccountGroupCode());
                vo.setIndexResultId(i.getId());
                getRatio(vo,task,"INDEX",null,null,i);
                voList.add(vo);
            });
        });

        //筛选搜索条件
        List<CostResultStatementVo> result = voList.stream()
                .filter(vo -> isValidPeriod(queryDto).test(vo))
                .filter(vo -> isValidMonthOverMonth(queryDto).test(vo))
                .filter(vo -> isValidYearOverYear(queryDto).test(vo))
                .filter(vo -> isValidTotalCount(queryDto).test(vo))
                .filter(vo->isValidUnit(queryDto).test(vo))
                .filter(vo -> isValidIndex(queryDto).test(vo))
                .filter(vo -> isValidGroup(queryDto).test(vo))
                .collect(Collectors.toList());

        int total = result.size();

        long startIndex = (queryDto.getCurrent()-1) * queryDto.getSize();
        long endIndex = Math.min(queryDto.getCurrent() * queryDto.getSize(), result.size());

        if (startIndex <result.size()) {
            result = result.subList((int) startIndex, (int) endIndex);
        }

        return new Page<CostResultStatementVo>(queryDto.getCurrent(),queryDto.getSize()).setTotal(total).setRecords(result);
    }

    @Override
    public List<CostResultStatementVo> indexStatementDetail(AccountStatementDetailQueryDto queryDto) {

        //根据时间生成一个voList
        List<CostResultStatementVo> voList = generateVoList(queryDto);

        //根据taskId获取task，再获取任务的accountType、dimension
        CostAccountTask task = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getId, queryDto.getTaskId()));
        //获取查询的这条数据的指标相关的信息
        CostTaskExecuteResultIndex indexInfo = costTaskExecuteResultIndexService.getById(queryDto.getIndexResultId());

        List<CostResultStatementVo> results = voList.stream().map(result -> {
            //查询是否存在accountType、dimension相同，且时间对应的任务
            CostAccountTask one = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                    .eq(CostAccountTask::getAccountType, task.getAccountType())
                    .eq(CostAccountTask::getDimension, task.getDimension())
                    .eq(CostAccountTask::getAccountStartTime, result.getAccountStartTime())
                    .eq(CostAccountTask::getAccountEndTime, result.getAccountEndTime())
                    .last("limit 1"));
            //不存在，返回只带有时间和维度的数据result
            if(one==null){
                return result;
            }
            QueryWrapper<CostTaskExecuteResultIndex> queryWrapper = Wrappers.query();
            queryWrapper.eq("task_id",one.getId())
                    .eq("index_id",indexInfo.getIndexId())
                    .eq("parent_id",indexInfo.getParentId())
                    .eq("unit_id",indexInfo.getUnitId())
                    .select("any_value(id) as id","index_id","any_value(unit_id) as unit_id","any_value(index_name) as index_name","parent_id","SUM(index_count) as indexCount")
                    .groupBy("index_id","unit_id","parent_id");
            CostTaskExecuteResultIndex i = costTaskExecuteResultIndexService.getOne(queryWrapper);

            CostResultStatementVo vo = BeanUtil.copyProperties(one, CostResultStatementVo.class);
            //指标计算结果不存在
            if(i==null){
                return vo;
            }
            //设置分组
            vo.setTotalCount(i.getIndexCount());
            vo.setIndexId(i.getIndexId());
            vo.setIndexName(i.getIndexName());
            vo.setIndexResultId(i.getId());
            vo.setParentId(i.getParentId());
            //设置分组相关信息
            vo.setUnitId(i.getUnitId());
            CostAccountUnit unit = costAccountUnitService.getById(i.getUnitId());
            vo.setUnitName(unit.getName());
            vo.setGroupId(unit.getAccountGroupCode());
            return getRatio(vo,one,"INDEX",null,null,i);
        }).collect(Collectors.toList());

        return results;
    }

    @Override
    public List<CostUnitIndexVo> itemStatementDetail(AccountStatementDetailQueryDto queryDto) {

        //taskId、unitId确定这条
        //此核算单元的总值
        QueryWrapper<CostTaskExecuteResult> unitWrapper = Wrappers.query();
        unitWrapper.eq("task_id",queryDto.getTaskId())
                .eq("unit_id",queryDto.getUnitId())
                .select("task_id", "unit_id", "SUM(total_count) as totalCount")
                .groupBy("task_id", "unit_id");
        CostTaskExecuteResult unit = costTaskExecuteResultService.getOne(unitWrapper);

        //此核算单元各核算指标的值
        QueryWrapper<CostTaskExecuteResultIndex> queryWrapper = Wrappers.query();
        queryWrapper.eq("task_id",queryDto.getTaskId())
                .eq("unit_id",queryDto.getUnitId())
                .select("any_value(id) as id","any_value(task_id) as task_id","index_id","any_value(unit_id) as unit_id",
                        "any_value(index_name) as index_name","any_value(parent_id) as parent_id","path","SUM(index_count) as indexCount")
                .groupBy("index_id","path");
        List<CostTaskExecuteResultIndex> indexList = costTaskExecuteResultIndexService.list(queryWrapper);

        List<CostUnitIndexVo> voList = indexList.stream().map(index -> {
            BigDecimal base = new BigDecimal("100");
            CostUnitIndexVo vo = BeanUtil.copyProperties(index, CostUnitIndexVo.class);
            vo.setTotalCount(index.getIndexCount());
            vo.setPercentage(unit.getTotalCount().compareTo(BigDecimal.ZERO) == 0?BigDecimal.ZERO:vo.getTotalCount().divide(unit.getTotalCount(),BigDecimal.ROUND_CEILING).multiply(base));

            //核算项的值
            QueryWrapper<CostTaskExecuteResultItem> wrapper = Wrappers.query();
            wrapper.eq("task_id", vo.getTaskId())
                    .eq("unit_id", vo.getUnitId())
                    .eq("index_id", vo.getIndexId())
                    .eq("path", vo.getPath())
                    .select("any_value(item_id) as item_id", "any_value(item_name) as item_name", "SUM(calculate_count) as calculateCount")
                    .groupBy("index_id", "path");

            List<CostTaskExecuteResultItem> itemList = costTaskExecuteResultItemService.list(wrapper);

            List<CostUnitItemVo> unitIndexVos = itemList.stream().map(item -> {
                CostUnitItemVo costUnitItemVo = BeanUtil.copyProperties(item, CostUnitItemVo.class);
                costUnitItemVo.setPercentage(costUnitItemVo.getCalculateCount().divide(vo.getTotalCount(),BigDecimal.ROUND_CEILING).multiply(base));
                return costUnitItemVo;
            }).collect(Collectors.toList());

            vo.setCostUnitItemVoList(unitIndexVos);
            return vo;
        }).collect(Collectors.toList());

        return voList;
    }

    @Override
    public List<IndexExcelVo> exportIndex() {

        List<CostResultStatementVo> list = indexStatementPage(new CostAccountStatementQueryDto()).getRecords();
        List<IndexExcelVo> excelVos = list.stream().map(vo ->{

            IndexExcelVo v = new IndexExcelVo();
            v.setIndexName(vo.getIndexName());
            v.setUnitName(vo.getUnitName());
            //设置核算分组
            java.util.Map<String, String> jsonMap = gson.fromJson(vo.getGroupId(), java.util.Map.class);
            v.setGroupId(jsonMap.get("label"));
            setExcelVo(vo,v);
            return v;
        }).collect(Collectors.toList());
        return excelVos;
    }

    @Override
    public List<CostResultStatementVo> unitStatementDetail(AccountStatementDetailQueryDto queryDto) {

        //生成一个voList
        List<CostResultStatementVo> voList=new ArrayList<>();
        //维度为年
        if(queryDto.getDimension().equals(DimensionEnum.YEAR.getCode())){
            voList = generateVoByYear(queryDto);
        }
        //维度为月
        else if(queryDto.getDimension().equals(DimensionEnum.MONTH.getCode())){
            voList = generateVoByMonth(queryDto);
        }
        else if(queryDto.getDimension().equals(DimensionEnum.QUARTER.getCode())){
            voList = generateVoByQuarter(queryDto);
        }
        else {
            voList = generateVoByDay(queryDto);
        }
        //此任务的信息
        CostAccountTask task = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getId, queryDto.getTaskId()));

        List<CostResultStatementVo> results = voList.stream().map(result -> {
            CostAccountTask one = costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                    .eq(CostAccountTask::getAccountType, task.getAccountType())
                    .eq(CostAccountTask::getDimension, task.getDimension())
                    .eq(CostAccountTask::getAccountStartTime, result.getAccountStartTime())
                    .eq(CostAccountTask::getAccountEndTime, result.getAccountEndTime())
                    .last("limit 1"));
            if(one==null){
                return result;
            }
            QueryWrapper<CostTaskExecuteResult> queryWrapper = Wrappers.query();
            queryWrapper.eq("task_id",one.getId())
                    .eq("unit_id",queryDto.getUnitId())
                    .select("task_id", "unit_id", "SUM(total_count) as totalCount")
                    .groupBy("task_id", "unit_id");
            CostTaskExecuteResult r = costTaskExecuteResultService.getOne(queryWrapper);

//            CostTaskExecuteResult r = costTaskExecuteResultService.getOne(Wrappers.<CostTaskExecuteResult>lambdaQuery()
//                    .eq(CostTaskExecuteResult::getTaskId, one.getId())
//                    .eq(CostTaskExecuteResult::getUnitId, queryDto.getUnitId()));

            CostResultStatementVo v = BeanUtil.copyProperties(one, CostResultStatementVo.class);
            if(r==null){
                return v;
            }
            //设置分组
            v.setUnitId(r.getUnitId());
            v.setUnitName(r.getUnitName());
            v.setTotalCount(r.getTotalCount());
            return getRatio(v, one, "UNIT", null,queryDto.getUnitId(),null);

        }).collect(Collectors.toList());

        return results;
    }

    @Override
    public List<UnitExcelVo> exportUnit() {
        List<CostResultStatementVo> list = unitStatementPage(new CostAccountStatementQueryDto()).getRecords();
        List<UnitExcelVo> excelVos = list.stream().map(vo ->{

            UnitExcelVo v = new UnitExcelVo();
            v.setUnitName(vo.getUnitName());
            v.setAccountTaskName(vo.getAccountTaskName());
            setExcelVo(vo,v);
            return v;
        }).collect(Collectors.toList());
        return excelVos;
    }

    /**
     * 获取同比、环比值
     * @param vo
     * @param task
     * @param code
     * @param groupId
     * @param unitId
     * @param index
     * @return
     */
    private CostResultStatementVo getRatio(CostResultStatementVo vo,CostAccountTask task,String code,String groupId,Long unitId,CostTaskExecuteResultIndex index){
        //todo 单位
        //支持统计
        if (task.getSupportStatistics().equals("1")) {
            //同比task
            CostAccountTask yearTask = getYearTask(task);
            //环比task
            CostAccountTask monthTask = new CostAccountTask();
            if (task.getDimension().equals(DimensionEnum.YEAR.getCode())) {
                monthTask=getYearTask(task);
            } else if (task.getDimension().equals(DimensionEnum.MONTH.getCode())) {
                monthTask = getMonthTask(task);
            } else if (task.getDimension().equals(DimensionEnum.QUARTER.getCode())) {
                monthTask = getQuarterTask(task);
            } else if (task.getDimension().equals(DimensionEnum.DAY.getCode())) {
                monthTask = getDayTask(task);
            }
            //总值
            YearRatio yearRatio = getYearRatio(vo.getTotalCount(), yearTask,code,groupId,unitId,index);
            //环比值
            MonthRatio monthRatio = getMonthRatio(vo.getTotalCount(), monthTask,code,groupId,unitId,index);

            vo.setYearRatio(yearRatio);
            vo.setMonthRatio(monthRatio);
        }
        return vo;
    }

    //封装同比
    private YearRatio getYearRatio(BigDecimal totalCount,CostAccountTask yearTask,String code,String groupId,Long unitId,CostTaskExecuteResultIndex index){

        BigDecimal base = new BigDecimal("100");
        //同比值
        BigDecimal yearCountNum=BigDecimal.ZERO;
        if(yearTask!=null){
            if(code.equals("RESULT")){
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                        .eq(CostTaskExecuteResult::getTaskId, yearTask.getId()));
                for (CostTaskExecuteResult result:results){
                    yearCountNum=yearCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("GROUP")){
                QueryWrapper<CostTaskExecuteResult> queryWrapper = Wrappers.query();
                queryWrapper.eq("task_id",yearTask.getId())
                        .eq("group_id",groupId)
                        .select("task_id", "group_id", "SUM(total_count) as totalCount")
                        .groupBy("task_id", "group_id");
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(queryWrapper);
                for (CostTaskExecuteResult result:results){
                    yearCountNum=yearCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("UNIT")){
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                        .eq(CostTaskExecuteResult::getTaskId, yearTask.getId())
                        .eq(CostTaskExecuteResult::getUnitId,unitId));
                for (CostTaskExecuteResult result:results){
                    yearCountNum=yearCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("INDEX")){
                QueryWrapper<CostTaskExecuteResultIndex> queryWrapper = Wrappers.query();
                queryWrapper.eq("task_id",yearTask.getId())
                        .eq("index_id",index.getIndexId())
                        .eq("unit_id",index.getUnitId())
                        .eq("parent_id",index.getParentId())
                        .select("any_value(id) as id","index_id","any_value(unit_id) as unit_id","any_value(index_name) as index_name","parent_id","SUM(index_count) as indexCount")
                        .groupBy("index_id","unit_id","parent_id");
                CostTaskExecuteResultIndex one = costTaskExecuteResultIndexService.getOne(queryWrapper);
                if(one!=null){
                    yearCountNum =one.getIndexCount();
                }

            }
        }
        if(yearCountNum==BigDecimal.ZERO){
            return new YearRatio(yearCountNum, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        BigDecimal increase = totalCount.subtract(yearCountNum);
        BigDecimal ratio = yearCountNum.compareTo(BigDecimal.ZERO) == 0?BigDecimal.ZERO:(increase.divide(yearCountNum,BigDecimal.ROUND_CEILING)).multiply(base);
        return new YearRatio(yearCountNum, ratio, increase);
    }

    //封装环比
    private MonthRatio getMonthRatio(BigDecimal totalCount,CostAccountTask monthTask,String code,String groupId,Long unitId,CostTaskExecuteResultIndex index){

        BigDecimal base = new BigDecimal("100");
        //环比值
        BigDecimal monthCountNum=BigDecimal.ZERO;
        if(monthTask!=null){
            if(code.equals("RESULT")){
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                        .eq(CostTaskExecuteResult::getTaskId, monthTask.getId()));
                for (CostTaskExecuteResult result:results){
                    monthCountNum=monthCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("GROUP")){
                QueryWrapper<CostTaskExecuteResult> queryWrapper = Wrappers.query();
                queryWrapper.eq("task_id",monthTask.getId())
                        .eq("group_id",groupId)
                        .select("task_id", "group_id", "SUM(total_count) as totalCount")
                        .groupBy("task_id", "group_id");
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(queryWrapper);
                for (CostTaskExecuteResult result:results){
                    monthCountNum=monthCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("UNIT")){
                List<CostTaskExecuteResult> results = costTaskExecuteResultService.list(Wrappers.<CostTaskExecuteResult>lambdaQuery()
                        .eq(CostTaskExecuteResult::getTaskId, monthTask.getId())
                        .eq(CostTaskExecuteResult::getUnitId,unitId));
                for (CostTaskExecuteResult result:results){
                    monthCountNum=monthCountNum.add(result.getTotalCount());
                }
            }
            else if(code.equals("INDEX")){
                QueryWrapper<CostTaskExecuteResultIndex> queryWrapper = Wrappers.query();
                queryWrapper.eq("task_id",monthTask.getId())
                        .eq("index_id",index.getIndexId())
                        .eq("unit_id",index.getUnitId())
                        .eq("parent_id",index.getParentId())
                        .select("any_value(id) as id","index_id","any_value(unit_id) as unit_id","any_value(index_name) as index_name","parent_id","SUM(index_count) as indexCount")
                        .groupBy("index_id","unit_id","parent_id");
                CostTaskExecuteResultIndex one = costTaskExecuteResultIndexService.getOne(queryWrapper);
                if(one!=null){
                    monthCountNum =one.getIndexCount();
                }
            }
        }
        if(monthCountNum==BigDecimal.ZERO){
            return new MonthRatio(monthCountNum, BigDecimal.ZERO, BigDecimal.ZERO);
        }
        //环比
        BigDecimal increaseNum = totalCount.subtract(monthCountNum);
        BigDecimal ratio = monthCountNum.compareTo(BigDecimal.ZERO) == 0?BigDecimal.ZERO:(increaseNum.divide(monthCountNum,BigDecimal.ROUND_CEILING)).multiply(base);

        return new MonthRatio(monthCountNum, ratio, increaseNum);
    }

    /**
     * 获取同比时间、环比年的task
     * @param task
     * @return
     */
    private CostAccountTask getYearTask(CostAccountTask task){
        LocalDate yearDateStart = task.getAccountStartTime().toLocalDate().minusYears(1);
        LocalDate yearDateEnd = task.getAccountEndTime().toLocalDate().minusYears(1);

        return costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getStatus, AccountTaskStatus.COMPLETED)
                .eq(CostAccountTask::getAccountType, task.getAccountType())
                .eq(CostAccountTask::getAccountStartTime, yearDateStart)
                .eq(CostAccountTask::getAccountEndTime, yearDateEnd)
                .last("limit 1"));
    }
    /**
     * 环比-月task
     * @param task
     * @return
     */
    private CostAccountTask getMonthTask(CostAccountTask task){
        LocalDate monthDateStart = task.getAccountStartTime().toLocalDate().minusMonths(1);
        LocalDate monthDateEnd = YearMonth.from(task.getAccountEndTime().toLocalDate().minusMonths(1)).atEndOfMonth();

        return costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getStatus, AccountTaskStatus.COMPLETED)
                .eq(CostAccountTask::getAccountType, task.getAccountType())
                .eq(CostAccountTask::getAccountStartTime, monthDateStart)
                .eq(CostAccountTask::getAccountEndTime, monthDateEnd)
                .last("limit 1"));
    }

    /**
     * 环比-季度task
     * @param task
     * @return
     */
    private CostAccountTask getQuarterTask(CostAccountTask task){
        LocalDate quarterDateStart = task.getAccountStartTime().toLocalDate().minusMonths(3);
        LocalDate quarterDateEnd =  YearMonth.from(task.getAccountEndTime().toLocalDate().minusMonths(3)).atEndOfMonth();

        return costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getStatus, AccountTaskStatus.COMPLETED)
                .eq(CostAccountTask::getAccountType, task.getAccountType())
                .eq(CostAccountTask::getAccountStartTime, quarterDateStart)
                .eq(CostAccountTask::getAccountEndTime, quarterDateEnd)
                .last("limit 1"));
    }

    /**
     * 环比-天task
     * @param task
     * @return
     */
    private CostAccountTask getDayTask(CostAccountTask task){
        LocalDate dayDateStart = task.getAccountStartTime().toLocalDate().minusDays(1);
        LocalDate dayDateEnd = task.getAccountEndTime().toLocalDate().minusDays(1);
        return costAccountTaskService.getOne(Wrappers.<CostAccountTask>lambdaQuery()
                .eq(CostAccountTask::getStatus, AccountTaskStatus.COMPLETED)
                .eq(CostAccountTask::getAccountType, task.getAccountType())
                .eq(CostAccountTask::getAccountStartTime, dayDateStart)
                .eq(CostAccountTask::getAccountEndTime, dayDateEnd)
                .last("limit 1"));
    }

    private List<CostResultStatementVo> generateVoByYear(AccountStatementDetailQueryDto queryDto){

        LocalDateTime startTime = LocalDateTime.of(Integer.parseInt(queryDto.getDetailDimStart().substring(0, 4)), Month.JANUARY, 1, 0, 0);
        LocalDateTime endTime = LocalDateTime.of(Integer.parseInt(queryDto.getDetailDimEnd().substring(0, 4)), Month.JANUARY, 1,0,0);

        List<CostResultStatementVo> vos = new ArrayList<>();
        LocalDateTime currentDateTime = startTime;
        while(currentDateTime.isBefore(endTime)||currentDateTime.equals(endTime)){

            LocalDateTime accountStartTime = currentDateTime;
            LocalDateTime accountEndTime = LocalDateTime.of(currentDateTime.getYear(),Month.DECEMBER,31,0,0);

            CostResultStatementVo vo = new CostResultStatementVo();
            vo.setAccountStartTime(accountStartTime);
            vo.setAccountEndTime(accountEndTime);
            vo.setDimension(queryDto.getDimension());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年");
            vo.setDetailDim(accountStartTime.format(formatter));

            vos.add(vo);
            //时间加一年
            currentDateTime = currentDateTime.plusYears(1);
        }
        return vos;
    }

    private List<CostResultStatementVo> generateVoByMonth(AccountStatementDetailQueryDto queryDto) {

        String dimStart = queryDto.getDetailDimStart();
        String dimEnd = queryDto.getDetailDimEnd();

//        LocalDateTime startTime = LocalDateTime.of(Integer.parseInt(dimStart.substring(0, 4)), Integer.parseInt(dimStart.substring(5,7)), 1, 0, 0);
//        LocalDateTime endTime = LocalDateTime.of(Integer.parseInt(dimEnd.substring(0, 4)),Integer.parseInt(dimEnd.substring(5,7)), 1,0,0);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy年MM月");

        Date dateS = null;
        try {
            dateS = dateFormat.parse(dimStart);
        } catch (ParseException e) {
            log.error("月份时间转换错误");
        }
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");

        int year = Integer.parseInt(yearFormat.format(dateS));
        Month month = Month.of(Integer.parseInt(monthFormat.format(dateS)));
        // 创建开始时间
        LocalDateTime startTime = LocalDateTime.of(year, month, 1,0,0,0);


        Date dateE = null;
        try {
            dateE = dateFormat.parse(dimEnd);
        } catch (ParseException e) {
            log.error("月份时间转换错误");
        }
        int yearE = Integer.parseInt(yearFormat.format(dateE));
        Month monthE = Month.of(Integer.parseInt(monthFormat.format(dateE)));
        // 创建结束时间
        LocalDateTime endTime = LocalDateTime.of(yearE, monthE, 1,0,0,0);


        List<CostResultStatementVo> vos = new ArrayList<>();
        LocalDateTime currentDateTime = startTime;
        while(currentDateTime.isBefore(endTime)||currentDateTime.equals(endTime)){

            LocalDateTime accountStartTime = currentDateTime;
            YearMonth yearMonth = YearMonth.of(currentDateTime.getYear(), currentDateTime.getMonth());

            LocalDateTime accountEndTime = LocalDateTime.of(currentDateTime.getYear(),currentDateTime.getMonth(),yearMonth.atEndOfMonth().getDayOfMonth(),0,0);

            CostResultStatementVo vo = new CostResultStatementVo();
            vo.setAccountStartTime(accountStartTime);
            vo.setAccountEndTime(accountEndTime);
            vo.setDimension(queryDto.getDimension());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月");
            vo.setDetailDim(accountStartTime.format(formatter));

            vos.add(vo);
            //时间加一月
            currentDateTime = currentDateTime.plusMonths(1);
        }
        return vos;
    }

    private List<CostResultStatementVo> generateVoByQuarter(AccountStatementDetailQueryDto queryDto){

        QuarterInfo startQuarter = parseQuarterData(queryDto.getDetailDimStart());
        QuarterInfo endQuarter = parseQuarterData(queryDto.getDetailDimEnd());

        List<CostResultStatementVo> vos = new ArrayList<>();

        QuarterInfo currentQuarter = startQuarter;

        while (currentQuarter.compareTo(endQuarter) <= 0) {
            LocalDateTime accountStartTime = LocalDateTime.of(currentQuarter.getYear(), currentQuarter.getQuarterStartMonth(), 1, 0, 0);
            LocalDateTime accountEndTime = LocalDateTime.of(currentQuarter.getYear(), currentQuarter.getQuarterEndMonth(), Month.of(currentQuarter.getQuarterEndMonth()).maxLength(), 0, 0);

            CostResultStatementVo vo = new CostResultStatementVo();
            vo.setAccountStartTime(accountStartTime);
            vo.setAccountEndTime(accountEndTime);

            vo.setDimension(queryDto.getDimension());
            String time = formatLocalDateTimeAsQuarter(accountEndTime);
            vo.setDetailDim(time);

            vos.add(vo);
            // 增加一个季度以继续下一个季度
            currentQuarter = currentQuarter.getNextQuarter();
        }
        return vos;
    }

    private List<CostResultStatementVo> generateVoByDay(AccountStatementDetailQueryDto queryDto){

        String dimStart = queryDto.getDetailDimStart();
        String dimEnd = queryDto.getDetailDimEnd();

        LocalDateTime startTime = LocalDateTime.of(Integer.parseInt(dimStart.substring(0, 4)), Integer.parseInt(dimStart.substring(5,7)),Integer.parseInt(dimStart.substring(8,10)), 0, 0);
        LocalDateTime endTime = LocalDateTime.of(Integer.parseInt(dimEnd.substring(0, 4)),Integer.parseInt(dimEnd.substring(5,7)), Integer.parseInt(dimEnd.substring(8,10)),0,0);

        List<CostResultStatementVo> vos = new ArrayList<>();

        LocalDateTime currentDateTime = startTime;
        while(currentDateTime.isBefore(endTime)||currentDateTime.equals(endTime)){

            LocalDateTime accountStartTime = currentDateTime;
            LocalDateTime accountEndTime = currentDateTime;

            CostResultStatementVo vo = new CostResultStatementVo();
            vo.setAccountStartTime(accountStartTime);
            vo.setAccountEndTime(accountEndTime);

            vo.setDimension(queryDto.getDimension());
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
            vo.setDetailDim(accountStartTime.format(formatter));

            vos.add(vo);
            //时间加一月
            currentDateTime = currentDateTime.plusDays(1);
        }
        return vos;
    }



    /**
     * 根据时间生成一个voList
     * @param queryDto
     * @return
     */
    private List<CostResultStatementVo> generateVoList(AccountStatementDetailQueryDto queryDto){
        //生成一个voList
        List<CostResultStatementVo> voList=new ArrayList<>();
        //维度为年
        if(queryDto.getDimension().equals(DimensionEnum.YEAR.getCode())){
            voList = generateVoByYear(queryDto);
        }
        //维度为月
        else if(queryDto.getDimension().equals(DimensionEnum.MONTH.getCode())){
            voList = generateVoByMonth(queryDto);
        }
        //维度为季
        else if(queryDto.getDimension().equals(DimensionEnum.QUARTER.getCode())){
            voList = generateVoByQuarter(queryDto);
        }
        //维度为天
        else {
            voList = generateVoByDay(queryDto);
        }
        return voList;
    }

    /**
     * 判断周期
     * @param queryDto
     * @return
     */
    private Predicate<CostResultStatementVo> isValidPeriod(CostAccountStatementQueryDto queryDto) {

        return vo -> {
            if(queryDto.getStartTime() == null ||queryDto.getEndTime() == null){
                return true;
            }
            else {
                return (vo.getAccountEndTime().isBefore(queryDto.getEndTime())||vo.getAccountEndTime().equals(queryDto.getEndTime()))&&
                        (vo.getAccountStartTime().isAfter(queryDto.getStartTime())||vo.getAccountStartTime().equals(queryDto.getStartTime()));
            }
        };
    }

    /**
     * 判断环比
     * @param queryDto
     * @return
     */
    private  Predicate<CostResultStatementVo> isValidMonthOverMonth(CostAccountStatementQueryDto queryDto) {
        return vo -> {
            if(StrUtil.isBlank(queryDto.getMonthOverMonth())){
                return true;
            }
            else {
                if (vo.getMonthRatio()!=null&&queryDto.getMonthOverMonth().equals(RationStatusNum.RISE.getCode())) {
                    return vo.getMonthRatio().getMonthIncrease().compareTo(BigDecimal.ZERO) > 0;
                } else if (vo.getMonthRatio()!=null&&queryDto.getMonthOverMonth().equals(RationStatusNum.FALL.getCode())) {
                    return vo.getMonthRatio().getMonthIncrease().compareTo(BigDecimal.ZERO) < 0;
                }
                else {
                    return true;
                }
            }
        };
    }

    /**
     * 判断同比
     * @param queryDto
     * @return
     */
    private  Predicate<CostResultStatementVo> isValidYearOverYear(CostAccountStatementQueryDto queryDto) {
        return vo -> {
            if( StrUtil.isBlank(queryDto.getYearOverYear())){
                return true;
            }
            else {
                if (vo.getYearRatio()!=null&&queryDto.getYearOverYear().equals(RationStatusNum.RISE.getCode())) {
                    return vo.getYearRatio().getYearIncrease().compareTo(BigDecimal.ZERO) > 0;
                } else if (vo.getYearRatio()!=null&&queryDto.getYearOverYear().equals(RationStatusNum.FALL.getCode())) {
                    return vo.getYearRatio().getYearIncrease().compareTo(BigDecimal.ZERO) < 0;
                }
                else {
                    return true;
                }
            }
        };
    }

    /**
     * 判断核算总值
     * @param queryDto
     * @return
     */
    private Predicate<CostResultStatementVo> isValidTotalCount(CostAccountStatementQueryDto queryDto) {
        return vo -> {
            if(queryDto.getTotalCountStart() == null ||queryDto.getTotalCountEnd() == null ){
                return true;
            }
            else {
                return vo.getTotalCount().compareTo(queryDto.getTotalCountStart()) >0&& vo.getTotalCount().compareTo( queryDto.getTotalCountEnd()) < 0;
            }
        };
    }

    /**
     * 判断核算分组
     * @param queryDto
     * @return
     */
    private Predicate<CostResultStatementVo> isValidGroup(CostAccountStatementQueryDto queryDto) {
        return vo -> {

            JsonObject jsonObject = gson.fromJson(vo.getGroupId(), JsonObject.class);
            String groupId = jsonObject.get("value").getAsString();
            if(StrUtil.isBlank(queryDto.getGroupId())){
                return true;
            }
            else {
                return groupId.equals(queryDto.getGroupId());
            }
        };
    }

    /**
     * 判断核算单元
     * @param queryDto
     * @return
     */
    private Predicate<CostResultStatementVo> isValidUnit(CostAccountStatementQueryDto queryDto) {

        return vo ->{
            if(queryDto.getUnitId()==null){
                return true;
            }
            else {
                return vo.getUnitId().equals(queryDto.getUnitId());
            }
        };
    }

    /**
     * 判断核算指标
     * @param queryDto
     * @return
     */
    private Predicate<CostResultStatementVo> isValidIndex(CostAccountStatementQueryDto queryDto) {
        return vo -> {
            if(queryDto.getIndexId()==null){
                return true;
            }
            else {
                return vo.getIndexId().equals(queryDto.getIndexId());
            }
        };
    }

    private void setExcelVo(CostResultStatementVo vo,BaseStatementExcelVo v){
        v.setDimension(vo.getDimension());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedStartTime = vo.getAccountStartTime().format(formatter);
        String formattedEndTime = vo.getAccountEndTime().format(formatter);
        String time = formattedStartTime + "至" + formattedEndTime;
        v.setAccountTime(time);
        if(vo.getYearRatio()!=null){
            v.setYearIncrease(vo.getYearRatio().getYearIncrease());
            v.setYearRatio(vo.getYearRatio().getYearRatio());
            v.setTotalCountYear(vo.getYearRatio().getTotalCountYear());
        }
        if(vo.getMonthRatio()!=null){
            v.setMonthIncrease(vo.getMonthRatio().getMonthIncrease());
            v.setMonthRatio(vo.getMonthRatio().getMonthRatio());
            v.setTotalCountMonth(vo.getMonthRatio().getTotalCountMonth());
        }
        v.setTotalCount(vo.getTotalCount());
    }
}
