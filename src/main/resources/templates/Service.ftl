<#assign entityCodeBig = entityCode?cap_first>
package ${comPathDot}.service.impl${childSufDot};

import lombok.RequiredArgsConstructor;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import ${comPathDot}.mapper${childSufDot}.${entityCodeBig}Mapper;
import ${comPathDot}.model.entity${childSufDot}.${entityCodeBig};
import ${comPathDot}.service${childSufDot}.I${entityCodeBig}Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
* ${entityName} 服务实现类
*
*/
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ${entityCodeBig}Service extends ServiceImpl<${entityCodeBig}Mapper, ${entityCodeBig}> implements I${entityCodeBig}Service {


}
