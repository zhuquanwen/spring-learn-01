package com.spring.learn.config;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/23 20:58
 * @since jdk1.8
 */
public class BeanDefinition {
    private String factoryBeanName;
    private String beanClassName;

    public String getFactoryBeanName() {
        return factoryBeanName;
    }

    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    public String getBeanClassName() {
        return beanClassName;
    }

    public void setBeanClassName(String beanClassName) {
        this.beanClassName = beanClassName;
    }

    public BeanDefinition() {
    }
    public BeanDefinition(String factoryBeanName, String beanClassName) {
        this.factoryBeanName = factoryBeanName;
        this.beanClassName = beanClassName;
    }
}
