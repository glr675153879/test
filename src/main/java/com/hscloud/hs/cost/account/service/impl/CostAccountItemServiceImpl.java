package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import com.hscloud.hs.cost.account.constant.enums.AccountObject;
import com.hscloud.hs.cost.account.mapper.CostAccountItemMapper;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultItemMapper;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemInitDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemQueryDto;
import com.hscloud.hs.cost.account.model.dto.CostIndexConfigItemDto;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.vo.ValidatorResultVo;
import com.hscloud.hs.cost.account.service.CostAccountItemService;
import com.hscloud.hs.cost.account.service.CostVerificationResultItemService;
import com.hscloud.hs.cost.account.utils.ExpressionCheckHelper;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.hscloud.hs.cost.account.utils.SqlUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.data.monitor.listener.event.DBChangeEvent;
import com.pig4cloud.pigx.common.data.monitor.listener.event.EventContent;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import dm.jdbc.util.StringUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * @author Admin
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CostAccountItemServiceImpl extends ServiceImpl<CostAccountItemMapper, CostAccountItem> implements CostAccountItemService {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;

    private final CostAccountItemMapper costAccountItemMapper;
    private final CostVerificationResultItemMapper verificationResultItemMapper;
    private final LocalCacheUtils cacheUtils;
    private final SqlUtil sqlUtil;
    private final CostVerificationResultItemService costVerificationResultItemService;

    private final ExecutorService executorService = new ThreadPoolExecutor(
            8, // 核心线程数
            8, // 最大线程数
            0L, // 空闲线程存活时间
            TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(1), // 有界队列
            r -> {
                Thread thread = new Thread(r);
                thread.setName("index-calculate-" + thread.getId());
                return thread;
            },
            new ThreadPoolExecutor.CallerRunsPolicy() // 当队列已满时的拒绝策略
    );

    @Override
    public IPage<CostAccountItem> listItem(CostAccountItemQueryDto costAccountItemQueryDto) {

        Page page = new Page<>(costAccountItemQueryDto.getCurrent(), costAccountItemQueryDto.getSize());
        return costAccountItemMapper.listByQueryDto(page, costAccountItemQueryDto);
//        return baseMapper.selectPage(new Page<>(costAccountItemQueryDto.getCurrent(), costAccountItemQueryDto.getSize()),
//                new LambdaQueryWrapper<>(costAccountItem));

//        return baseMapper.selectPage(new Page<>(costAccountItemQueryDto.getCurrent(), costAccountItemQueryDto.getSize()),
//                new LambdaQueryWrapper<>(new CostAccountItem()).eq(Objects.nonNull(costAccountItemQueryDto.getGroupId()), CostAccountItem::getGroupId, costAccountItemQueryDto.getGroupId())
//                        .like(StrUtil.isNotBlank(costAccountItemQueryDto.getAccountItemName()), CostAccountItem::getAccountItemName, costAccountItemQueryDto.getAccountItemName()));
    }

    @Override
    public Boolean initItem(CostAccountItemInitDto costAccountItemInitDto) {
        //TODO
        //先启用所有的核算项
        //再启动监听任务
//        EventContent eventContent = new EventContent();
//        eventContent.setNeedSendMessage(true);
//        applicationEventPublisher.publishEvent(new DBChangeEvent(eventContent));
        return null;
    }


    @Override
    public Boolean enableItem(Long id, String status) {
        CostAccountItem costAccountItem = new CostAccountItem();
        costAccountItem.setId(id);
        costAccountItem.setStatus(status);
        return baseMapper.updateById(costAccountItem) > 0;
    }

    @Override
    public IPage<CostAccountItem> getItemsByName(String name) {
        return baseMapper.selectPage(new Page<>(1, 10), new LambdaQueryWrapper<>(new CostAccountItem()).like(CostAccountItem::getAccountItemName, name));
    }

    @Override
    public CostAccountItem getById(Long id) {
        return baseMapper.selectById(id);
    }

    @Override
    public void deleteById(Long id) {
        //删除核算项时，查看该核算项是否已被其他指标使用
        List<CostIndexConfigItem> costIndexConfigItemList = new CostIndexConfigItem().selectList(new LambdaQueryWrapper<CostIndexConfigItem>().eq(CostIndexConfigItem::getConfigId, id));
        if (CollUtil.isNotEmpty(costIndexConfigItemList)) {
            List<Long> indexId = costIndexConfigItemList.stream().map(CostIndexConfigItem::getIndexId).collect(Collectors.toList());
            List<String> name = new CostAccountIndex().selectList(new LambdaQueryWrapper<CostAccountIndex>().in(CostAccountIndex::getId, indexId)).stream().map(CostAccountIndex::getName).collect(Collectors.toList());
            throw new BizException("该项已配置在" + name + "核算指标中，不可删除");
        }
        this.removeById(id);
    }

    /**
     * 增量计算核算项的值
     *
     * @param costAccountItem
     */
    @Override
    public void getItemResult(CostAccountItem costAccountItem) {
        BigDecimal result = new BigDecimal(0.0);
        Queue<CostVerificationResultItem> resultItemList = new ConcurrentLinkedQueue<>();
        //获取当月时间时间
        String formattedYearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        //先查询删除定时任务核算项表中的数据
        List<CostVerificationResultItem> list = new CostVerificationResultItem().selectList(new LambdaQueryWrapper<CostVerificationResultItem>()
                .eq(CostVerificationResultItem::getItemId, costAccountItem.getId())
                .eq(CostVerificationResultItem::getAccountDate, formattedYearMonth));
        final List<Long> idList = list.stream().map(CostVerificationResultItem::getId).collect(Collectors.toList());
        //删除
        if(!idList.isEmpty()) {
            verificationResultItemMapper.deleteBatchIds(idList);
        }
        //新增计算
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getDelFlag, "0").eq(CostAccountUnit::getStatus, "0"));
        for (CostAccountUnit costAccountUnit : costAccountUnitList) {
            CostVerificationResultItem resultItem = new CostVerificationResultItem();
            resultItem.setItemName(costAccountItem.getAccountItemName());
            resultItem.setAccountDate(formattedYearMonth);
            resultItem.setItemId(costAccountItem.getId());
            //计算
            ValidatorResultVo itemVo = getItem(costAccountItem, costAccountUnit.getId(), formattedYearMonth, formattedYearMonth, resultItem, resultItemList);
            result = (itemVo == null || StrUtil.isBlank(itemVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(itemVo.getResult()));
            //封装
            resultItem.setUnitId(Long.valueOf(costAccountUnit.getId()));
            resultItem.setItemCount(result);
            resultItem.setUnitName(costAccountUnit.getName());
            resultItem.setDimension(costAccountItem.getDimension());
            resultItemList.add(resultItem);
        }
        //插入表中
        verificationResultItemMapper.insertBatchSomeColumn(resultItemList);
    }


    /**
     * 定时插入项计算结果明细
     */
    @XxlJob("saveItemResultDetail")
    public void saveItemResultDetail() {
        Queue<CostVerificationResultItem> resultItemList = new ConcurrentLinkedQueue<>();
        //自定义传参
        String jobParam = XxlJobHelper.getJobParam();
        //获取所有可用核算项
        List<CostAccountItem> costAccountItemList = new CostAccountItem().selectList(new LambdaQueryWrapper<CostAccountItem>()
                .eq(CostAccountItem::getStatus, "0")
                .eq(CostAccountItem::getDelFlag, "0"));
        //获取所有可用核算单元
        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>()
                .eq(CostAccountUnit::getDelFlag, "0")
                .eq(CostAccountUnit::getStatus, "0"));
        List<CostAccountUnit> synchronizedUnitList = Collections.synchronizedList(costAccountUnitList);
        List<CostAccountItem> synchronizedItemList = Collections.synchronizedList(costAccountItemList);
        //根据需求设置线程数量
        int threadCount = costAccountItemList.size();
        CountDownLatch latch = new CountDownLatch(threadCount);
        Integer count = 0;
        for (CostAccountItem costAccountItem:synchronizedItemList) {
            count++;
            //多线程执行
            executorService.execute(() -> {
                try {
                    BigDecimal result = new BigDecimal(0.0);
                    Integer unitCount = 0;
                    String startTime = "";
                    String endTime = "";
                    //设置时间
                    if (StrUtil.isNotEmpty(jobParam) && jobParam != "") {
                        final List<String> times = ExpressionCheckHelper.getIds(jobParam);
                        startTime = times.get(0);
                        endTime =times.get(1);
                        log.info("自定义传入参数开始时间为：" + startTime + "结束时间为：" + endTime);
                    } else {
                        //获取当前时间的上一个月
                        String formattedYearMonth = LocalDateTime.now().minusMonths(1).format(DateTimeFormatter.ofPattern("yyyyMM"));
                        startTime = formattedYearMonth;
                        endTime = formattedYearMonth;
                        log.info("默认定时任务开始时间为:"+ startTime + "结束时间为：" + endTime);
                    }
                    for (CostAccountUnit costAccountUnit : synchronizedUnitList) {
                        unitCount++;
                        //查询表中是否有数据,有就跳过(新增核算项的情况)
                        CostVerificationResultItem costVerificationResultItem=new CostVerificationResultItem().selectOne(new LambdaQueryWrapper<CostVerificationResultItem>()
                                .eq(CostVerificationResultItem::getUnitId,costAccountUnit.getId())
                                .eq(CostVerificationResultItem::getItemId,costAccountItem.getId())
                                .eq(CostVerificationResultItem::getAccountDate,startTime));
                        if (costVerificationResultItem!=null){
                            continue;
                        }
                        CostVerificationResultItem resultItem = new CostVerificationResultItem();
                        resultItem.setItemName(costAccountItem.getAccountItemName());
                        resultItem.setAccountDate(startTime);
                        resultItem.setItemId(costAccountItem.getId());
                        //计算
                        ValidatorResultVo itemVo = getItem(costAccountItem, costAccountUnit.getId(), startTime, endTime, resultItem, resultItemList);
                        result = (itemVo == null || StrUtil.isBlank(itemVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(itemVo.getResult()));
                        //封装
                        resultItem.setUnitId(Long.valueOf(costAccountUnit.getId()));
                        resultItem.setItemCount(result);
                        resultItem.setUnitName(costAccountUnit.getName());
                        resultItem.setDimension(costAccountItem.getDimension());
                        resultItemList.add(resultItem);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        try {
            latch.await(); // 等待所有线程完成
        } catch (Exception e) {
            log.info("错误", e);
        }
        //插入表格
        //costVerificationResultItemService.saveBatch(resultItemList);
        verificationResultItemMapper.insertBatchSomeColumn(new ArrayList<>(resultItemList));
        resultItemList.clear();
    }

    /**
     * 此方法用于定时任务计算核算项
     *
     * @param costAccountItem
     * @param unitId
     * @param startTime
     * @param endTime
     * @param resultItem
     * @param resultItemList
     * @return
     */
    private ValidatorResultVo getItem(CostAccountItem costAccountItem, Long unitId, String startTime, String endTime, CostVerificationResultItem resultItem, Queue<CostVerificationResultItem> resultItemList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //构造计算配置
        Map<String, String> map = new HashMap<>();
        map.put("startTime", startTime);
        map.put("endTime", endTime);
        if (StrUtil.isBlank(costAccountItem.getDimension())) {
            vo.setErrorMsg("核算项" + costAccountItem.getAccountItemName() + "未配置核算维度");
            return vo;
        }
        //核算粒度判断
        switch (new JSONObject(costAccountItem.getDimension()).getStr("value")) {
            //科室单元
            case AccountObject.KPI_OBJECT_DEPT_UNIT:
                vo = getDeptUnit(costAccountItem, unitId, map, resultItem, resultItemList);
                break;
            //人员
            case AccountObject.KPI_OBJECT_PERSON:
                vo = getPerson(costAccountItem, unitId, map, resultItem, resultItemList);
                break;
            //科室
            case AccountObject.KPI_OBJECT_DEPT:
                vo = getDept(costAccountItem, unitId, map, resultItem, resultItemList);
                break;
//            //全院
//            case AccountObject.KPI_OBJECT_ALL:
//                vo = getAll(costIndexConfigItemDto, objectIdType, objectId, map);
//                break;
            default:
                break;
        }
        return vo;
    }

    /**
     * 此方法用于计算核算维度为科室单元的值
     *
     * @return
     */
    private ValidatorResultVo getDeptUnit(CostAccountItem costAccountItem, Long unitId, Map<String, String> map, CostVerificationResultItem resultItem, Queue<CostVerificationResultItem> resultItemList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //获取sql语句
        String unitConfig = costAccountItem.getConfig();
        if (StringUtil.isEmpty(unitConfig)) {
            vo.setErrorMsg("核算项" + costAccountItem.getAccountItemName() + "查询不到对应的sql语句");
            return vo;
        }
        //构造对象
        CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem();
        BeanUtil.copyProperties(resultItem, costVerificationResultItem);

        map.put("account_unit_id", unitId + "");
        String unit = sqlUtil.executeSql(unitConfig, map);
        //封装信息
        costVerificationResultItem.setObjectCount(unit == null ? new BigDecimal(0.0) : new BigDecimal(unit));
        costVerificationResultItem.setObjectId(unitId);
        costVerificationResultItem.setType("Unit");
        resultItemList.add(costVerificationResultItem);
        vo.setResult(unit);
        return vo;
    }

    /**
     * 此方法用于计算核算维度为人员的值
     *
     * @return
     */
    private ValidatorResultVo getPerson(CostAccountItem costAccountItem, Long unitId, Map<String, String> map, CostVerificationResultItem resultItem, Queue<CostVerificationResultItem> resultItemList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> userMap = new HashMap<>();
        //获取sql语句
        String config = costAccountItem.getConfig();
        if (StringUtil.isEmpty(config)) {
            vo.setErrorMsg("核算项" + costAccountItem.getAccountItemName() + "查询不到对应的sql语句");
            return vo;
        }
        //获取该科室单元下所有科室
        final List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                .eq(CostUnitRelateInfo::getType, "dept"));
        //判断科室单元下是否有科室,没有则直接查询科室单元下的人员
        if (deptList.size() > 0) {
            //获取科室下所有的人员
            List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());
            userMap = sqlUtil.getUserIdsAndNamesByDeptIds(deptIds);
        }
        //获取该科室单元下所有的人员
        List<CostUnitRelateInfo> userList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                .eq(CostUnitRelateInfo::getType, "user"));
        //获取该科室单元下所有不参与核算的人员
        List<CostUnitExcludedInfo> excludedUserList = new CostUnitExcludedInfo().selectList(Wrappers.<CostUnitExcludedInfo>lambdaQuery()
                .eq(CostUnitExcludedInfo::getAccountUnitId, unitId)
                .eq(CostUnitExcludedInfo::getType, "user"));
        Map<Long, String> idNames = userList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitRelateInfo::getName));
        Map<Long, String> excludedIdNames = excludedUserList.stream().collect(Collectors.toMap(info -> Long.parseLong(info.getRelateId()), CostUnitExcludedInfo::getName));
        //将所有关联科室的人添加到部门获取的map
        userMap.putAll(idNames);
        //将所有不参与核算人员删除
        excludedIdNames.forEach(userMap::remove);
        if (userMap.isEmpty()) {
            vo.setErrorMsg(unitId + "查询不到人员");
            return vo;
        }
        //遍历求值
        for (Map.Entry<Long, String> entry : userMap.entrySet()) {
            //构造对象
            CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem();
            BeanUtil.copyProperties(resultItem, costVerificationResultItem);
            Long userId = entry.getKey();
            map.put("user_id", userId.toString());
            //执行查询到的sql
            String person = sqlUtil.executeSql(config, map);
            map.remove("user_id", userId);
            result = result.add(person == null ? new BigDecimal(0.0) : new BigDecimal(person));
            //封装信息
            costVerificationResultItem.setObjectCount(person == null ? new BigDecimal(0.0) : new BigDecimal(person));
            costVerificationResultItem.setObjectId(userId);
            costVerificationResultItem.setType("User");
            resultItemList.add(costVerificationResultItem);
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于计算核算维度为科室的值
     *
     * @return
     */
    private ValidatorResultVo getDept(CostAccountItem costAccountItem, Long unitId, Map<String, String> map, CostVerificationResultItem resultItem, Queue<CostVerificationResultItem> resultItemList) {
        ValidatorResultVo vo = new ValidatorResultVo();
        BigDecimal result = new BigDecimal(0.0);
        Map<Long, String> deptMap = new HashMap<>();
        //获取sql语句
        String config = costAccountItem.getConfig();
        if (StringUtil.isEmpty(config)) {
            vo.setErrorMsg("核算项" + costAccountItem.getAccountItemName() + "查询不到对应的sql语句");
            return vo;
        }
        //获取该科室单元下所有科室
        final List<CostUnitRelateInfo> deptList = new CostUnitRelateInfo().selectList(Wrappers.<CostUnitRelateInfo>lambdaQuery()
                .eq(CostUnitRelateInfo::getAccountUnitId, unitId)
                .eq(CostUnitRelateInfo::getType, "dept"));
        if (deptList == null || deptList.size() < 1) {
            vo.setErrorMsg("科室单元" + unitId + "下没有科室");
            return vo;
        }
        //获取科室的id并去重
        List<Long> deptIds = deptList.stream().map(CostUnitRelateInfo::getRelateId).map(Long::parseLong).collect(Collectors.toList());

        //获取科室code
        deptMap = sqlUtil.getDeptCodesByDeptIds(deptIds);
        for (Map.Entry<Long, String> entry : deptMap.entrySet()) {
            //构造对象
            CostVerificationResultItem costVerificationResultItem = new CostVerificationResultItem();
            BeanUtil.copyProperties(resultItem, costVerificationResultItem);

            String code = entry.getValue();
            map.put("dept_code", code);
            //执行查询到的sql
            String dept = sqlUtil.executeSql(config, map);
            map.remove("dept_code", code);
            result = result.add(dept == null ? new BigDecimal(0.0) : new BigDecimal(dept));
            //封装信息
            costVerificationResultItem.setObjectCount(dept == null ? new BigDecimal(0.0) : new BigDecimal(dept));
            costVerificationResultItem.setObjectId(Long.valueOf(code));
            costVerificationResultItem.setType("Dept");
            resultItemList.add(costVerificationResultItem);
        }
        vo.setResult(result + "");
        return vo;
    }

    /**
     * 此方法用于计算核算维度为全院的值
     *
     * @return
     */
    private ValidatorResultVo getAll(CostIndexConfigItemDto costIndexConfigItemDto, String objectIdType, String objectId, Map<String, String> map) {
        ValidatorResultVo vo = new ValidatorResultVo();
        //获取sql语句
        String deptConfig = cacheUtils.getCostAccountItem(costIndexConfigItemDto.getConfigId()).getConfig();
        if (StringUtil.isEmpty(deptConfig)) {
            vo.setErrorMsg("查询不到对应的sql语句");
            return vo;
        }
        String all = sqlUtil.executeSql(deptConfig, map);
        vo.setResult(all);
        return vo;
    }


}
