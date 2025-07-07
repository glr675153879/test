package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.imputation.ProjectCoefficientForbiddenDTO;
import com.hscloud.hs.cost.account.model.entity.imputation.ProjectCoefficient;

/**
 * 项目系数 服务接口类
 */
public interface IProjectCoefficientService extends IService<ProjectCoefficient> {

    int forbidden(ProjectCoefficientForbiddenDTO projectCoefficientForbiddenDTO);

}
