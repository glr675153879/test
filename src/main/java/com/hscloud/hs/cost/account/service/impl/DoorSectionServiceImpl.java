package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.DoorSectionMapper;
import com.hscloud.hs.cost.account.model.entity.DoorSectionEntity;
import com.hscloud.hs.cost.account.model.vo.DoorSectionVo;
import com.hscloud.hs.cost.account.service.DoorSectionService;
import lombok.var;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service("doorSectionService")
public class DoorSectionServiceImpl extends ServiceImpl<DoorSectionMapper, DoorSectionEntity> implements DoorSectionService {

    @Autowired
    private DoorSectionMapper doorSectionMapper;

    @Override
    public Page<DoorSectionVo> getDoorSectionPage(Page page, DoorSectionEntity entity) {
        Page<DoorSectionEntity> result = new Page<>(page.getCurrent(), page.getSize());
        LambdaQueryWrapper<DoorSectionEntity> wrapper = new LambdaQueryWrapper<DoorSectionEntity>();

        var resPage = page(page, wrapper);

        page.setRecords(resPage.getRecords());
        page.setTotal(resPage.getRecords().size());
        return page;
    }
}