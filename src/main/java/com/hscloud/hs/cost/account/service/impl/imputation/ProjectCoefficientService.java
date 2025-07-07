package com.hscloud.hs.cost.account.service.impl.imputation;

import com.hscloud.hs.cost.account.model.dto.imputation.ProjectCoefficientForbiddenDTO;
import com.pig4cloud.pigx.common.core.exception.BizException;
import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.imputation.ProjectCoefficientMapper;
import com.hscloud.hs.cost.account.model.entity.imputation.ProjectCoefficient;
import com.hscloud.hs.cost.account.service.imputation.IProjectCoefficientService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 项目系数 服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectCoefficientService extends ServiceImpl<ProjectCoefficientMapper, ProjectCoefficient> implements IProjectCoefficientService {


    @Transactional(rollbackFor = Exception.class)
    @Override
    public int forbidden(ProjectCoefficientForbiddenDTO projectCoefficientForbiddenDTO) {
        ProjectCoefficient projectCoefficient = this.baseMapper.selectById(projectCoefficientForbiddenDTO.getId());
        if (Objects.isNull(projectCoefficient)) {
            log.error("id为{}的数据不存在", projectCoefficientForbiddenDTO.getId());
            throw new BizException("此条数据不存在");
        }
        projectCoefficient.setStatus(projectCoefficientForbiddenDTO.getStatus());
        return this.baseMapper.updateById(projectCoefficient);
    }

}
