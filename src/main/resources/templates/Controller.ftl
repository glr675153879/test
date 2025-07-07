<#assign entityCodeBig = entityCode?cap_first>
package ${comPathDot}.controller${childSufDot};

import com.baomidou.mybatisplus.core.metadata.IPage;
import ${comPathDot}.model.entity.base.PageRequest;
import ${comPathDot}.model.entity${childSufDot}.${entityCodeBig};
import ${comPathDot}.service${childSufDot}.I${entityCodeBig}Service;
import com.pig4cloud.pigx.common.core.util.R;
import com.pig4cloud.pigx.common.log.annotation.SysLog;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import java.util.Arrays;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;

/**
* ${entityName}
*
*/
@RestController
@RequiredArgsConstructor
@RequestMapping("${childSufPath}/${entityCode}")
@Tag(name = "${entityCode}", description = "${entityName}")
@SecurityRequirement(name = HttpHeaders.AUTHORIZATION)
@Slf4j
public class ${entityCodeBig}Controller {

    private final I${entityCodeBig}Service ${entityCode}Service;

    @SysLog("${entityName}info")
    @GetMapping("/info/{id}")
    @Operation(summary = "${entityName}info")
    @PreAuthorize("@pms.hasPermission('${entityCode}_info')")
    public R<${entityCodeBig}> info(@PathVariable Long id) {
        return R.ok(${entityCode}Service.getById(id));
    }

    @SysLog("${entityName}page")
    @GetMapping("/page")
    @Operation(summary = "${entityName}page")
    @PreAuthorize("@pms.hasPermission('${entityCode}_page')")
    public R<IPage<${entityCodeBig}>> page(PageRequest<${entityCodeBig}> pr) {
        return R.ok(${entityCode}Service.page(pr.getPage(),pr.getWrapper()));
    }

    @SysLog("${entityName}list")
    @GetMapping("/list")
    @Operation(summary = "${entityName}list")
    @PreAuthorize("@pms.hasPermission('${entityCode}_list')")
    public R<List<${entityCodeBig}>> list(PageRequest<${entityCodeBig}> pr) {
        return R.ok(${entityCode}Service.list(pr.getWrapper()));
    }

    @SysLog("${entityName}add")
    @PostMapping("/add")
    @Operation(summary = "${entityName}add")
    @PreAuthorize("@pms.hasPermission('${entityCode}_add')")
    public R add(@RequestBody ${entityCodeBig} ${entityCode})  {
        return R.ok(${entityCode}Service.save(${entityCode}));
    }

    @SysLog("${entityName}edit")
    @PostMapping("/edit")
    @Operation(summary = "${entityName}edit")
    @PreAuthorize("@pms.hasPermission('${entityCode}_edit')")
    public R edit(@RequestBody ${entityCodeBig} ${entityCode})  {
        return R.ok(${entityCode}Service.updateById(${entityCode}));
    }

    @SysLog("${entityName}del")
    @PostMapping("/del/{id}")
    @Operation(summary = "${entityName}del")
    @PreAuthorize("@pms.hasPermission('${entityCode}_del')")
    public R del(@PathVariable Long id)  {
        return R.ok(${entityCode}Service.removeById(id));
    }

    @SysLog("${entityName}delBatch")
    @PostMapping("/delBatch/{ids}")
    @Operation(summary = "${entityName}delBatch 1,2,3")
    @PreAuthorize("@pms.hasPermission('${entityCode}_delBatch')")
    public R delBatch(@PathVariable String ids)  {
        return R.ok(${entityCode}Service.removeBatchByIds(Arrays.asList(ids.split(","))));
    }
}