package com.hscloud.hs.cost.account.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hscloud.hs.cost.account.mapper.SecondDistributionTaskIndividualPostMapper;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionUserAttendanceDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskIndividualPost;
import com.hscloud.hs.cost.account.model.vo.UserAttendanceVo;
import com.hscloud.hs.cost.account.service.ISecondDistributionIndexCalcService;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskIndividualPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 二次分配任务个人岗位绩效表 服务实现类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
@Service
public class SecondDistributionTaskIndividualPostServiceImpl extends ServiceImpl<SecondDistributionTaskIndividualPostMapper, SecondDistributionTaskIndividualPost> implements ISecondDistributionTaskIndividualPostService {



    @Autowired
    private ISecondDistributionIndexCalcService secondDistributionIndexCalcService;

    @Override
    public List<UserAttendanceVo> getUserAttendanceList(SecondDistributionUserAttendanceDto secondDistributionUserAttendanceDto) {
        return secondDistributionIndexCalcService.calAttendance(secondDistributionUserAttendanceDto.getPeriod(), secondDistributionUserAttendanceDto.getUserIds());
    }
}
