package com.xzj.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;

/**
 * 为 passport 的 Mapper 增加服务前缀，避免与公共兼容层中的同名 Mapper 冲突。
 */
public class PassportMapperBeanNameGenerator implements BeanNameGenerator {

    @Override
    public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
        String className = definition.getBeanClassName();
        int lastDot = className == null ? -1 : className.lastIndexOf('.');
        String simpleName = lastDot >= 0 ? className.substring(lastDot + 1) : className;
        return "passportService" + simpleName;
    }
}


