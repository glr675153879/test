package com.hscloud.hs.cost.account.controller.second;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.constant.enums.second.ModeType;
import com.hscloud.hs.cost.account.model.dto.second.ProgDetailItemSave1DTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgDetailItemSave2DTO;
import com.hscloud.hs.cost.account.model.dto.second.ProgItemDelDTO;
import com.hscloud.hs.cost.account.model.dto.second.UnitTaskDetailItemSaveDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskDetailItem;
import com.hscloud.hs.cost.account.model.entity.second.UnitTaskProjectDetail;
import com.hscloud.hs.cost.account.model.vo.second.UnitTaskDetailItemVo;
import com.hscloud.hs.cost.account.model.vo.second.importXls.ImportResultVo;
import com.hscloud.hs.cost.account.service.second.IUnitTaskDetailItemService;
import com.hscloud.hs.cost.account.service.second.IUnitTaskProjectDetailService;
import com.hscloud.hs.cost.account.utils.CommonUtils;
import com.hscloud.hs.cost.account.utils.ExcelUtil;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
* 任务科室二次分配明细大项值
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/second/unitTaskDetailItem")
@Tag(name = "unitTaskDetailItem", description = "任务科室二次分配明细大项值")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
public class UnitTaskDetailItemController {

    private final IUnitTaskDetailItemService unitTaskDetailItemService;
    private final IUnitTaskProjectDetailService unitTaskProjectDetailService;

    @SysLog("任务科室二次分配明细大项值info")
    @GetMapping("/info/{id}")
    @Operation(summary = "任务科室二次分配明细大项值info")
    public R<UnitTaskDetailItem> info(@PathVariable Long id) {
        return R.ok(unitTaskDetailItemService.getById(id));
    }

    @SysLog("任务科室二次分配明细大项值page")
    @GetMapping("/page")
    @Operation(summary = "任务科室二次分配明细大项值page")
    public R<IPage<UnitTaskDetailItem>> page(PageRequest<UnitTaskDetailItem> pr) {
        return R.ok(unitTaskDetailItemService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("任务科室二次分配明细大项值list")
    @GetMapping("/list")
    @Operation(summary = "任务科室二次分配明细大项值list")
    public R<List<UnitTaskDetailItem>> list(PageRequest<UnitTaskDetailItem> pr) {
        return R.ok(unitTaskDetailItemService.list(pr.getWrapper()));
    }

    @SysLog("任务科室二次分配明细大项值userList")
    @GetMapping("/userList")
    @Operation(summary = "任务科室二次分配明细大项值userList")
    public R<List<UnitTaskDetailItemVo>> userList(Long unitTaskProjectDetailId) {
        return R.ok(unitTaskDetailItemService.userList(unitTaskProjectDetailId));
    }

    @SysLog("任务科室二次分配明细大项值add")
    @PostMapping("/add")
    @Operation(summary = "任务科室二次分配明细大项值add")
    public R add(@RequestBody UnitTaskDetailItem unitTaskDetailItem)  {
        return R.ok(unitTaskDetailItemService.save(unitTaskDetailItem));
    }

    @SysLog("任务科室二次分配明细大项值save")
    @PostMapping("/save")
    @Operation(summary = "任务科室二次分配明细大项值save")
    public R save(@RequestBody UnitTaskDetailItemSaveDTO unitTaskDetailItemSaveDTO)  {
        unitTaskDetailItemService.saveItems(unitTaskDetailItemSaveDTO);
        return R.ok();
    }

    @SysLog("方案系数分配保存 saveProgDetailItem1")
    @PostMapping("/saveProgDetailItem1")
    @Operation(summary = "方案系数分配保存 saveProgDetailItem1")
    public R saveProgDetailItem1(@RequestBody ProgDetailItemSave1DTO progDetailItemSave1DTO)  {
        unitTaskDetailItemService.saveProgDetailItem1(progDetailItemSave1DTO);
        return R.ok();
    }

    @SysLog("方案工作量保存 saveProgDetailItem2")
    @PostMapping("/saveProgDetailItem2")
    @Operation(summary = "方案工作量保存 saveProgDetailItem2")
    public R saveProgDetailItem2(@RequestBody ProgDetailItemSave2DTO progDetailItemSave2DTO)  {
        unitTaskDetailItemService.saveProgDetailItem2(progDetailItemSave2DTO);
        return R.ok();
    }

    @SysLog("方案工作量排序 updateProgDetailItem2")
    @PostMapping("/updateProgDetailItem2Index")
    @Operation(summary = "方案工作量排序 updateProgDetailItem2")
    public R updateProgDetailItem2Index(@RequestBody ProgDetailItemSave2DTO progDetailItemSave2DTO) {
        unitTaskDetailItemService.updateProgDetailItem2Index(progDetailItemSave2DTO);
        return R.ok();
    }

    @SysLog("任务科室二次分配明细大项值edit")
    @PostMapping("/edit")
    @Operation(summary = "任务科室二次分配明细大项值edit")
    public R edit(@RequestBody UnitTaskDetailItem unitTaskDetailItem)  {
        return R.ok(unitTaskDetailItemService.updateById(unitTaskDetailItem));
    }

    @SysLog("任务科室二次分配明细大项值delete")
    @PostMapping("/delete")
    @Operation(summary = "任务科室二次分配明细大项值delete")
    public R delete(@RequestBody ProgItemDelDTO progItemDelDTO)  {
        unitTaskDetailItemService.deleteById(progItemDelDTO);
        return R.ok();
    }

    @SysLog("任务科室二次分配明细大项值delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "任务科室二次分配明细大项值delBatch 1,2,3")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(unitTaskDetailItemService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @GetMapping("/exportErci")
    @Operation(summary = "科室二次导出")
    public void exportErci(Long unitTaskProjectDetailId, HttpServletResponse response) {
        unitTaskDetailItemService.exportErci(unitTaskProjectDetailId,response);
    }

    @PostMapping("/importErci/{unitTaskProjectDetailId}")
    @Operation(summary = "科室二次导入")
    public R<ImportResultVo> importErci(@PathVariable Long unitTaskProjectDetailId, @RequestPart("file") MultipartFile file) throws Exception {
        String[][] xlsDataArr = ExcelUtil.doExcelH(file,2);
        UnitTaskProjectDetail detail = unitTaskProjectDetailService.getById(unitTaskProjectDetailId);
        String modeType = CommonUtils.getDicVal(detail.getModeType());
        if(ModeType.ratio.toString().equals(modeType)){
            return R.ok(unitTaskDetailItemService.importErciXishu(unitTaskProjectDetailId,xlsDataArr));
        }else{
            return R.ok(unitTaskDetailItemService.importErciWork(unitTaskProjectDetailId,xlsDataArr));
        }
    }

}