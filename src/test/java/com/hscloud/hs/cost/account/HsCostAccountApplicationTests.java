// package com.hscloud.hs.cost.account;
//
// import com.alibaba.fastjson.JSON;
// import com.hscloud.hs.cost.account.constant.enums.SignEncryptType;
// import com.hscloud.hs.cost.account.model.dto.GatewayApiDto;
// import com.hscloud.hs.cost.account.model.entity.CostDocNRelation;
// import com.hscloud.hs.cost.account.model.entity.OdsDisKslyxx;
// import com.hscloud.hs.cost.account.model.pojo.ResponseData;
// import com.hscloud.hs.cost.account.service.impl.kpi.KpiAccountUnitService;
// import com.hscloud.hs.cost.account.service.impl.kpi.KpiMemberService;
// import com.hscloud.hs.cost.account.service.impl.kpi.KpiUserAttendanceService;
// import com.hscloud.hs.cost.account.utils.GatewayApiClient;
// import com.hscloud.hs.cost.account.utils.SqlUtil;
// import com.pig4cloud.pigx.common.core.exception.BizException;
// import javafx.util.Pair;
// import org.dom4j.Document;
// import org.dom4j.DocumentException;
// import org.dom4j.Element;
// import org.dom4j.io.SAXReader;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
//
// import java.io.StringReader;
// import java.math.BigDecimal;
// import java.time.LocalDateTime;
// import java.time.format.DateTimeFormatter;
// import java.util.ArrayList;
// import java.util.HashMap;
// import java.util.List;
// import java.util.regex.Matcher;
// import java.util.regex.Pattern;
//
// @SpringBootTest
// class HsCostAccountApplicationTests {
//
//     @Autowired
//     private SqlUtil sqlUtil;
//
//     @Autowired
//     private KpiUserAttendanceService kapiUserAttendanceService;
//
//     @Autowired
//     private KpiAccountUnitService kpiAccountUnitService;
//
//     @Autowired
//     private KpiMemberService kpiMemberService;
//
//     @Test
//     void contextLoads() {
//     }
//
//     @Test
//     void contextLoads2() {
//
//         kapiUserAttendanceService.removeById(1827186223908790701L);
// //        List<KpiAccountUnit> list = kpiAccountUnitService.list(new LambdaQueryWrapper<KpiAccountUnit>().eq(KpiAccountUnit::getStatus, "1"));
// //        for (KpiAccountUnit a : list){
// //            KpiMember member = new KpiMember();
// //            member.setHostCode("g_alv");
// //            member.setMemberId(Long.valueOf(a.getResponsiblePersonId()));
// //            member.setMemberType(MemberEnum.ROLE_EMP.getType());
// //            member.setCreatedDate(new Date());
// //            member.setPeriod(0L);
// //
// //                kpiMemberService.save(member);
// //
// //        }
//     }
//
//
//     // 数据采集中心api访问测试
//     @Test
//     public void dateAdaptApiTest() {
//         GatewayApiDto gatewayApiDto = new GatewayApiDto();
//         // url
//         gatewayApiDto.setUrl("http://192.168.9.201:14084/onedataschedulerapi/openapi/gateway");
//         // 应用密钥
//         gatewayApiDto.setAppSecret("7Fc4d1Afe7954c18A8bbC7DD6a9396d6");
//         // 应用key
//         gatewayApiDto.setAppKey("06A9F1E9300F4FBA");
//         // 加密类型
//         gatewayApiDto.setSignEncryptType(SignEncryptType.SHA256);
//         // API名称method
//         gatewayApiDto.setHeaderMethodType("sql_yourfinger_ruleflow");
//
//         // 请求获取数据
//         GatewayApiClient gatewayApiClient = new GatewayApiClient(gatewayApiDto);
//         ResponseData responseData = gatewayApiClient.doPost(JSON.parseObject(""));
//         Object data = responseData.getData();
//         String msg = responseData.getMsg();
//
//         // Response:
//         System.out.println("code:" + responseData.getCode());
//         System.out.println("data:" + data);
//         System.out.println("msg:" + msg);
//     }
//
//     // model测试
//     @Test
//     public void modelTest() {
//         CostDocNRelation costDocNRelation = new CostDocNRelation();
//         costDocNRelation.setNurseAccountGroupId(1L);
//         costDocNRelation.setDocAccountGroupId(2L);
//         costDocNRelation.setId(3L);
//         costDocNRelation.setTenantId(4L);
//         costDocNRelation.insert();
//     }
//
//     // 测试获取数据的封装测试
//     @Test
//     public void fz() throws DocumentException {
//
//         String body = "<soap:Envelope xmlns:soap=\"http://www.w3.org/2003/05/soap-envelope\">\n" +
//                 "    <soap:Body>\n" +
//                 "        <won:invokeResponse xmlns:won=\"www.WondersHSBP.com\">\n" +
//                 "            <!--type: string-->\n" +
//                 "            <won:result>\n" +
//                 "                <![CDATA[\n" +
//                 "                <?xml version=\"1.0\" encoding=\"utf-8\"?><body><response><ret_code>0</ret_code><ret_info>调用成功</ret_info><Info><DEPT_CODE>113</DEPT_CODE><DEPT_NAME" +
//                 ">皮肤医美中心</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>439</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1510" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>439</NUM><AMOUNT>1510.00</AMOUNT></Info><Info><DEPT_CODE>46</DEPT_CODE><DEPT_NAME>骨伤(二)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1410</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>3998" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>1410</NUM><AMOUNT>3998.00</AMOUNT></Info><Info><DEPT_CODE>104</DEPT_CODE><DEPT_NAME>肾病科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM" +
//                 "><NON_DISPOSABLE_NUM>373</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2183.00</NON_DISPOSABLE_AMOUNT><NUM>373</NUM><AMOUNT>2183" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>34</DEPT_CODE><DEPT_NAME>妇科(一)病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1050</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2919.00</NON_DISPOSABLE_AMOUNT><NUM>1050</NUM><AMOUNT>2919.00</AMOUNT></Info><Info><DEPT_CODE>80</DEPT_CODE><DEPT_NAME>肿瘤(一)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>367</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2023" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>367</NUM><AMOUNT>2023.00</AMOUNT></Info><Info><DEPT_CODE>13</DEPT_CODE><DEPT_NAME>急诊科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM" +
//                 "><NON_DISPOSABLE_NUM>2678</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>8036.00</NON_DISPOSABLE_AMOUNT><NUM>2678</NUM><AMOUNT>8036" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>332222</DEPT_CODE><DEPT_NAME>门诊骨科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>362</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>728.00</NON_DISPOSABLE_AMOUNT><NUM>362</NUM><AMOUNT>728" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>105</DEPT_CODE><DEPT_NAME>脑病科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>191</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>756.00</NON_DISPOSABLE_AMOUNT><NUM>191</NUM><AMOUNT>756" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>45612</DEPT_CODE><DEPT_NAME>血透中心</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>38</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>267.00</NON_DISPOSABLE_AMOUNT><NUM>38</NUM><AMOUNT>267.00</AMOUNT></Info><Info><DEPT_CODE>81</DEPT_CODE><DEPT_NAME>肿瘤(二)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>421</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1901" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>421</NUM><AMOUNT>1901.00</AMOUNT></Info><Info><DEPT_CODE>94</DEPT_CODE><DEPT_NAME>经典风湿病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM" +
//                 "><NON_DISPOSABLE_NUM>232</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1643.00</NON_DISPOSABLE_AMOUNT><NUM>232</NUM><AMOUNT>1643" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>72</DEPT_CODE><DEPT_NAME>体检中心</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>346</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>4473.00</NON_DISPOSABLE_AMOUNT><NUM>346</NUM><AMOUNT>4473" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>1212121</DEPT_CODE><DEPT_NAME>皮肤甲乳肛肠男科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1762</NON_DISPOSABLE_NUM" +
//                 "><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>7294.00</NON_DISPOSABLE_AMOUNT><NUM>1762</NUM><AMOUNT>7294" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>18</DEPT_CODE><DEPT_NAME>耳鼻喉科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>696</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>10594.00</NON_DISPOSABLE_AMOUNT><NUM>696</NUM><AMOUNT>10594" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>82</DEPT_CODE><DEPT_NAME>ICU</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>512</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>3743.00</NON_DISPOSABLE_AMOUNT><NUM>512</NUM><AMOUNT>3743" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>93</DEPT_CODE><DEPT_NAME>名医馆</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>484</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>9917.00</NON_DISPOSABLE_AMOUNT><NUM>484</NUM><AMOUNT>9917" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>44</DEPT_CODE><DEPT_NAME>胃肠肝胆脑外病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>962</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2338.00</NON_DISPOSABLE_AMOUNT><NUM>962</NUM><AMOUNT>2338" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>78</DEPT_CODE><DEPT_NAME>眼耳泌外血液病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>197</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>888.00</NON_DISPOSABLE_AMOUNT><NUM>197</NUM><AMOUNT>888" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>31</DEPT_CODE><DEPT_NAME>放射科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>58</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>555.00</NON_DISPOSABLE_AMOUNT><NUM>58</NUM><AMOUNT>555" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>0000002</DEPT_CODE><DEPT_NAME>麻醉科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>6</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>60.00</NON_DISPOSABLE_AMOUNT><NUM>6</NUM><AMOUNT>60" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>88</DEPT_CODE><DEPT_NAME>感染（肺病）综合科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>60</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>460.00</NON_DISPOSABLE_AMOUNT><NUM>60</NUM><AMOUNT>460" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>36</DEPT_CODE><DEPT_NAME>化验室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>46</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>460.00</NON_DISPOSABLE_AMOUNT><NUM>46</NUM><AMOUNT>460" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>121</DEPT_CODE><DEPT_NAME>血液老年肾内病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>50</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>254.00</NON_DISPOSABLE_AMOUNT><NUM>50</NUM><AMOUNT>254" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>0002</DEPT_CODE><DEPT_NAME>手术室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>15378</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>182879.00</NON_DISPOSABLE_AMOUNT><NUM>15378</NUM><AMOUNT>182879.00</AMOUNT></Info><Info><DEPT_CODE>76</DEPT_CODE><DEPT_NAME>肿瘤(三)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>331</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1865" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>331</NUM><AMOUNT>1865.00</AMOUNT></Info><Info><DEPT_CODE>27</DEPT_CODE><DEPT_NAME>口腔科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM" +
//                 "><NON_DISPOSABLE_NUM>4244</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>12602.00</NON_DISPOSABLE_AMOUNT><NUM>4244</NUM><AMOUNT>12602" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>121</DEPT_CODE><DEPT_NAME>血液老年病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>50</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>83.00</NON_DISPOSABLE_AMOUNT><NUM>50</NUM><AMOUNT>83" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>120</DEPT_CODE><DEPT_NAME>门诊妇科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>2169</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>7346.00</NON_DISPOSABLE_AMOUNT><NUM>2169</NUM><AMOUNT>7346" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>10</DEPT_CODE><DEPT_NAME>眼科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>2210</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>6816.00</NON_DISPOSABLE_AMOUNT><NUM>2210</NUM><AMOUNT>6816" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>19</DEPT_CODE><DEPT_NAME>江东门诊</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>270</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>7450.00</NON_DISPOSABLE_AMOUNT><NUM>270</NUM><AMOUNT>7450" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>123444555</DEPT_CODE><DEPT_NAME>消化内科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>195</NON_DISPOSABLE_NUM" +
//                 "><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>862.00</NON_DISPOSABLE_AMOUNT><NUM>195</NUM><AMOUNT>862" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>92</DEPT_CODE><DEPT_NAME>内分泌科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>89</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>867.00</NON_DISPOSABLE_AMOUNT><NUM>89</NUM><AMOUNT>867" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>11122</DEPT_CODE><DEPT_NAME>检验科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1840</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>18393.00</NON_DISPOSABLE_AMOUNT><NUM>1840</NUM><AMOUNT>18393" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>01010</DEPT_CODE><DEPT_NAME>针灸推拿科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>643</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>11587.00</NON_DISPOSABLE_AMOUNT><NUM>643</NUM><AMOUNT>11587" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>109</DEPT_CODE><DEPT_NAME>心内科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>40</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>422.00</NON_DISPOSABLE_AMOUNT><NUM>40</NUM><AMOUNT>422" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>113</DEPT_CODE><DEPT_NAME>皮肤医美中心（护）</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>439</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT" +
//                 ">0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>18444.00</NON_DISPOSABLE_AMOUNT><NUM>439</NUM><AMOUNT>18444" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>103</DEPT_CODE><DEPT_NAME>针推康复老年病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>686</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>4840.00</NON_DISPOSABLE_AMOUNT><NUM>686</NUM><AMOUNT>4840" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>12312311</DEPT_CODE><DEPT_NAME>中治室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1847</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT" +
//                 ">0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>63845.00</NON_DISPOSABLE_AMOUNT><NUM>1847</NUM><AMOUNT>63845" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>20</DEPT_CODE><DEPT_NAME>美容</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>5</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>45.00</NON_DISPOSABLE_AMOUNT><NUM>5</NUM><AMOUNT>45" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>106</DEPT_CODE><DEPT_NAME>肺病科病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>200</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>885.00</NON_DISPOSABLE_AMOUNT><NUM>200</NUM><AMOUNT>885" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>78</DEPT_CODE><DEPT_NAME>眼耳泌外病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>197</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>464.00</NON_DISPOSABLE_AMOUNT><NUM>197</NUM><AMOUNT>464" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>14</DEPT_CODE><DEPT_NAME>肛肠科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>129</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>192.00</NON_DISPOSABLE_AMOUNT><NUM>129</NUM><AMOUNT>192.00</AMOUNT></Info><Info><DEPT_CODE>45</DEPT_CODE><DEPT_NAME>骨伤(一)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1085</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2646" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>1085</NUM><AMOUNT>2646.00</AMOUNT></Info><Info><DEPT_CODE>35</DEPT_CODE><DEPT_NAME>妇科(二)" +
//                 "病区</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>716</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2026" +
//                 ".00</NON_DISPOSABLE_AMOUNT><NUM>716</NUM><AMOUNT>2026.00</AMOUNT></Info><Info><DEPT_CODE>83</DEPT_CODE><DEPT_NAME>输液室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM" +
//                 "><NON_DISPOSABLE_NUM>553</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>4011.00</NON_DISPOSABLE_AMOUNT><NUM>553</NUM><AMOUNT>4011" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>3333</DEPT_CODE><DEPT_NAME>内镜中心</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1638</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>5210.00</NON_DISPOSABLE_AMOUNT><NUM>1638</NUM><AMOUNT>5210" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>13344</DEPT_CODE><DEPT_NAME>介入科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>31</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>107.00</NON_DISPOSABLE_AMOUNT><NUM>31</NUM><AMOUNT>107" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>91</DEPT_CODE><DEPT_NAME>发热门诊</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>4</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>40.00</NON_DISPOSABLE_AMOUNT><NUM>4</NUM><AMOUNT>40" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>40</DEPT_CODE><DEPT_NAME>B超室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>101</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1000.00</NON_DISPOSABLE_AMOUNT><NUM>101</NUM><AMOUNT>1000" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>62</DEPT_CODE><DEPT_NAME>普通门诊</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>3</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>30.00</NON_DISPOSABLE_AMOUNT><NUM>3</NUM><AMOUNT>30" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>102</DEPT_CODE><DEPT_NAME>肺功能室</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>187</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>6160.00</NON_DISPOSABLE_AMOUNT><NUM>187</NUM><AMOUNT>6160" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>332222</DEPT_CODE><DEPT_NAME>骨科门诊</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>362</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>5.00</NON_DISPOSABLE_AMOUNT><NUM>362</NUM><AMOUNT>5" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>98</DEPT_CODE><DEPT_NAME>门诊外科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>1188</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>2643.00</NON_DISPOSABLE_AMOUNT><NUM>1188</NUM><AMOUNT>2643" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>88</DEPT_CODE><DEPT_NAME>感染（肺病）科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>60</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>1.00</NON_DISPOSABLE_AMOUNT><NUM>60</NUM><AMOUNT>1" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>43</DEPT_CODE><DEPT_NAME>院感科</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>8</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>160.00</NON_DISPOSABLE_AMOUNT><NUM>8</NUM><AMOUNT>160" +
//                 ".00</AMOUNT></Info><Info><DEPT_CODE>97</DEPT_CODE><DEPT_NAME>门诊盆底</DEPT_NAME><DISPOSABLE_NUM>0</DISPOSABLE_NUM><NON_DISPOSABLE_NUM>28</NON_DISPOSABLE_NUM><DISPOSABLE_AMOUNT>0" +
//                 "</DISPOSABLE_AMOUNT><NON_DISPOSABLE_AMOUNT>420.00</NON_DISPOSABLE_AMOUNT><NUM>28</NUM><AMOUNT>420.00</AMOUNT></Info></response></body>]]>\n" +
//                 "</won:result>\n" +
//                 "</won:invokeResponse>\n" +
//                 "</soap:Body>\n" +
//                 "</soap:Envelope>";
//         // 获取body中的内容并进行解析
//         Pattern pattern = Pattern.compile("<!\\[CDATA\\[(.*?)\\]\\]>", Pattern.DOTALL);
//         Matcher matcher = pattern.matcher(body);
//         // 获取 <body>……</body>中的内容
//         String xmlContext = null;
//         if (matcher.find()) {
//             xmlContext = matcher.group(1).trim();
//         } else {
//             throw new BizException("当前返回数据不合法！");
//         }
//
//         SAXReader reader = new SAXReader();
//         Document document = reader.read(new StringReader(xmlContext));
//
//         Element rootElement = document.getRootElement();
//
//         Element responseElement = rootElement.element("response"); // 获取'response'元素
//
//         // 判断响应的结果是否成功，即ret_code是否为0
//         String code = responseElement.elementText("ret_code");
//
//         if (code.equals("1")) {
//             throw new BizException("调用接口发生异常！");
//         }
//
//         // 判断响应的结果信息
//         String msg = responseElement.elementText("ret_info");
//
//         // 封装返回的date信息
//         List<OdsDisKslyxx> datas = new ArrayList<>();
//         List<Element> elements = responseElement.elements("Info");
//         for (Element element : elements) {
//             OdsDisKslyxx data = new OdsDisKslyxx();
//             data.setDeptCode(element.elementText("DEPT_CODE"));
//             data.setDeptName(element.elementText("DEPT_NAME"));
//             data.setDisposableNum(new BigDecimal(element.elementText("DISPOSABLE_NUM")));
//             data.setNonDisposableNum(new BigDecimal(element.elementText("NON_DISPOSABLE_NUM")));
//             data.setDisposableAmount(new BigDecimal(element.elementText("DISPOSABLE_AMOUNT")));
//             data.setNonDisposableAmount(new BigDecimal(element.elementText("NON_DISPOSABLE_AMOUNT")));
//             data.setNum(new BigDecimal(element.elementText("NUM")));
//             data.setAmount(new BigDecimal(element.elementText("AMOUNT")));
//             // 自定插入
//             // data.setCreateTime("");
//             datas.add(data);
//         }
//
//         // 3.保存到数据库中
//
//         // 初始化hashmap
//         HashMap<Pair<String, String>, Integer> hashMap = new HashMap<>();
//
//
//         // todo 目前只保存到数据库中,并进行逻辑处理，可以与上面的循环进行合并（优化）
//         for (OdsDisKslyxx data : datas) {
//             /**
//              * 逻辑处理
//              * 1. dt的时间粒度是按日
//              * 2. seq是数据接受序号，同日期同科室接受多次的，seq自增
//              */
//
//             LocalDateTime localDateTime = LocalDateTime.now();
//             DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd");
//             String dt = localDateTime.format(dateTimeFormatter);
//             data.setDt(dt);
//
//             // 当前容器和科室
//             Pair<String, String> pair = new Pair<>(dt, data.getDeptCode());
//             if (hashMap.get(pair) == null) {
//                 hashMap.put(pair, 1);
//             } else {
//                 hashMap.replace(pair, hashMap.get(pair) + 1);
//             }
//
//             data.setSeq(hashMap.get(pair).toString());
//
//             // 插入操作
//             // data.insert();
//
//             // 通过jdbcTemplate去执行插入操作
//             sqlUtil.insertOdsDisKslyxx(data);
//
//
//         }
//     }
//
//
// }
