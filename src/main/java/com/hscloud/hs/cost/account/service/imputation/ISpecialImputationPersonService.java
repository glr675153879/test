package com.hscloud.hs.cost.account.service.imputation;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.imputation.NonAndSpecialDelDTO;
import com.hscloud.hs.cost.account.model.entity.imputation.SpecialImputationPerson;
import com.hscloud.hs.cost.account.model.entity.second.Attendance;

import java.util.List;

/**
 * 特殊归集人员 服务接口类
 */
public interface ISpecialImputationPersonService extends IService<SpecialImputationPerson> {

    List<Attendance> listByPId(Long imputationId, Long accountUnitId, Long indexId, List<SpecialImputationPerson> specialImputationPersonAllList);

    boolean saveOrEditSpecialImputationPerson(SpecialImputationPerson specialImputationPerson);

    IPage<SpecialImputationPerson> pageSpecialImputationPerson(Page<SpecialImputationPerson> page, QueryWrapper<SpecialImputationPerson> wrapper, Long imputationId);

    boolean removeSpecialImputationPerson(NonAndSpecialDelDTO nonAndSpecialDelDTO);

    List<Attendance> listAllByPId(Long imputationId, Long indexId);
}
