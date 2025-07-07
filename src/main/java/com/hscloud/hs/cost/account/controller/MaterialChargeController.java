package com.hscloud.hs.cost.account.controller;


import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeQueryDto;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeStatusDto;
import com.hscloud.hs.cost.account.model.dto.MaterialChargeUpdateDto;
import com.hscloud.hs.cost.account.model.entity.MaterialCharge;
import com.hscloud.hs.cost.account.service.IMaterialChargeService;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

/**
 * 物资收费管理
 * @author  lian
 * @date  2024/6/2 14:59
 *
 */

@RestController
@RequestMapping("/materialCharge")
@Tag(description = "materialCharge", name = "物资收费管理")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@RequiredArgsConstructor
public class MaterialChargeController {

    private final IMaterialChargeService materialChargeService;

    /**
     * 编辑/匹配
     */
    @PostMapping("/update")
    public R updateItem(@RequestBody @Validated MaterialChargeUpdateDto updateDto) {
        MaterialCharge materialCharge = this.materialChargeService.getBaseMapper().selectById(updateDto.getId());
        if(Objects.nonNull(materialCharge)){
            materialCharge.setIsCharge(updateDto.getIsCharge());
            return R.ok(materialChargeService.updateById(materialCharge));
        }else{
            return R.ok();
        }
    }


    /**
     * 分页模糊匹配查询
     */
    @SysLog("物资收费管理page")
    @GetMapping("/page")
    @Operation(summary = "物资收费管理page")
    public R<IPage<MaterialCharge>> listItem(MaterialChargeQueryDto dto) {
        return R.ok(materialChargeService.pageList(dto));
    }

    /**
     * 同步数据数据小组数据
     * @author  lian
     * @date  2024/6/2 16:40
     *
     */
    @GetMapping("/syncData")
    @Operation(summary = "同步数据数据小组数据")
    public R syncData(MaterialChargeQueryDto dto) {
        return R.ok(materialChargeService.syncData(dto));
    }

    /**
     * 启用/停用
     * @author  lian
     * @date  2024/6/2 22:50
     *
     */

    @PostMapping("/switch")
    @Operation(summary = "启用/停用")
    public R switchStatus(@RequestBody @Validated MaterialChargeStatusDto dto) {
        materialChargeService.switchStatus(dto);
        return R.ok();
    }

}
