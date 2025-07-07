package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionSettingsManagementMapper;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.dto.SecondQueryDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionSettingsManagement;
import com.hscloud.hs.cost.account.model.vo.SecondDistributionSettingsManagementVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionSettingsManagementService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 分配设置管理绩效 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-15
 */
@Service
public class SecondDistributionSettingsManagementServiceImpl extends ServiceImpl<SecondDistributionSettingsManagementMapper, SecondDistributionSettingsManagement> implements ISecondDistributionSettingsManagementService {

    /**
     * 启停用管理绩效
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatus(EnableDto dto) {

        //根据核算指标id获取核算指标对象
        SecondDistributionSettingsManagement settingsManagement = this.getById(dto.getId());
        if (settingsManagement == null) {
            throw new BizException("该管理绩效不存在");
        }
        settingsManagement.setStatus(dto.getStatus());
        return updateById(settingsManagement);
    }

    /**
     * 新增管理岗位绩效
     * @param settingsManagement
     * @return
     */
    @Override
    public Boolean saveManagement(SecondDistributionSettingsManagement settingsManagement) {
//        HsUser user = SecurityUtils.getUser();
        //查询表中是否已存在该管理岗位
        List<SecondDistributionSettingsManagement> settingsManagementList = new SecondDistributionSettingsManagement().selectList(new LambdaQueryWrapper<SecondDistributionSettingsManagement>()
                .eq(SecondDistributionSettingsManagement::getPosition, settingsManagement.getPosition())
                .eq(SecondDistributionSettingsManagement::getUnitId,settingsManagement.getUnitId()));
        if (CollUtil.isNotEmpty(settingsManagementList)) {
            throw new BizException("此管理岗位已存在，不可重复添加");
        }
//        List<SecondDistributionSettingsManagement> managements = new ArrayList<>();
//        for (Long deptId : user.getDeptIds()) {
//            settingsManagement.setDeptId(deptId);
//            managements.add(settingsManagement);
//        }
        return this.save(settingsManagement);
    }

    /**
     * 获取管理绩效列表
     * @param queryDto
     * @return
     */
    @Override
    public Page<SecondDistributionSettingsManagementVo> getList(SecondQueryDto queryDto) {
        Page<SecondDistributionSettingsManagement> page = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        Page<SecondDistributionSettingsManagementVo> voPage = new Page<>(page.getCurrent(), page.getSize(),page.getTotal());
//        HsUser user = SecurityUtils.getUser();
        if (queryDto.getUnitId() == null) {
            return voPage;
        }
        // 执行分页查询
        IPage<SecondDistributionSettingsManagement> results = this.page(page, new LambdaQueryWrapper<SecondDistributionSettingsManagement>()
                .eq(queryDto.getUnitId()!=null,SecondDistributionSettingsManagement::getUnitId, queryDto.getUnitId()));
        // 将结果转换为VO对象
        List<SecondDistributionSettingsManagement> records = results.getRecords();
        List<SecondDistributionSettingsManagementVo> vos = new ArrayList<>(records.size());
        for (SecondDistributionSettingsManagement record : records) {
            SecondDistributionSettingsManagementVo vo = BeanUtil.copyProperties(record, SecondDistributionSettingsManagementVo.class);
            vos.add(vo);
        }

        voPage.setRecords(vos);
        return voPage;
    }

    @Override
    public Boolean updateManagement(SecondDistributionSettingsManagement settingsManagement) {
        SecondDistributionSettingsManagement byId = this.getById(settingsManagement);
        if (byId!=null && "1".equals(byId.getIsSystem())) {
            throw new BizException("系统字段不可修改");
        }
        return this.updateById(settingsManagement);
    }
}
