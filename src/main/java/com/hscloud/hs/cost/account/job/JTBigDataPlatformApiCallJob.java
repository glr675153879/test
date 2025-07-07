package com.hscloud.hs.cost.account.job;

import com.hscloud.hs.cost.account.constant.enums.JTBigDataPlatformApiTypeEnum;
import com.hscloud.hs.cost.account.utils.BigDataPlatformApiClient;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** 金唐大数据平台api调用定时任务
 * @author banana
 * @create 2023-10-25 9:48
 */
@Slf4j
@Component
public class JTBigDataPlatformApiCallJob {

    @Autowired
    private BigDataPlatformApiClient bigDataPlatformApiClient;

    /**
     * 获取追溯系统物品（消毒信息）统计定时任务
     * 根据jobParam的入参信息区分是自定义调用还是定时任务调用
     */
    @XxlJob("disinfectionInfo")
    @Transactional(rollbackFor = Exception.class)
    public ReturnT<String> disinfectionInfoCallInterface() throws DocumentException {
        log.info("获取追溯系统物品（消毒信息）统计定时任务开始……");
        String jobParam = XxlJobHelper.getJobParam();
        log.info("获取请求参数信息：{}", jobParam);

        if(!"".equals(jobParam)){
            log.info("【本次调用为自定义调用】");
            String[] date = jobParam.split(",");
            String startDate = null, endDate = null;
            if(StringUtils.isNotBlank(date[0]) && StringUtils.isNotBlank(date[1])){
                startDate = date[0];
                endDate = date[1];
                log.info("入参开始时间：{}", startDate);
                log.info("入参结束时间：{}", endDate);
            }else{
                log.error("请求参数格式不合法");
                throw new BizException("请求参数格式不合法");
            }

            // 定义日期时间格式
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            // 将字符串转换为LocalDate类型
            LocalDate startLocalDate = LocalDate.parse(startDate, formatter);
            LocalDate endLocalDate = LocalDate.parse(endDate, formatter);

            //循环获取自定义调用日期区间的内容信息
            while(!startLocalDate.isAfter(endLocalDate)){
                //入参时间替换
                bigDataPlatformApiClient.replaceInputParamDate(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(),
                        startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                        startLocalDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
                //调取接口
                bigDataPlatformApiClient.save(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(),
                        bigDataPlatformApiClient.callInterface(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType()));
                startLocalDate = startLocalDate.plusDays(1);
            }
        }else{
            log.info("【本次调用为定时任务】");
            //获取昨天的LocalDateTime时间
            LocalDateTime yestday = LocalDateTime.now().minusDays(1);
            //入参时间替换
            bigDataPlatformApiClient.replaceInputParamDate(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(),
                    yestday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00")),
                    yestday.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59")));
            //调取接口
            bigDataPlatformApiClient.save(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType(),
                    bigDataPlatformApiClient.callInterface(JTBigDataPlatformApiTypeEnum.TRACINGITEMS.getType()));
        }
        log.info("获取追溯系统物品（消毒信息）统计定时任务完成……");
        return ReturnT.SUCCESS;
    }

}
