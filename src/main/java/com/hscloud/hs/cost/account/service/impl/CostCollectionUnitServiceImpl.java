package com.hscloud.hs.cost.account.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountUnitMapper;
import com.hscloud.hs.cost.account.mapper.CostCollectionUnitMapper;
import com.hscloud.hs.cost.account.model.dto.AccountDepartmentDto;
import com.hscloud.hs.cost.account.model.dto.AccountUnitIdAndNameDto;
import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitDto;
import com.hscloud.hs.cost.account.model.dto.CostCollectionUnitQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostCollectionUnit;
import com.hscloud.hs.cost.account.model.vo.CostCollectionUnitVo;
import com.hscloud.hs.cost.account.service.ICostCollectionUnitService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 归集单元表 服务实现类
 * </p>
 *
 * @author
 * @since 2023-09-05
 */
@Service
public class CostCollectionUnitServiceImpl extends ServiceImpl<CostCollectionUnitMapper, CostCollectionUnit> implements ICostCollectionUnitService {

    @Autowired
    private CostCollectionUnitMapper costCollectionUnitMapper;


    @Autowired
    private CostAccountUnitMapper costAccountUnitMapper;

    /**
     * 新增归集单元
     * @param dto
     * @return
     */
    @Override
    @Transactional
    public void saveCollectionUnit(CostCollectionUnitDto dto) {
        if (StrUtil.isEmpty(dto.getCollectionName()) || dto.getCollectionAccountDepartment() == null || CollUtil.isEmpty(dto.getAccountUnitIds())) {
            throw new BizException("必填参数不能为空");
        }
        LambdaQueryWrapper<CostCollectionUnit> queryWrapper = new LambdaQueryWrapper<CostCollectionUnit>();
        queryWrapper.eq(CostCollectionUnit::getCollectionName, dto.getCollectionName());
        CostCollectionUnit collectionUnit = getOne(queryWrapper);
        if (collectionUnit != null) {
            throw new BizException("归集单元已存在，不能重复添加");
        }
        CostCollectionUnit costCollectionUnit = BeanUtil.copyProperties(dto, CostCollectionUnit.class);
        costCollectionUnit.setCollectionAccountDepartmentId(dto.getCollectionAccountDepartment().getId());
        costCollectionUnit.setCollectionAccountDepartmentName(dto.getCollectionAccountDepartment().getName());
        costCollectionUnit.setCollectionAccountDepartmentType(dto.getCollectionAccountDepartment().getType());

        save(costCollectionUnit);
        //插入中间表数据
        for (Long accountUnitId : dto.getAccountUnitIds()) {
            costCollectionUnitMapper.saveAccountCollection(costCollectionUnit.getId(), accountUnitId);
        }

    }

    /**
     * 修改归集单元
     * @param dto
     * @return
     */
    @Override
    public Boolean updateCollectionUnit(CostCollectionUnitDto dto) {
        if (dto.getId() == null || StrUtil.isEmpty(dto.getCollectionName()) || dto.getCollectionAccountDepartment() == null || CollUtil.isEmpty(dto.getAccountUnitIds())) {
            throw new BizException("必填参数不能为空");
        }
        //根据归集单元id查询关联的科室单元id
        LambdaQueryWrapper<CostCollectionUnit> queryWrapper = new LambdaQueryWrapper<CostCollectionUnit>();
        queryWrapper.eq(CostCollectionUnit::getId, dto.getId());
        CostCollectionUnit oldCostCollectionUnit = getOne(queryWrapper);
        if (oldCostCollectionUnit == null) {
            throw new BizException("归集单元不存在");
        }
        //根据归集单元id查询关联的分摊科室单元ids
        List<Long> accountUnitIds = costCollectionUnitMapper.getAccountUnitIds(dto.getId());
        //如果修改了分摊科室id，则修改中间表
        if (!accountUnitIds.equals(dto.getAccountUnitIds())) {
            costCollectionUnitMapper.removeAccountUnitIds(dto.getId());
            for (Long accountUnitId : dto.getAccountUnitIds()) {
                costCollectionUnitMapper.saveAccountCollection(oldCostCollectionUnit.getId(), accountUnitId);
            }
        }
        CostCollectionUnit costCollectionUnit = BeanUtil.copyProperties(dto, CostCollectionUnit.class);
        return this.updateById(costCollectionUnit);
    }

    /**
     * 查询归集单元列表
     * @return
     */
    @Override
    public IPage<CostCollectionUnitVo> listCollectionUnit(CostCollectionUnitQueryDto queryDto) {

        Page<CostCollectionUnitVo> collectionUnitPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());

        //获取归集单元列表
        LambdaQueryWrapper<CostCollectionUnit> queryWrapper = new LambdaQueryWrapper<CostCollectionUnit>();
        queryWrapper.eq(queryDto.getId() != null,CostCollectionUnit::getId,queryDto.getId())
                .like(StrUtil.isNotEmpty(queryDto.getCollectionName()),CostCollectionUnit::getCollectionName,queryDto.getCollectionName())
                .ge(queryDto.getBeginTime() != null, CostCollectionUnit::getCreateTime, queryDto.getBeginTime())  // 如果startTime不为null，则添加查询条件 >= startTime
                .le(queryDto.getEndTime() != null, CostCollectionUnit::getCreateTime, queryDto.getBeginTime())
                .eq(CostCollectionUnit::getDelFlag,'0');
        List<CostCollectionUnit> costCollectionUnits = list(queryWrapper);
        if (CollUtil.isEmpty(costCollectionUnits)) {
            return null;
        }
        List<CostCollectionUnitVo> costCollectionUnitVos = new ArrayList<>();
        for (CostCollectionUnit costCollectionUnit : costCollectionUnits) {
            CostCollectionUnitVo costCollectionUnitVo = BeanUtil.copyProperties(costCollectionUnit, CostCollectionUnitVo.class);
            //根据归集单元id查询关联的科室单元id
            AccountDepartmentDto accountDepartmentDto = new AccountDepartmentDto();
            accountDepartmentDto.setId(costCollectionUnit.getCollectionAccountDepartmentId());
            accountDepartmentDto.setName(costCollectionUnit.getCollectionAccountDepartmentName());
            accountDepartmentDto.setType(costCollectionUnit.getCollectionAccountDepartmentType());
            costCollectionUnitVo.setCollectionAccountDepartment(accountDepartmentDto);
            List<Long> accountUnitIds = costCollectionUnitMapper.getAccountUnitIds(costCollectionUnit.getId());
            List<AccountUnitIdAndNameDto> costAccountUnits = new ArrayList<>();
            for (Long accountUnitId : accountUnitIds) {
                String byName = costAccountUnitMapper.getByName(accountUnitId);
                AccountUnitIdAndNameDto accountUnitIdAndNameDto = new AccountUnitIdAndNameDto();
                accountUnitIdAndNameDto.setAccountUnitId(accountUnitId);
                accountUnitIdAndNameDto.setAccountUnitName(byName);
                costAccountUnits.add(accountUnitIdAndNameDto);
            }
            costCollectionUnitVo.setAccountUnitIds(costAccountUnits);
            costCollectionUnitVos.add(costCollectionUnitVo);
        }
        collectionUnitPage.setRecords(costCollectionUnitVos);
        collectionUnitPage.setTotal(costCollectionUnits.size());
        return collectionUnitPage;
    }

    /**
     * 删除归集单元
     * @param id
     */
    @Override
    public void deleteCollectionUnitById(Long id) {
        //删除中间表数据
        costCollectionUnitMapper.removeAccountUnitIds(id);
        this.removeById(id);
    }

    /**
     * 修改归集单元状态
     * @param dto
     * @return
     */
    @Override
    public Boolean updateStatusCollectionUnit(CostCollectionUnitDto dto) {
        //根据归集单元id获取归集单元对象
        CostCollectionUnit costCollectionUnit = getById(dto.getId());
        if (costCollectionUnit == null) {
            throw new BizException("归集单元不存在");
        }
        costCollectionUnit.setStatus(dto.getStatus());
        return updateById(costCollectionUnit);
    }
}
