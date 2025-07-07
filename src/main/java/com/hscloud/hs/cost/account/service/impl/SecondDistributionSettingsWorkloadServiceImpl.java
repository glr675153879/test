package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionSettingsWorkloadMapper;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsWorkload;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsWorkloadVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsWorkloadService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分配设置工作量绩效 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-16
 */
@Service
public class SecondDistributionSettingsWorkloadServiceImpl extends ServiceImpl<SecondDistributionSettingsWorkloadMapper, SecondDistributionSettingsWorkload> implements ISecondDistributionSettingsWorkloadService {

    /**
     * 修改工作量绩效状态
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(EnableDto dto) {

        //根据核算指标id获取核算指标对象
        SecondDistributionSettingsWorkload settingsWorkload = this.getById(dto.getId());
        if (settingsWorkload == null) {
            throw new BizException("该工作量绩效不存在");
        }
        settingsWorkload.setStatus(dto.getStatus());
        return updateById(settingsWorkload);

    }

    /**
     * 新增工作量绩效
     * @param settingsWorkload
     * @return
     */
    @Override
    public Boolean saveWorkload(SecondDistributionSettingsWorkload settingsWorkload) {
//        HsUser user = SecurityUtils.getUser();
        //查询表中是否已存在该工作量绩效
        List<SecondDistributionSettingsWorkload> workloadList = new SecondDistributionSettingsWorkload().selectList(new LambdaQueryWrapper<SecondDistributionSettingsWorkload>()
                .eq(SecondDistributionSettingsWorkload::getName, settingsWorkload.getName())
                .eq(SecondDistributionSettingsWorkload::getUnitId,settingsWorkload.getUnitId()));
        if (CollUtil.isNotEmpty(workloadList)) {
            throw new BizException("此工作量绩效已存在，不可重复添加");
        }
//        List<SecondDistributionSettingsWorkload> workloads = new ArrayList<>();
//        for (Long deptId : user.getDeptIds()) {
//            settingsWorkload.setDeptId(deptId);
//            workloads.add(settingsWorkload);
//        }
        return this.save(settingsWorkload);
    }

    /**
     * 获取工作量绩效列表
     * @param queryDto
     * @return
     */
    @Override
    public Page<SecondDistributionSettingsWorkloadVo> getList(SecondQueryDto queryDto) {

        Page<SecondDistributionSettingsWorkload> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//        HsUser user = SecurityUtils.getUser();
        Page<SecondDistributionSettingsWorkloadVo> voPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());
        if (queryDto.getUnitId() == null) {
            return voPage;
        }
        // 执行分页查询
        IPage<SecondDistributionSettingsWorkload> results = this.page(page,
                new LambdaQueryWrapper<SecondDistributionSettingsWorkload>()
                        .eq(SecondDistributionSettingsWorkload::getUnitId, queryDto.getUnitId()));
        // 将结果转换为VO对象
        List<SecondDistributionSettingsWorkload> records = results.getRecords();
        List<SecondDistributionSettingsWorkloadVo> vos = new ArrayList<>(records.size());
        for (SecondDistributionSettingsWorkload record : records) {
            SecondDistributionSettingsWorkloadVo vo = BeanUtil.copyProperties(record, SecondDistributionSettingsWorkloadVo.class);
            vos.add(vo);
        }

        voPage.setRecords(vos);
        return voPage;
    }
}
