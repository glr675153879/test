package com.hscloud.hs.cost.account.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountUnitQueryDtoNew;
import com.hscloud.hs.cost.account.model.entity.CostAccountUnit;
import com.hscloud.hs.cost.account.model.vo.CostAccountUnitExcelVO;
import com.pig4cloud.pigx.admin.api.entity.SysUser;
import com.pig4cloud.pigx.common.core.util.R;
import org.springframework.validation.BindingResult;

import java.util.List;


public interface CostAccountUnitService extends IService<CostAccountUnit> {
    IPage listUnit(CostAccountUnitQueryDtoNew input);

    Long saveUnit(CostAccountUnitDto costAccountUnitDto);

    Long updateUnit(CostAccountUnitDto costAccountUnitDto);

    void deleteUnitById(Long id);

    void switchUnit(CostAccountUnitDto costAccountUnitDto);

    /**
     * 导入科室单元
     * @param excelVOList
     * @param bindingResult
     * @return
     */
    R importUnit(List<CostAccountUnitExcelVO> excelVOList, BindingResult bindingResult);

    List<SysUser> listUser(Long unitId);

}
