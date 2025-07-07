package com.hscloud.hs.cost.account.service;


import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionUserAttendanceDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskIndividualPost;
import com.hscloud.hs.cost.account.model.vo.UserAttendanceVo;

import java.util.List;

/**
 * <p>
 * 二次分配任务个人岗位绩效表 服务类
 * </p>
 *
 * @author 
 * @since 2023-11-17
 */
public interface ISecondDistributionTaskIndividualPostService extends IService<SecondDistributionTaskIndividualPost> {


    List<UserAttendanceVo> getUserAttendanceList(SecondDistributionUserAttendanceDto secondDistributionTaskIndividualPost);
}
