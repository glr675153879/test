package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionIndividualPostDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionTaskIndividualPostDto;
import com.hscloud.hs.cost.account.model.dto.SecondDistributionUserAttendanceDto;
import com.hscloud.hs.cost.account.model.entity.SecondDistributionTaskIndividualPost;
import com.hscloud.hs.cost.account.service.ISecondDistributionTaskIndividualPostService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <p>
 * 二次分配任务个人岗位绩效表 前端控制器
 * </p>
 *
 * @author
 * @since 2023-11-17
 */
@RestController
@RequestMapping("/second/distribution/task/individual/post")
public class SecondDistributionTaskIndividualPostController {


    @Autowired
    private ISecondDistributionTaskIndividualPostService secondDistributionTaskIndividualPostService;


    @PostMapping
    @Operation(summary = "新增个人岗位绩效")
    public R saveIndividualPost(@RequestBody @Validated SecondDistributionIndividualPostDto secondDistributionTaskIndividualPostDto) {
        //先删除原先的数据
        List<SecondDistributionTaskIndividualPost> taskIndividualPostDtoList = secondDistributionTaskIndividualPostDto.getTaskIndividualPostDtoList();
        secondDistributionTaskIndividualPostService.remove(Wrappers.<SecondDistributionTaskIndividualPost>query()
                .lambda()
                .eq(SecondDistributionTaskIndividualPost::getTaskUnitRelateId, secondDistributionTaskIndividualPostDto.getTaskUnitRelateId())
                .eq(SecondDistributionTaskIndividualPost::getPlanId, secondDistributionTaskIndividualPostDto.getPlanId())
                .eq(SecondDistributionTaskIndividualPost::getIndexId, secondDistributionTaskIndividualPostDto.getIndexId())
        );
        if (taskIndividualPostDtoList.isEmpty()) {
            return R.ok();
        }
        return R.ok(secondDistributionTaskIndividualPostService.saveBatch(taskIndividualPostDtoList));
    }


    @GetMapping
    @Operation(summary = "查询个人岗位绩效")
    public R getIndividualPost(SecondDistributionTaskIndividualPost secondDistributionTaskIndividualPost) {
        return R.ok(secondDistributionTaskIndividualPostService.list(Wrappers.<SecondDistributionTaskIndividualPost>query()
                .lambda()
                .eq(Objects.nonNull(secondDistributionTaskIndividualPost.getPlanId()), SecondDistributionTaskIndividualPost::getPlanId, secondDistributionTaskIndividualPost.getPlanId())
                .eq(Objects.nonNull(secondDistributionTaskIndividualPost.getIndexId()), SecondDistributionTaskIndividualPost::getIndexId, secondDistributionTaskIndividualPost.getIndexId())
                .eq(Objects.nonNull(secondDistributionTaskIndividualPost.getTaskUnitRelateId()), SecondDistributionTaskIndividualPost::getTaskUnitRelateId, secondDistributionTaskIndividualPost.getTaskUnitRelateId())
                .eq(Objects.nonNull(secondDistributionTaskIndividualPost.getJobNumber()), SecondDistributionTaskIndividualPost::getJobNumber, secondDistributionTaskIndividualPost.getJobNumber())
                .eq(StrUtil.isNotBlank(secondDistributionTaskIndividualPost.getEducation()), SecondDistributionTaskIndividualPost::getEducation, secondDistributionTaskIndividualPost.getEducation())
                .eq(StrUtil.isNotBlank(secondDistributionTaskIndividualPost.getTitleLevel()), SecondDistributionTaskIndividualPost::getTitleLevel, secondDistributionTaskIndividualPost.getTitleLevel())
                .like(StrUtil.isNotBlank(secondDistributionTaskIndividualPost.getName()), SecondDistributionTaskIndividualPost::getName, secondDistributionTaskIndividualPost.getName())));
    }


    @PostMapping("/userAttendanceList")
    @Operation(summary = "查询人员出勤率")
    public R getUserAttendanceList(@RequestBody @Validated SecondDistributionUserAttendanceDto secondDistributionTaskIndividualPost) {
        return R.ok(secondDistributionTaskIndividualPostService.getUserAttendanceList(secondDistributionTaskIndividualPost));
    }


}
