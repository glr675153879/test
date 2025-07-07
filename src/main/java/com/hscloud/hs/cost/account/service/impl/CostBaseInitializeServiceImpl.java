package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostBaseInitializeMapper;
import com.hscloud.hs.cost.account.model.entity.CostBaseInitialize;
import com.hscloud.hs.cost.account.service.ICostBaseInitializeService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.data.monitor.listener.event.DBChangeEvent;
import com.pig4cloud.pigx.common.data.monitor.listener.event.EventContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 初始化完成表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-09-08
 */
@Service
public class CostBaseInitializeServiceImpl extends ServiceImpl<CostBaseInitializeMapper, CostBaseInitialize> implements ICostBaseInitializeService {

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;


    //初始化完成
    @Override
    public void initialize(CostBaseInitialize costBaseInitialize) {

        LambdaQueryWrapper<CostBaseInitialize> queryWrapper = new LambdaQueryWrapper<CostBaseInitialize>();
        queryWrapper.eq(StrUtil.isNotEmpty(costBaseInitialize.getAppCode()), CostBaseInitialize::getAppCode, costBaseInitialize.getAppCode())
                .eq(StrUtil.isNotEmpty(costBaseInitialize.getBizCode()), CostBaseInitialize::getBizCode, costBaseInitialize.getBizCode());
        CostBaseInitialize one = getOne(queryWrapper);
        if (one != null) {
            throw new BizException("已完成初始化");
        }
        costBaseInitialize.setStatus(true);
        this.save(costBaseInitialize);
        //TODO 确认监控
//        EventContent eventContent = new EventContent();
//        eventContent.setNeedSendMessage(false);
//        applicationEventPublisher.publishEvent(new DBChangeEvent(eventContent));
    }

    //获取是否初始化
    @Override
    public CostBaseInitialize getInitialize(CostBaseInitialize costBaseInitialize) {
        LambdaQueryWrapper<CostBaseInitialize> queryWrapper = new LambdaQueryWrapper<CostBaseInitialize>();
        queryWrapper.eq(StrUtil.isNotEmpty(costBaseInitialize.getAppCode()), CostBaseInitialize::getAppCode, costBaseInitialize.getAppCode())
                .eq(StrUtil.isNotEmpty(costBaseInitialize.getBizCode()), CostBaseInitialize::getBizCode, costBaseInitialize.getBizCode());
        CostBaseInitialize one = getOne(queryWrapper);
        if (one != null) {
            return one;
        }
        return null;
    }
}
