package com.hscloud.hs.cost.account.controller.second;

import cn.hutool.core.comparator.CompareUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.SecondDistributionTaskStatusEnum;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.ProgDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.UnitTask;
import com.hscloud.hs.cost.account.service.second.IProgDetailItemService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailItemService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
* 方案科室二次分配明细大项
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/progDetailItem")
@Tag(name = "progDetailItem", description = "方案科室二次分配明细大项")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class ProgDetailItemController {

    private final IProgDetailItemService progDetailItemService;
    private final IUnitTaskService unitTaskService;
    private final IUnitTaskDetailItemService unitTaskDetailItemService;

    @SysLog("方案科室二次分配明细大项info")
    @GetMapping("/info/{id}")
    @Operation(summary = "方案科室二次分配明细大项info")
    public R<ProgDetailItem> info(@PathVariable Long id) {
        return R.ok(progDetailItemService.getById(id));
    }

    @SysLog("方案科室二次分配明细大项page")
    @GetMapping("/page")
    @Operation(summary = "方案科室二次分配明细大项page")
    public R<IPage<ProgDetailItem>> page(PageRequest<ProgDetailItem> pr) {
        return R.ok(progDetailItemService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("方案科室二次分配明细大项list")
    @GetMapping("/list")
    @Operation(summary = "方案科室二次分配明细大项list")
    public R<List<ProgDetailItem>> list(Long unitTaskId,Long unitDetailId,PageRequest<ProgDetailItem> pr) {
        if (unitTaskId != null && unitDetailId!= null){//已完成的 从任务中查
            UnitTask unitTask = unitTaskService.getById(unitTaskId);
            if (unitTask != null && Objects.equals(unitTask.getStatus(), SecondDistributionTaskStatusEnum.APPROVAL_APPROVED.getCode())){
                return R.ok(unitTaskDetailItemService.getProgItemList(unitTaskId,unitDetailId));
            }

        }

        List<ProgDetailItem> list = progDetailItemService.list(pr.getWrapper());
        list.sort((o1, o2) -> {
            int compare = CompareUtil.compare(o1.getParentId(), o2.getParentId(), false);
            if (compare != 0) {
                return compare;
            }
            int compare1 = CompareUtil.compare(o1.getSortNum(), o2.getSortNum());
            if (compare1 != 0) {
                return compare1;
            }
            return CompareUtil.compare(o1.getId(), o2.getId());
        });
        return R.ok(list);
    }

    public static void main(String[] args) {
        List<Integer> a = new ArrayList<>();
        a.add(3);
        a.add(7);
        a.add(11);
        a.add(null);
        a.add(1);
        a.sort((o1, o2) -> CompareUtil.compare(o1, o2, true));
        System.out.println(JSON.toJSONString(a));
    }

    @SysLog("方案科室二次分配明细大项add")
    @PostMapping("/add")
    @Operation(summary = "方案科室二次分配明细大项add")
    public R add(@RequestBody ProgDetailItem progDetailItem)  {
        return R.ok(progDetailItemService.save(progDetailItem));
    }

    @SysLog("方案科室二次分配明细大项edit")
    @PostMapping("/edit")
    @Operation(summary = "方案科室二次分配明细大项edit")
    public R edit(@RequestBody ProgDetailItem progDetailItem)  {
        return R.ok(progDetailItemService.updateById(progDetailItem));
    }

    @SysLog("方案科室二次分配明细大项del")
    @PostMapping("/del/{id}")
    @Operation(summary = "方案科室二次分配明细大项del")
    public R del(@PathVariable Long id)  {
        return R.ok(progDetailItemService.removeById(id));
    }

    @SysLog("方案科室二次分配明细大项delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "方案科室二次分配明细大项delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(progDetailItemService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}