package com.hscloud.hs.cost.account.controller.kpi;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.excel.metadata.data.ReadCellData;
import com.alibaba.excel.util.ConverterUtils;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.hscloud.hs.cost.account.model.dto.ConfirmSignDTO;
import com.hscloud.hs.cost.account.model.dto.kpi.KpiSignDTO;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiSignLeft;
import com.hscloud.hs.cost.account.service.kpi.IKpiSignLeftService;
import com.pig4cloud.pigx.common.core.exception.BizException;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.*;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

/**
* 绩效签发 左侧固定
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("/kpi/kpiSignLeft")
@Tag(name = "kpiSignLeft", description = "绩效签发 左侧固定")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class KpiSignLeftController {

    private final IKpiSignLeftService kpiSignLeftService;

    @SysLog("绩效签发 左侧固定info")
    @GetMapping("/info/{id}")
    @Operation(summary = "绩效签发 左侧固定info")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_info')")
    public R<KpiSignLeft> info(@PathVariable Long id) {
        return R.ok(kpiSignLeftService.getById(id));
    }

    @SysLog("绩效签发 左侧固定page")
    @GetMapping("/page")
    @Operation(summary = "绩效签发 左侧固定page")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_page')")
    public R<IPage<KpiSignLeft>> page(PageRequest<KpiSignLeft> pr) {
        return R.ok(kpiSignLeftService.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("绩效签发 左侧固定list")
    @GetMapping("/list")
    @Operation(summary = "绩效签发 左侧固定list")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_list')")
    public R<List<KpiSignLeft>> list(PageRequest<KpiSignLeft> pr) {
        return R.ok(kpiSignLeftService.list(pr.getWrapper()));
    }

    @SysLog("绩效签发 左侧固定add")
    @PostMapping("/add")
    @Operation(summary = "绩效签发 左侧固定add")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_add')")
    public R add(@RequestBody KpiSignLeft kpiSignLeft)  {
        return R.ok(kpiSignLeftService.save(kpiSignLeft));
    }

    @SysLog("绩效签发 左侧固定edit")
    @PostMapping("/edit")
    @Operation(summary = "绩效签发 左侧固定edit")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_edit')")
    public R edit(@RequestBody KpiSignLeft kpiSignLeft)  {
        return R.ok(kpiSignLeftService.updateById(kpiSignLeft));
    }

    @SysLog("绩效签发 左侧固定del")
    @PostMapping("/del/{id}")
    @Operation(summary = "绩效签发 左侧固定del")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_del')")
    public R del(@PathVariable Long id)  {
        return R.ok(kpiSignLeftService.removeById(id));
    }

    @SysLog("绩效签发 左侧固定delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "绩效签发 左侧固定delBatch 1,2,3")
    //@PreAuthorize("@pms.hasPermission('kpiSignLeft_delBatch')")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(kpiSignLeftService.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

    @SysLog("绩效签发 列表")
    @GetMapping("/sign/list")
    @Operation(summary = "绩效签发 列表")
    public R<KpiSignDTO> signList(Long period) {
        return R.ok(kpiSignLeftService.signList(period));
    }

    @SysLog("确认签发")
    @PostMapping("/confirm/sign")
    @Operation(summary = "确认签发")
    public R confirmSign(@RequestBody ConfirmSignDTO dto) {
        kpiSignLeftService.confirmSign(dto);
        return R.ok();
    }

    @SysLog("取消签发 测试用！！！")
    @PostMapping("/confirm/unsign")
    @Operation(summary = "取消签发 测试用！！！ ")
    public R confirmUnsign(@RequestBody ConfirmSignDTO dto) {
        kpiSignLeftService.confirmUnsign(dto);
        return R.ok();
    }

    @SysLog("继承上月 入参周期继承上个周期 是否继承为是 数据来源为手工上报")
    @PostMapping("/extend")
    @Operation(summary = "继承上月 入参周期继承上个周期 是否继承为是 数据来源为手工上报")
    public R extend(@RequestBody ConfirmSignDTO dto) {
        kpiSignLeftService.extend(dto);
        return R.ok();
    }

    @SysLog("导入 overwriteFlag1覆盖导入(清空后插) 2增量导入")
    @Operation(summary = "导入 overwriteFlag1覆盖导入(清空后插) 2增量导入")
    @PostMapping("/import")
    public R importData(@RequestParam(value = "file") MultipartFile file,
                        @RequestParam(value ="period") Long period,
                        @RequestParam(value ="overwriteFlag") String overwriteFlag) throws IOException {
        List<Map<Integer,String>> list=new ArrayList<>();
        try {
            EasyExcel.read(file.getInputStream(), new AnalysisEventListener<Map<Integer,String>>() {
                @Override
                public void invoke(Map<Integer,String> data, AnalysisContext context) {
                    Map<Integer, String> in = new HashMap<>(data);
                    list.add(in);
                }
                @Override
                public void doAfterAllAnalysed(AnalysisContext context) {
                    // 解析完成后的操作
                }
                @Override
                public void invokeHead(Map<Integer, ReadCellData<?>> headMap, AnalysisContext context) {
                    Map<Integer, String> integerStringMap = ConverterUtils.convertToStringMap(headMap, context);
                    list.add(integerStringMap);
                }
            }).sheet(0).doRead();
            if (list.isEmpty()){
                throw new BizException("未识别到数据");
            }
            kpiSignLeftService.importData(period,overwriteFlag,list);
            System.out.println(1);
        }
        catch (BizException e){
            throw new BizException(e.getDefaultMessage());
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new BizException(e.getMessage());
        }finally {
            file.getInputStream().close();
        }
        return R.ok();
    }


}