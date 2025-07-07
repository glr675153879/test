package com.hscloud.hs.cost.account.controller.report;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hscloud.hs.cost.account.model.entity.base.BaseEntity;
import com.hscloud.hs.cost.account.model.entity.base.PageRequest;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Arrays;
import java.util.List;

/**
 * @author : [pc]
 * @version : [v1.0]
 * @createTime : [2024/4/29 8:53]
 */
@RequiredArgsConstructor
public abstract class BaseController<S extends IService<M>, M extends BaseEntity<M>> {

    private final S service;

    @SysLog("报表设计表info")
    @GetMapping("/info/{id}")
    @Operation(summary = "报表设计表info")
    public R<M> info(@PathVariable Long id) {
        return R.ok(service.getById(id));
    }

    @SysLog("报表设计表page")
    @GetMapping("/page")
    @Operation(summary = "报表设计表page")
    public R<IPage<M>> page(PageRequest<M> pr) {
        return R.ok(service.page(pr.getPage(), pr.getWrapper()));
    }

    @SysLog("报表设计表list")
    @GetMapping("/list")
    @Operation(summary = "报表设计表list")
    public R<List<M>> list(PageRequest<M> pr) {
        return R.ok(service.list(pr.getWrapper()));
    }

    @SysLog("报表设计表add")
    @PostMapping("/add")
    @Operation(summary = "报表设计表add")
    public R add(@RequestBody M M) {
        return R.ok(service.save(M));
    }

    @SysLog("报表设计表edit")
    @PostMapping("/edit")
    @Operation(summary = "报表设计表edit")
    public R edit(@RequestBody M m) {
        return R.ok(service.updateById(m));
    }

    @SysLog("报表设计表del")
    @PostMapping("/del/{id}")
    @Operation(summary = "报表设计表del")
    public R del(@PathVariable Long id) {
        return R.ok(service.removeById(id));
    }

    @SysLog("报表设计表delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "报表设计表delBatch 1,2,3")
    public R delBatch(@PathVariable String ids) {
        return R.ok(service.removeBatchByIds(Arrays.asList(ids.split(","))));
    }

}
