package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.CostAccountTaskNewMapper;
import com.hscloud.hs.cost.account.model.dto.CostAccountTaskQueryNewDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountTaskNew;
import com.hscloud.hs.cost.account.model.vo.CostAccountTaskVo;
import com.hscloud.hs.cost.account.service.ICostAccountTaskNewService;
import com.pig4cloud.pigx.admin.api.feign.RemoteUserService;
import com.pig4cloud.pigx.admin.api.vo.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 核算任务表(新) 服务实现类
 * </p>
 *
 * @author author
 * @since 2023-11-23
 */
@Service
@RequiredArgsConstructor
public class CostAccountTaskNewServiceImpl extends ServiceImpl<CostAccountTaskNewMapper, CostAccountTaskNew> implements ICostAccountTaskNewService {

    private final CostAccountTaskNewMapper taskNewMapper;

    private final RemoteUserService remoteUserService;

    /**
     * 分页查询
     *
     * @param
     * @return
     */
    @Override
    public IPage<CostAccountTaskNew> listAccountTaskNew(CostAccountTaskQueryNewDto queryDto) {

        Page objectPage = new Page<>(queryDto.getCurrent(), queryDto.getSize());
        IPage<CostAccountTaskNew> costAccountTaskPage = taskNewMapper.listByQueryDto(objectPage, queryDto);
        costAccountTaskPage.setTotal(objectPage.getTotal());
        return costAccountTaskPage;
    }

    /**
     * 设置创建人详细信息
     *
     * @param id
     * @param costAccountTaskVo
     * @return
     */
    private CostAccountTaskVo setUserDetail(Long id, CostAccountTaskVo costAccountTaskVo) {
        //查询信息
        UserVO userVO = remoteUserService.details(id).getData();
        costAccountTaskVo.setName(userVO.getName());
        costAccountTaskVo.setJobNumber(userVO.getJobNumber());
        return costAccountTaskVo;
    }
}
