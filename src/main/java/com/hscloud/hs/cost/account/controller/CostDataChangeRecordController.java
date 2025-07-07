package com.hscloud.hs.cost.account.controller;


import cn.hutool.core.bean.BeanUtil;
import com.hscloud.hs.cost.account.model.dto.CostDataChangeRecordCountDto;
import com.hscloud.hs.cost.account.model.dto.CostDataChangeRecordDto;
import com.hscloud.hs.cost.account.model.entity.CostDataChangeRecord;
import com.hscloud.hs.cost.account.model.vo.CostDataChangeRecordVo;
import com.hscloud.hs.cost.account.service.ICostDataChangeRecordService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 数据变更记录 前端控制器
 * </p>
 *
 * @author
 * @since 2023-09-11
 */
@RestController
@RequestMapping("/changeRecord")
@Tag(name = "异动处理", description = "changeRecord")
public class CostDataChangeRecordController {
    @Autowired
    private ICostDataChangeRecordService costDataChangeRecordService;

    @GetMapping("/list")
    @Operation(summary = "查询异动列表")
    public R getChangeRecord(String bizCode) {
        return R.ok(costDataChangeRecordService.getChangeRecord(bizCode));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取异动记录")
    public R getById(@PathVariable Long id) {
        CostDataChangeRecord dataChangeRecord = costDataChangeRecordService.getById(id);
        CostDataChangeRecordVo costDataChangeRecordVo = BeanUtil.copyProperties(dataChangeRecord, CostDataChangeRecordVo.class);
        return R.ok(costDataChangeRecordVo);
    }


    @PostMapping("/getCount")
    @Operation(summary = "根据业务类型获取异动数量")
    public R getCount(@RequestBody CostDataChangeRecordCountDto dto) {
        return R.ok(costDataChangeRecordService.getCount(dto));
    }

    @PutMapping("/update")
    @Operation(summary = "更新异动记录")
    public R update(@RequestBody CostDataChangeRecordDto dto) {
        CostDataChangeRecord costDataChangeRecord = BeanUtil.copyProperties(dto, CostDataChangeRecord.class);
        costDataChangeRecord.setStatus("1");
        return R.ok(costDataChangeRecordService.updateById(costDataChangeRecord));
    }
}
