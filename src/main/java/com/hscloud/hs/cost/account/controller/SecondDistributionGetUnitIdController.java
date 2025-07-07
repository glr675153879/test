package com.hscloud.hs.cost.account.controller;


import com.hscloud.hs.cost.account.service.ISecondDistributionGetUnitIdService;
import com.pig4cloud.pigx.common.core.util.R;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/second/getUnit")
@RequiredArgsConstructor
public class SecondDistributionGetUnitIdController {

    private final ISecondDistributionGetUnitIdService getUnitIdService;
    @GetMapping
    public R getUnitIdByUserId() {

        return R.ok( getUnitIdService.getUnitByUserId());

    }
}
