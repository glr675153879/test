package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionSettingsRankMapper;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsRank;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsRankVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsRankService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分配设置职称绩效 服务实现类
 * </p>
 *
 * @author
 * @since 2023-11-20
 */
@Service
public class SecondDistributionSettingsRankServiceImpl extends ServiceImpl<SecondDistributionSettingsRankMapper, SecondDistributionSettingsRank> implements ISecondDistributionSettingsRankService {

    /**
     * 新增职称绩效
     *
     * @param settingsRank
     */
    @Override
    public Boolean saveRank(SecondDistributionSettingsRank settingsRank) {
//        HsUser user = SecurityUtils.getUser();
        //查询表中是否已存在该单项绩效
        List<SecondDistributionSettingsRank> rankList = new SecondDistributionSettingsRank().selectList(new LambdaQueryWrapper<SecondDistributionSettingsRank>()
                .eq(SecondDistributionSettingsRank::getName, settingsRank.getName())
                .eq(SecondDistributionSettingsRank::getUnitId,settingsRank.getUnitId()));
        if (CollUtil.isNotEmpty(rankList)) {
            throw new BizException("此职称绩效已存在，不可重复添加");
        }
//        List<SecondDistributionSettingsRank> ranks = new ArrayList<>();
//        for (Long deptId : user.getDeptIds()) {
//            settingsRank.setDeptId(deptId);
//            ranks.add(settingsRank);
//        }

        return this.save(settingsRank);
    }

    /**
     * 启停用职称绩效
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(EnableDto dto) {
        //根据核算指标id获取核算指标对象
        SecondDistributionSettingsRank settingsRank = this.getById(dto.getId());
        if (settingsRank == null) {
            throw new BizException("该职称绩效不存在");
        }
        settingsRank.setStatus(dto.getStatus());
        return updateById(settingsRank);
    }

    /**
     * 获取职称绩效列表
     * @param queryDto
     * @return
     */
    @Override
    public Page<SecondDistributionSettingsRankVo> getList(SecondQueryDto queryDto) {
        Page<SecondDistributionSettingsRank> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//        HsUser user = SecurityUtils.getUser();
        Page<SecondDistributionSettingsRankVo> voPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());
        if (queryDto.getUnitId() == null) {
            return voPage;
        }
        Page<SecondDistributionSettingsRank> results = new SecondDistributionSettingsRank().selectPage(page, new LambdaQueryWrapper<SecondDistributionSettingsRank>()
                .eq(SecondDistributionSettingsRank::getUnitId, queryDto.getUnitId()));
        List<SecondDistributionSettingsRank> records = results.getRecords();

        List<SecondDistributionSettingsRankVo> voList = new ArrayList<>();
        //返回封装vo对象
        for (SecondDistributionSettingsRank settingsRank : records) {
            SecondDistributionSettingsRankVo settingsDegreeVo = BeanUtil.copyProperties(settingsRank, SecondDistributionSettingsRankVo.class);
            voList.add(settingsDegreeVo);
        }

        voPage.setRecords(voList);
        voPage.setTotal(voList.size());
        return voPage;
    }
}
