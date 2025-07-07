package com.hscloud.hs.cost.account.service.impl.userAttendance;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.userAttendance.FirstDistributionAccountFormulaParamMapper;
import com.hscloud.hs.cost.account.model.dto.userAttendance.FirstDistributionAccountFormulaParamDto;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.userAttendance.CostUserAttendanceCustomFields;
import com.hscloud.hs.cost.account.model.entity.userAttendance.FirstDistributionAccountFormulaParam;
import com.hscloud.hs.cost.account.service.userAttendance.IFirstDistributionAccountFormulaParamService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 一次分配考勤公式参数 服务实现类
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FirstDistributionAccountFormulaParamService extends ServiceImpl<FirstDistributionAccountFormulaParamMapper, FirstDistributionAccountFormulaParam> implements IFirstDistributionAccountFormulaParamService {

    private final CostUserAttendanceCustomFieldsService costUserAttendanceCustomFieldsService;

    @Override
    public List<FirstDistributionAccountFormulaParamDto> listParam(PageRequest<FirstDistributionAccountFormulaParam> pr) {
        List<FirstDistributionAccountFormulaParamDto> combinedList = new ArrayList<>();
        List<FirstDistributionAccountFormulaParam> originList = this.list(pr.getWrapper());
        LambdaQueryWrapper<CostUserAttendanceCustomFields> qr = new LambdaQueryWrapper<>();
        Object dt = pr.getQ().get("dt");
        List<CostUserAttendanceCustomFields> extraList = costUserAttendanceCustomFieldsService.list(
                qr.eq(CostUserAttendanceCustomFields::getStatus, 1).eq(Objects.nonNull(dt), CostUserAttendanceCustomFields::getDt, dt));
        FirstDistributionAccountFormulaParamDto fixedDto = new FirstDistributionAccountFormulaParamDto();
        fixedDto.setName("考勤组天数");
        fixedDto.setCode("KQZTS");
        combinedList.add(fixedDto);
        // 将参数转换为dto
        for (FirstDistributionAccountFormulaParam origin : originList) {
            FirstDistributionAccountFormulaParamDto dto = new FirstDistributionAccountFormulaParamDto();
            dto.setName(origin.getParamName());
            dto.setCode(origin.getParamKey());
            combinedList.add(dto);
        }
        // 将自定义字段转换为dto
        for (CostUserAttendanceCustomFields extra : extraList) {
            FirstDistributionAccountFormulaParamDto dto = new FirstDistributionAccountFormulaParamDto();
            BeanUtils.copyProperties(extra, dto);
            combinedList.add(dto);
        }
        return combinedList;
    }
}
