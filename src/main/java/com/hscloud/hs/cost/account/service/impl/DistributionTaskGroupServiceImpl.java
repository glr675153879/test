package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountIndexMapper;
import com.hscloud.hs.cost.account.mapper.DistributionTaskGroupMapper;
import com.hscloud.hs.cost.account.model.dto.DistributionTaskGroupQueryDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountIndex;
import com.hscloud.hs.cost.account.model.entity.DistributionTaskGroup;
import com.hscloud.hs.cost.account.model.vo.TaskResultVo;
import com.hscloud.hs.cost.account.service.IDistributionTaskGroupService;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.security.service.PigxUser;
import com.pig4cloud.pigx.common.security.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 任务分组 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-11-20
 */
@Service
@RequiredArgsConstructor
public class DistributionTaskGroupServiceImpl extends ServiceImpl<DistributionTaskGroupMapper, DistributionTaskGroup> implements IDistributionTaskGroupService {

    private final DistributionTaskGroupMapper taskGroupMapper;
    private final CostAccountIndexMapper indexMapper;
    private final LocalCacheUtils cacheUtils;

    /**
     * 启停用任务分组
     *
     * @param id
     * @param status
     * @return
     */
    @Override
    public Boolean enableTaskGroup(long id, String status) {
        //根据id查询对应的任务分组
        DistributionTaskGroup distributionTaskGroup = new DistributionTaskGroup().selectById(id);
        distributionTaskGroup.setStatus(status);
//        //修改关联的指标状态
//        CostAccountIndex costAccountIndex=new CostAccountIndex().selectById(distributionTaskGroup.getIndexId());
//        costAccountIndex.setStatus(status);
//        indexMapper.updateById(costAccountIndex);
        return updateById(distributionTaskGroup);
    }

    /**
     * *分页模糊匹配
     *
     * @param dto
     * @return
     */
    @Override
    public IPage<DistributionTaskGroup> listTaskGroup(DistributionTaskGroupQueryDto dto) {
        Page page = new Page<>(dto.getCurrent(), dto.getSize());
        final IPage<DistributionTaskGroup> distributionTaskGroupIPage = taskGroupMapper.listByQueryDto(dto, page);
        distributionTaskGroupIPage.setTotal(page.getTotal());
        return distributionTaskGroupIPage;
    }

    /**
     * 新增
     *
     * @param distributionTaskGroup
     */
    @Override
    @Transactional
    public void saveTaskGroup(DistributionTaskGroup distributionTaskGroup) {
        distributionTaskGroup.setCreateTime(LocalDateTime.now());
        final PigxUser user = SecurityUtils.getUser();
        distributionTaskGroup.setCreateUserId(user.getId());
        distributionTaskGroup.setCreateUserName(user.getName());

        //插入指标表中
        CostAccountIndex costAccountIndex = new CostAccountIndex();
        costAccountIndex.setStatus("1");
        costAccountIndex.setName(distributionTaskGroup.getIndexName());
        costAccountIndex.setCreateTime(LocalDateTime.now());
        costAccountIndex.setIndexGroupId(1727494440030859265L);
        costAccountIndex.setIsSystemIndex("1");
        costAccountIndex.setCreateBy(user.getId() + "");
        costAccountIndex.setUpdateBy(user.getId() + "");
        costAccountIndex.setUpdateTime(LocalDateTime.now());
        costAccountIndex.insert();
        cacheUtils.setIndexMap(costAccountIndex);
        distributionTaskGroup.setIndexId(costAccountIndex.getId());
        distributionTaskGroup.insert();
    }

    /**
     * 查询所有核算任务分组名称
     * @return
     */
    @Override
    public List<TaskResultVo> getTaskGroupNames() {
        List<DistributionTaskGroup> taskGroups = taskGroupMapper.selectList(null);
        List<TaskResultVo> vos = taskGroups.stream().map(tg -> {
            TaskResultVo taskResultVo = new TaskResultVo();
            taskResultVo.setGroupName(tg.getName());
            taskResultVo.setGroupType(tg.getType());
            taskResultVo.setAccountObject(tg.getAccountObject());
            return taskResultVo;
        }).collect(Collectors.toList());
        return vos;
    }

    /**
     * 修改任务分组
     * @param distributionTaskGroup
     * @return
     */
    @Override
    @Transactional
    public R updateTaskGroup(DistributionTaskGroup distributionTaskGroup) {
        final PigxUser user = SecurityUtils.getUser();
        CostAccountIndex index=new CostAccountIndex().selectById(distributionTaskGroup.getIndexId());
        if (index==null){
            CostAccountIndex costAccountIndex=new CostAccountIndex();
            costAccountIndex.setStatus("1");
            costAccountIndex.setName(distributionTaskGroup.getIndexName());
            costAccountIndex.setCreateTime(LocalDateTime.now());
            costAccountIndex.setIndexGroupId(1727494440030859265L);
            costAccountIndex.setIsSystemIndex("1");
            costAccountIndex.setCreateBy(user.getId() + "");
            costAccountIndex.setUpdateBy(user.getId() + "");
            costAccountIndex.setUpdateTime(LocalDateTime.now());
            costAccountIndex.insert();
            cacheUtils.setIndexMap(costAccountIndex);
            distributionTaskGroup.setIndexId(costAccountIndex.getId());
        }else {
            index.setName(distributionTaskGroup.getIndexName());
            index.updateById();
        }
        return R.ok(distributionTaskGroup.updateById());
    }

}
