package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionSettingsDegreeMapper;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsDegree;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsDegreeVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsDegreeService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分配设置学位系数 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-20
 */
@Service
public class SecondDistributionSettingsDegreeServiceImpl extends ServiceImpl<SecondDistributionSettingsDegreeMapper, SecondDistributionSettingsDegree> implements ISecondDistributionSettingsDegreeService {

    /**
     * 新增学位系数
     * @param settingsDegree
     */
    @Override
    public Boolean saveRank(SecondDistributionSettingsDegree settingsDegree) {
//        HsUser user = SecurityUtils.getUser();
        //查询表中是否已存在该单项绩效
        List<SecondDistributionSettingsDegree> degreeList = new SecondDistributionSettingsDegree().selectList(new LambdaQueryWrapper<SecondDistributionSettingsDegree>()
                .eq(SecondDistributionSettingsDegree::getName, settingsDegree.getName())
                .eq(SecondDistributionSettingsDegree::getUnitId,settingsDegree.getUnitId()));
        if (CollUtil.isNotEmpty(degreeList)) {
            throw new BizException("此学位系数存在，不可重复添加");
        }
//        List<SecondDistributionSettingsDegree> degrees = new ArrayList<>();
//        for (Long deptId : user.getDeptIds()) {
//            settingsDegree.setDeptId(deptId);
//            degrees.add(settingsDegree);
//        }
       return this.save(settingsDegree);

    }

    /**
     * 启停用学位系数
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(EnableDto dto) {
        //根据核算指标id获取核算指标对象
        SecondDistributionSettingsDegree settingsDegree = this.getById(dto.getId());
        if (settingsDegree == null) {
            throw new BizException("该学位系数不存在");
        }
        settingsDegree.setStatus(dto.getStatus());
        return updateById(settingsDegree);
    }

    /**
     * 获取学位系数列表
     * @param queryDto
     * @return
     */
    @Override
    public Page<SecondDistributionSettingsDegreeVo> getList(SecondQueryDto queryDto) {
        Page<SecondDistributionSettingsDegree> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
//        HsUser user = SecurityUtils.getUser();
        Page<SecondDistributionSettingsDegreeVo> voPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());
        if (queryDto.getUnitId() == null) {
            return voPage;
        }
        // 执行分页查询
        IPage<SecondDistributionSettingsDegree> results = this.page(page,
                new LambdaQueryWrapper<SecondDistributionSettingsDegree>()
                        .eq(SecondDistributionSettingsDegree::getUnitId, queryDto.getUnitId()));
        // 将结果转换为VO对象
        List<SecondDistributionSettingsDegree> records = results.getRecords();
        List<SecondDistributionSettingsDegreeVo> vos = new ArrayList<>(records.size());
        for (SecondDistributionSettingsDegree record : records) {
            SecondDistributionSettingsDegreeVo vo = BeanUtil.copyProperties(record, SecondDistributionSettingsDegreeVo.class);
            vos.add(vo);
        }
        voPage.setRecords(vos);
        return voPage;
    }
}
