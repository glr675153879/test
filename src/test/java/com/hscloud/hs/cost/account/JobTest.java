package com.hscloud.hs.cost.account;

import com.hscloud.hs.cost.account.service.impl.CostAccountItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class JobTest {



    @Autowired
    private CostAccountItemServiceImpl costAccountItemService;



    @Test
    public void test() {
        costAccountItemService.saveItemResultDetail();
    }




}
