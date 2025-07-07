package com.hscloud.hs.cost.account.service.impl.second.async;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.constant.CacheConstants;
import com.hscloud.hs.cost.account.constant.enums.second.ActionType;
import com.hscloud.hs.cost.account.mapper.second.ProgrammeMapper;
import com.hscloud.hs.cost.account.model.dto.second.ProgrammePublishDTO;
import com.hscloud.hs.cost.account.model.entity.second.*;
import com.hscloud.hs.cost.account.model.vo.second.ProgrammeInfoVo;
import com.hscloud.hs.cost.account.service.impl.second.ProgDetailItemService;
import com.hscloud.hs.cost.account.service.impl.second.ProgProjectDetailService;
import com.hscloud.hs.cost.account.service.second.IGrantUnitService;
import com.hscloud.hs.cost.account.service.second.IProgProjectService;
import com.hscloud.hs.cost.account.service.second.IProgrammeService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.RedisUtil;
import com.hscloud.hs.cost.account.utils.RegularUtil;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 核算方案 服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProgrammeAsyncService {

    @Lazy
    @Autowired
    private IProgProjectService progProjectService;
    @Lazy
    @Autowired
    private IUnitTaskService unitTaskService;
    private final RedisUtil redisUtil;

    @Transactional(rollbackFor = Exception.class)
    @Async("secondAsync")
    public void syncByProgramme(Programme unitProgramme) {
        log.info("secondAsync.syncByProgramme == start"+Thread.currentThread().getId());
        String key = CacheConstants.SYNC_BY_PROGRAMME+unitProgramme.getId();
        try{
            if(redisUtil.get(key) != null){
                return;
            }else{
                redisUtil.setLock(key,1,30L, TimeUnit.SECONDS);
            }
            //同步分配方案
            progProjectService.syncByProgramme(unitProgramme);
            //同步分配任务
            unitTaskService.syncByProgramme(unitProgramme);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            redisUtil.unLock(key);
        }
        log.info("secondAsync.syncByProgramme == end"+Thread.currentThread().getId());
    }

}
