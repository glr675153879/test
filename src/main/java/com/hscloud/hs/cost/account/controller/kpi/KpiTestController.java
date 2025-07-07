package com.hscloud.hs.cost.account.controller.kpi;


import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Expression;
import com.hscloud.hs.cost.account.constant.enums.kpi.CodePrefixEnum;
import com.hscloud.hs.cost.account.service.kpi.CommCodeService;
import com.pig4cloud.pigx.common.core.util.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/kpi/test")
@Tag(name = "kpi测试", description = "kpi测试")
public class KpiTestController {

    @Autowired
    private CommCodeService commCodeService;


    @GetMapping("/commCode")
    @Operation(summary = "commCode")
    public R getAccountIndexPage(String code) {
        return R.ok(commCodeService.commCode(CodePrefixEnum.INDEX));
    }

    @GetMapping("/avi")
    @Operation(summary = "avi")
    public R getAccountIndexPage() {
        Expression exp1 = AviatorEvaluator.compile("1/0");
        Map<String, Object> env2 = new HashMap<>();
        env2.put("a",-100);
        env2.put("b",10);
        Object result = exp1.execute(env2);
        return R.ok(result);
    }

}
