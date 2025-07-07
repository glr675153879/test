package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionSettingsSingleMapper;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsSingle;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsSingleVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsSingleService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分配设置单项绩效 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
@Service
public class SecondDistributionSettingsSingleServiceImpl extends ServiceImpl<SecondDistributionSettingsSingleMapper, SecondDistributionSettingsSingle> implements ISecondDistributionSettingsSingleService {

    /**
     * 启禁用单项绩效
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(EnableDto dto) {
        //根据核算指标id获取核算指标对象
        SecondDistributionSettingsSingle settingsSingle = this.getById(dto.getId());
        if (settingsSingle == null) {
            throw new BizException("该单项绩效不存在");
        }
        settingsSingle.setStatus(dto.getStatus());
        return updateById(settingsSingle);
    }

    /**
     * 新增单项绩效
     * @param settingsSingle
     */
    @Override
    public Boolean saveSingle(SecondDistributionSettingsSingle settingsSingle) {
//        HsUser user = SecurityUtils.getUser();
        //查询表中是否已存在该单项绩效
//        List<SecondDistributionSettingsSingle> singleList = new SecondDistributionSettingsSingle().selectList(new LambdaQueryWrapper<SecondDistributionSettingsSingle>()
//                .eq(SecondDistributionSettingsSingle::getName, settingsSingle.getName())
//                .in(SecondDistributionSettingsSingle::getDeptId, user.getDeptIds()));


        List<SecondDistributionSettingsSingle> singleList = new SecondDistributionSettingsSingle().selectList(new LambdaQueryWrapper<SecondDistributionSettingsSingle>()
                .eq(SecondDistributionSettingsSingle::getName, settingsSingle.getName())
                .eq(SecondDistributionSettingsSingle::getUnitId,settingsSingle.getUnitId()));
        if (CollUtil.isNotEmpty(singleList)) {
            throw new BizException("此单项绩效已存在，不可重复添加");
        }
//        List<SecondDistributionSettingsSingle> singles = new ArrayList<>();
//        for (Long deptId : user.getDeptIds()) {
//            settingsSingle.setDeptId(deptId);
//            singles.add(settingsSingle);
//        }
        return this.save(settingsSingle);
    }

    /**
     * 查询单项绩效列表
     * @param queryDto
     * @return
     */
    @Override
    public Page<SecondDistributionSettingsSingleVo> getList(SecondQueryDto queryDto) {


        Page<SecondDistributionSettingsSingle> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//        HsUser user = SecurityUtils.getUser();
        Page<SecondDistributionSettingsSingleVo> voPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());
        if (queryDto.getUnitId() == null) {
            return voPage;
        }
        // 执行分页查询
        IPage<SecondDistributionSettingsSingle> results = this.page(page, new LambdaQueryWrapper<SecondDistributionSettingsSingle>().eq(SecondDistributionSettingsSingle::getUnitId, queryDto.getUnitId()));
        // 将结果转换为VO对象
        List<SecondDistributionSettingsSingle> records = results.getRecords();
        List<SecondDistributionSettingsSingleVo> vos = new ArrayList<>(records.size());
        for (SecondDistributionSettingsSingle record : records) {
            SecondDistributionSettingsSingleVo vo = BeanUtil.copyProperties(record, SecondDistributionSettingsSingleVo.class);
            vos.add(vo);
        }

        voPage.setRecords(vos);
        return voPage;
    }
}
