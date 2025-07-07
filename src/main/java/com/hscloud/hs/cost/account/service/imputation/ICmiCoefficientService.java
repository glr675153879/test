package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.imputation.CmiCoefficient;

/**
 * CMI系数 服务接口类
 */
public interface ICmiCoefficientService extends IService<CmiCoefficient> {

    boolean saveOrUpdateCmi(CmiCoefficient cmiCoefficient);
}
