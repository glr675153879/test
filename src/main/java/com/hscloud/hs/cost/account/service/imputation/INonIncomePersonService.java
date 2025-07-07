package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.model.entity.imputation.NonIncomePerson;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;

import java.util.List;

/**
 * 不计收入人员 服务接口类
 */
public interface INonIncomePersonService extends IService<NonIncomePerson> {

    List<Attendance> listByPId(Long imputationId, Long indexId, List<NonIncomePerson> allList);

    boolean saveOrUpdateNonIncomePerson(NonIncomePerson nonIncomePerson);

    IPage<NonIncomePerson> pageNonIncomePerson(Page<NonIncomePerson> page, QueryWrapper<NonIncomePerson> wrapper, Long imputationId);

    boolean removeNonIncomePerson(NonAndSpecialDelDTO nonAndSpecialDelDTO);
}
