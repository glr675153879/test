package com.hscloud.hs.cost.account.config;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.googlecode.aviator.AviatorEvaluator;
import com.googlecode.aviator.Options;
import com.googlecode.aviator.lexer.token.OperatorType;
import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorDouble;
import com.googlecode.aviator.runtime.type.AviatorObject;
import com.hscloud.hs.cost.account.mapper.kpi.KpiAccountTaskChildMapper;
import com.hscloud.hs.cost.account.mapper.kpi.KpiConfigMapper;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTask;
import com.hscloud.hs.cost.account.model.entity.kpi.KpiAccountTaskChild;
import com.hscloud.hs.cost.account.utils.kpi.aviator.DIV;
import com.hscloud.hs.cost.account.utils.kpi.aviator.IF;
import com.hscloud.hs.cost.account.utils.kpi.aviator.INT;
import com.hscloud.hs.cost.account.utils.kpi.aviator.ROUND;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * @author banana
 * @create 2024-09-23 23:08
 */

@Configuration
public class MyBatisConfig {

    @Resource
    private MybatisPlusInterceptor mybatisPlusInterceptor;
    @Resource
    private KpiAccountTaskChildMapper kpiAccountTaskChildMapper;
    @Resource
    private KpiConfigMapper kpiConfigMapper;

    /*@Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() throws Exception {
        System.out.println(mybatisPlusInterceptor);
        mybatisPlusInterceptor.addInnerInterceptor(new MyCustomInterceptor());
        return mybatisPlusInterceptor;
    }*/

    /* @PostConstruct*/
    public void init() {
        // mybatisPlusInterceptor.addInnerInterceptor(new MyCustomInterceptor());
        List<InnerInterceptor> interceptors = mybatisPlusInterceptor.getInterceptors();
        List<InnerInterceptor> innerInterceptors = new ArrayList<>();
        innerInterceptors.add(new MyCustomInterceptor());
        for (InnerInterceptor interceptor : interceptors) {
            innerInterceptors.add(interceptor);
        }
        mybatisPlusInterceptor.setInterceptors(innerInterceptors);
    }

    @PostConstruct
    public void initTask() {
        kpiAccountTaskChildMapper.initTask();
        kpiConfigMapper.initConfig();
    }

    @PostConstruct
    public void initAviator() {
        // -- 1. 解析浮点数为 Decimal 类型
        AviatorEvaluator.getInstance().setOption(Options.ALWAYS_PARSE_FLOATING_POINT_NUMBER_INTO_DECIMAL, true);
        // -- 2. 解析整数为 Decimal 类型
        AviatorEvaluator.getInstance().setOption(Options.ALWAYS_PARSE_INTEGRAL_NUMBER_INTO_DECIMAL, true);
        AviatorEvaluator.addFunction(new IF());
        AviatorEvaluator.addFunction(new INT());
        AviatorEvaluator.addFunction(new DIV());
        AviatorEvaluator.addFunction(new ROUND());

        AviatorEvaluator.addOpFunction(OperatorType.DIV,
                new AbstractFunction() {
                    @Override
                    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {
                        double inputParam1 = FunctionUtils.getNumberValue(arg1, env).doubleValue();
                        double inputParam2 = FunctionUtils.getNumberValue(arg2, env).doubleValue();
                        if (inputParam1 == 0 || inputParam2 == 0) {
                            return new AviatorDouble(0);
                        } else {
                            return new AviatorDouble(inputParam1 / inputParam2);
                        }
                    }

                    @Override
                    public String getName() {
                        return "/";
                    }
                });

    }
}
