package com.spring.learn.context;

import com.spring.learn.beans.support.BeanDefinitionReader;
import com.spring.learn.config.BeanDefinition;
import com.spring.learn.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/23 20:38
 * @since jdk1.8
 */
public class ApplicationContext {
    private String[] locations;
    private BeanDefinitionReader reader;
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    public ApplicationContext(String... locations) {
        this.locations = locations;

        //1. 加载配置文件
        this.reader = new BeanDefinitionReader(locations);

        //2. 将所有的配置信息解析为BeanDefinition
        List<BeanDefinition> beanDefinitions = this.reader.doLoadBeanDefinitions();

        //3. 将BeanDefinition缓存起来
        doRegistryBeanDefinition(beanDefinitions);

        //4. 根据BeanDefinition实例化所有的Bean
        doCreateBean();
    }

    private void doCreateBean() {
        //调用getBean方法
    }

    private void doRegistryBeanDefinition(List<BeanDefinition> beanDefinitions) {
        //缓存到BeanDefinitionMap
    }

    public Object getBean(Class clazz) {
        return getBean(StringUtil.firstLower(clazz.getSimpleName()));
    }
    public Object getBean(String beanName) {
        return null;
    }
}
