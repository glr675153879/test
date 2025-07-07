package com.hscloud.hs.cost.account.controller;

import com.hscloud.hs.cost.account.constant.enums.GroupType;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemInitDto;
import com.hscloud.hs.cost.account.model.dto.CostAccountItemQueryDto;
import com.hscloud.hs.cost.account.model.dto.EnableDto;
import com.hscloud.hs.cost.account.model.entity.CostAccountItem;
import com.hscloud.hs.cost.account.model.entity.CostBaseGroup;
import com.hscloud.hs.cost.account.service.CostAccountItemService;
import com.hscloud.hs.cost.account.service.ICostBaseGroupService;
import com.hscloud.hs.cost.account.utils.LocalCacheUtils;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author Admin
 */
@RestController
@RequestMapping("/account/item")
@Tag(description = "account_item", name = "核算项管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class CostAccountItemController {

   
    private final CostAccountItemService costAccountItemService;
    private final ICostBaseGroupService costBaseGroupService;

    private final LocalCacheUtils cacheUtils;

    /**
     * 新增核算项
     */
    @PostMapping("/add")
    @PreAuthorize("@pms.hasPermission('kpi_accounting_item_add')")
    public R addItem(@RequestBody CostAccountItem costAccountItem) {
        String groupId = costAccountItem.getGroupId();
        CostBaseGroup group = costBaseGroupService.getById(groupId);
        if(group != null){
            costAccountItem.setTypeGroup(group.getTypeGroup());
        }
        costAccountItemService.save(costAccountItem);
        cacheUtils.setItemMap(costAccountItem);
        return R.ok();
    }


    /**
     * 删除核算项
     */
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("@pms.hasPermission('kpi_accounting_item_del')")
    public R deleteItem(@PathVariable Long id) {

        costAccountItemService.deleteById(id);

        return R.ok();
    }


    /**
     * 启停用核算项
     */

    @PutMapping("/enable")
    @PreAuthorize("@pms.hasPermission('kpi_accounting_item_enable')")
    public R enableItem(@RequestBody @Validated EnableDto enableDto) {

        return R.ok(costAccountItemService.enableItem(Long.parseLong(enableDto.getId()), enableDto.getStatus()));
    }

    /**
     * 修改核算项
     */
    @PutMapping("/update")
    @PreAuthorize("@pms.hasPermission('kpi_accounting_item_edit')")
    public R updateItem(@RequestBody CostAccountItem costAccountItem) {
        final boolean updateById = costAccountItemService.updateById(costAccountItem);
        CostAccountItem costAccountItemDB = costAccountItemService.getById(costAccountItem);
        String typeGroup = costAccountItemDB.getTypeGroup();
        if(typeGroup != null && typeGroup.equals(GroupType.secondItem.toString())){
            return R.ok(updateById);
        }

        //增量数据计算修改核算项
        costAccountItemService.getItemResult(costAccountItem);

        return R.ok(updateById);
    }

    /**
     * 按组查询核算项
     */
    @GetMapping("/list")
    public R listItem(@Validated CostAccountItemQueryDto costAccountItemQueryDto) {
        return R.ok(costAccountItemService.listItem(costAccountItemQueryDto));
    }

    /**
     * 初始化核算项
     * @param costAccountItemInitDto 核算项初始化参数
     */
    @PostMapping("/init")
    //todo 初始化权限
    public R initItem(@RequestBody @Validated CostAccountItemInitDto costAccountItemInitDto) {
        return R.ok(costAccountItemService.initItem(costAccountItemInitDto));
    }

}
