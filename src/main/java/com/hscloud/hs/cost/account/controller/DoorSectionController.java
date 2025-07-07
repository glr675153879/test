package com.hscloud.hs.cost.account.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hscloud.hs.cost.account.model.entity.DoorSectionEntity;
import com.hscloud.hs.cost.account.service.DoorSectionService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;



/**
 * 门诊收入
 *
 * @author zjd
 * @email zjd@gmail.com
 * @date 2023-09-07 15:01:08
 */
@RestController
@Tag(name = "门诊收入数据", description = "抬头：时间（月）、项目、院区、科室、科别、医生、大类、小类、数量、金额（元）")
@RequestMapping("account/doorsection")
public class DoorSectionController {
    @Autowired
    private DoorSectionService doorSectionService;


    /**
     * 列表
     */
    @GetMapping("/page")
    public R page(Page page, DoorSectionEntity entity){

        return R.ok(doorSectionService.getDoorSectionPage(page,entity));

    }

    /**
     * 保存
     */
    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('account_save')")
    public R save(@RequestBody DoorSectionEntity doorSection){
		doorSectionService.save(doorSection);

        return R.ok();
    }

    /**
     * 修改
     */
    @PutMapping("/update")
    @PreAuthorize("@pms.hasPermission('account_update')")
    public R update(@RequestBody DoorSectionEntity doorSection){
		doorSectionService.updateById(doorSection);

        return R.ok();
    }

    /**
     * 删除
     */
    @DeleteMapping("/delete")
    @PreAuthorize("@pms.hasPermission('account_delete')")
    public R delete(@RequestBody String[] dts){
		doorSectionService.removeByIds(Arrays.asList(dts));

        return R.ok();
    }

}
