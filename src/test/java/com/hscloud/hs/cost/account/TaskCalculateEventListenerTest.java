package com.hscloud.hs.cost.account;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.listener.TaskCalculateEventListener;
import com.hscloud.hs.cost.account.mapper.CostAccountIndexMapper;
import com.hscloud.hs.cost.account.mapper.CostVerificationResultIndexNewMapper;
import com.hscloud.hs.cost.account.model.entity.*;
import com.hscloud.hs.cost.account.model.pojo.AdsIncomePerformanceOther;
import com.hscloud.hs.cost.account.service.CostAccountTaskService;
import com.hscloud.hs.cost.account.service.CostTaskExecuteResultService;
import com.hscloud.hs.cost.account.service.CostVerificationResultIndexService;
import com.hscloud.hs.cost.account.service.ICostAccountIndexService;
import com.hscloud.hs.cost.account.service.impl.CostAccountIndexServiceImpl;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author 小小w
 * @date 2023/9/26 19:57
 */
@SpringBootTest
@Slf4j
public class TaskCalculateEventListenerTest {
    @Autowired
    TaskCalculateEventListener taskCalculateEventListener;

    @Autowired
    CostAccountTaskService costAccountTaskService;

    @Autowired
    CostAccountIndexServiceImpl costAccountIndexServiceImpl;
    @Autowired
    CostVerificationResultIndexService costVerificationResultIndexService;
    @Autowired
    ICostAccountIndexService costAccountIndexService;
    @Autowired
    CostAccountIndexMapper costAccountIndexMapper;


    @Autowired
    CostTaskExecuteResultService costTaskExecuteResultService;

    @Autowired
    StringRedisTemplate stringRedisTemplate;

    @Autowired
    CostVerificationResultIndexNewMapper costVerificationResultIndexNewMapper;

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


//    @Test
//    void testTaskCalculateEvent() {
//        BigDecimal result = new BigDecimal(0.0);
//
//        List<CostVerificationResultIndex> resultIndexList = new ArrayList<>();
//        //自定义传参
//        String jobParam = XxlJobHelper.getJobParam();
//        List<CostAccountIndex> costAccountIndexList = costAccountIndexMapper.selectList(new LambdaQueryWrapper<CostAccountIndex>().eq(CostAccountIndex::getDelFlag, "0").eq(CostAccountIndex::getStatus, "0"));
//        List<CostAccountUnit> costAccountUnitList = new CostAccountUnit().selectList(new LambdaQueryWrapper<CostAccountUnit>().eq(CostAccountUnit::getDelFlag, "0").eq(CostAccountUnit::getStatus, "0"));
//        List<Long> unitIdList = costAccountUnitList.stream()
//                .map(CostAccountUnit::getId)
//                .collect(Collectors.toList());
//        //获取所有的科室单元
//        Integer count = 0;
//        for (int i = costAccountIndexList.size()-1; i >= 0; i--) {
//            count++;
//            final CostAccountIndex costAccountIndex = costAccountIndexList.get(i);
////            //查看表中是否有
////            final CostVerificationResultIndex costVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
////                    .eq(CostVerificationResultIndex::getIndexId, costAccountIndex.getId()));
////            if (costVerificationResultIndex != null) {
////                continue;
////            }
//            Integer unitCount = 0;
//            for (Long unitId : unitIdList) {//遍历执行计算
//                unitCount++;
//                //查看表中是否有
//                final CostVerificationResultIndex newCostVerificationResultIndex = new CostVerificationResultIndex().selectOne(new LambdaQueryWrapper<CostVerificationResultIndex>()
//                        .eq(CostVerificationResultIndex::getIndexId, costAccountIndex.getId())
//                        .eq(CostVerificationResultIndex::getUnitId, unitId));
//                if (newCostVerificationResultIndex != null) {
//                    continue;
//                }
//                String startTime = "";
//                String endTime = "";
//                CostVerificationResultIndex resultIndex = new CostVerificationResultIndex();
//
//                if (StrUtil.isNotEmpty(jobParam) && jobParam != "") {
//                    log.info("本次为自定义传参调用");
//                    String[] split = jobParam.split(",");
//                    startTime = split[0];
//                    endTime = split[1];
//                    log.info("自定义传入参数开始时间为：" + split[0] + "结束时间为：" + split[1]);
//                } else {
//                    //获取当前时间的上一个月
//                    String formattedYearMonth = LocalDateTime.now().minusMonths(2).format(DateTimeFormatter.ofPattern("yyyyMM"));
//                    log.info("本次为定时任务传参调用");
//                    startTime = formattedYearMonth;
//                    endTime = formattedYearMonth;
//                }
//                resultIndex.setUnitId(Long.valueOf(unitId));
//                resultIndex.setAccountDate(startTime);
//                resultIndex.setOuterMostIndexId(Long.valueOf(costAccountIndex.getId()));
//                //创建辅助final变量
//                final String sTime = startTime;
//                final String eTime = endTime;
//                //多线程执行
////                Callable<ValidatorResultVo> task = () -> {
//                try {
//                    ValidatorResultVo indexVo = costAccountIndexServiceImpl.getVerificationIndex(costAccountIndex.getIndexFormula(), sTime, eTime, unitId.toString(), costAccountIndex.getStatisticalCycle() == null ? null : costAccountIndex.getStatisticalCycle().toString(), "Y", resultIndex, resultIndexList);
//                    result = (indexVo == null || StrUtil.isBlank(indexVo.getResult()) ? new BigDecimal(0.0) : new BigDecimal(indexVo.getResult()));
//                } catch (Exception e) {
//                    log.info("错误:",e);
//                    result=new BigDecimal(0.0);
//                }
//                //插入校验明细表
//                resultIndex.setIndexId(costAccountIndex.getId());
//                resultIndex.setCalculateFormulaDesc(costAccountIndex.getIndexFormula());
//                resultIndex.setIndexCount(result);
//                resultIndexList.add(resultIndex);
//                //插入表中
//                costVerificationResultIndexService.saveBatch(resultIndexList);
//                //删除集合中的数据
//                resultIndexList.clear();
//            }
//            //插入表中
//            costVerificationResultIndexService.saveBatch(resultIndexList);
//            //删除集合中的数据
//            resultIndexList.clear();
//        }
//
//    }


    Gson gson = new Gson();
    String key = "cost_account_item";

    @Test
    void testRedis() {
        HashMap<String, String> map = new HashMap<>();
        for (CostAccountItem costAccountItem : new CostAccountItem().selectAll()) {
            String value = gson.toJson(costAccountItem);
            String id = costAccountItem.getId().toString();

            map.put(id, value);
        }
        stringRedisTemplate.opsForHash().putAll(key, map);
        stringRedisTemplate.expire(key, CacheConstants.duration + new Random().nextInt(1000), TimeUnit.SECONDS);

    }


    @Test
    void testGetRedis() {

        HashMap<String, String> map = new HashMap<>();
        for (CostTaskExecuteResultItem costTaskExecuteResultItem : new CostTaskExecuteResultItem().selectAll()) {
            String value = gson.toJson(costTaskExecuteResultItem);
            String id = costTaskExecuteResultItem.getId().toString();

            map.put(id, value);
        }

        stringRedisTemplate.opsForHash().putAll(CacheConstants.COST_TASK_EXECUTE_RESULT_ITEM + "1721723774991396866", map);

//        CostAccountItem costAccountItem = gson.fromJson(stringRedisTemplate.opsForHash().get(key, "41").toString(), CostAccountItem.class);
//        System.out.println(costAccountItem);
    }


    @Test()
    void test2() {
        List<CostIndexConfigIndex> costIndexConfigIndexList = new CostIndexConfigIndex().selectList(new LambdaQueryWrapper<CostIndexConfigIndex>().eq(CostIndexConfigIndex::getConfigIndexId, 1707223175972626433L));
        if (CollUtil.isNotEmpty(costIndexConfigIndexList)) {
            List<Long> indexId = costIndexConfigIndexList.stream().map(CostIndexConfigIndex::getIndexId).collect(Collectors.toList());
            List<String> name = new CostAccountIndex().selectList(new LambdaQueryWrapper<CostAccountIndex>().in(CostAccountIndex::getId, indexId)).stream().map(CostAccountIndex::getName).collect(Collectors.toList());
            name.add("11");
            System.out.println(name.toString());

            throw new BizException(name + "中包含该指标，不允许删除");
        }
    }


    @Test
    void Test() {
        List<AdsIncomePerformanceOther> list = new AdsIncomePerformanceOther().selectAll();
        for (AdsIncomePerformanceOther adsIncomePerformanceOther : list) {
            System.out.println("\""+adsIncomePerformanceOther.getKhxm()+"\",");
        }
        System.out.println(123456);
    }

    @Test
    void Test3() {


    }

    /**
     * 此方法用于批量插入数据库
     *
     * @param resultIndexList
     * @param
     */
    private void insertBatchData(List<CostVerificationResultIndexNew> resultIndexList, int number) {
        List<CostVerificationResultIndexNew> batchList = new ArrayList<>();
        for (int i = 0; i < resultIndexList.size(); i++) {
            batchList.add(resultIndexList.get(i));
            if (batchList.size() % number == 0 || number == batchList.size() - 1) {
                // 执行批量插入操作
                costVerificationResultIndexNewMapper.insertBatchSomeColumn(resultIndexList);
                // 清空当前批次数据
                batchList.clear();
            }
        }
    }
}
