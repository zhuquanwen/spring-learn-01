package com.spring.learn.context;

import com.spring.learn.annotation.*;
import com.spring.learn.beans.BeanWrapper;
import com.spring.learn.beans.support.BeanDefinitionReader;
import com.spring.learn.config.BeanDefinition;
import com.spring.learn.util.StringUtil;

import java.lang.reflect.Field;
import java.util.*;

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
    private Map<String, BeanWrapper> factoryBeanInstanceCache = new HashMap<>();
    private Map<String, Object> factoryBeanObjectCache = new HashMap<>();

    public ApplicationContext(String... locations) {
        try {
            this.locations = locations;

            //1. 加载配置文件
            this.reader = new BeanDefinitionReader(locations);

            //2. 将所有的配置信息解析为BeanDefinition
            List<BeanDefinition> beanDefinitions = this.reader.doLoadBeanDefinitions();

            //3. 将BeanDefinition缓存起来
            doRegistryBeanDefinition(beanDefinitions);

            //4. 根据BeanDefinition实例化所有的Bean
            doCreateBean();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void doCreateBean() throws Exception {
        //调用getBean方法
        for (Map.Entry<String, BeanDefinition> beanDefinitionEntry : beanDefinitionMap.entrySet()) {
            String beanName = beanDefinitionEntry.getKey();
            BeanDefinition beanDefinition = beanDefinitionEntry.getValue();
            Object bean = getBean(beanName);
        }
    }

    private void doRegistryBeanDefinition(List<BeanDefinition> beanDefinitions) throws Exception {
        //缓存到BeanDefinitionMap
        for (BeanDefinition beanDefinition : beanDefinitions) {
            if (beanDefinitionMap.containsKey(beanDefinition.getFactoryBeanName())) {
                throw new Exception("The beanName:" + beanDefinition.getBeanClassName() + " is exists");
            }
            beanDefinitionMap.put(beanDefinition.getFactoryBeanName(), beanDefinition);
            beanDefinitionMap.put(beanDefinition.getBeanClassName(), beanDefinition);
        }

    }

    public Object getBean(Class clazz) throws Exception {
        return getBean(StringUtil.firstLower(clazz.getSimpleName()));
    }

    /**
     * 完成bean的实例化并依赖注入
     * */
    public Object getBean(String beanName) throws Exception {
        //拿到beanName对应的配置信息-BeanDefinition
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if (beanDefinition == null) {
            return null;
        }

        //根据BeanDefinition创建实例
        Object instance = instaniateBean(beanName, beanDefinition);

        //将实例封装成BeanWrapper
        BeanWrapper beanWrapper = new BeanWrapper(instance);

        //将BeanWrapper对象缓存到IOC容器中
        factoryBeanInstanceCache.put(beanName, beanWrapper);

        //完成依赖注入
        populateBean(beanName, beanDefinition, beanWrapper);

        return this.factoryBeanInstanceCache.get(beanName).getWrapperInstance();
    }

    public int getBeanDefiniationCount() {
        return beanDefinitionMap.size();
    }


    private void populateBean(String beanName, BeanDefinition beanDefinition, BeanWrapper beanWrapper) throws IllegalAccessException {
        Object instance = beanWrapper.getWrapperInstance();
        Class<?> wrapperClass = beanWrapper.getWrapperClass();
        Field[] fields = wrapperClass.getDeclaredFields();
        for (Field field : fields) {
            if (!field.isAnnotationPresent(Autowired.class)) {
                continue;
            }

            Autowired autowired = field.getAnnotation(Autowired.class);
            String autowiredBeanName = autowired.name();
            if ("".equals(autowiredBeanName)) {
                autowiredBeanName = field.getType().getName();
            }
            if (!factoryBeanInstanceCache.containsKey(autowiredBeanName)) {
                continue;
            }
            field.setAccessible(true);
            field.set(instance, factoryBeanInstanceCache.get(autowiredBeanName));
        }
    }



    private Object instaniateBean(String beanName, BeanDefinition beanDefinition) throws Exception {
        String beanClassName = beanDefinition.getBeanClassName();
        Class<?> aClass = Class.forName(beanClassName);
        if (!aClass.isAnnotationPresent(Component.class) &&
                !aClass.isAnnotationPresent(RestController.class) &&
                !aClass.isAnnotationPresent(Service.class) &&
                !aClass.isAnnotationPresent(Repository.class)) {
            return null;
        }
        Object instance = aClass.getDeclaredConstructor().newInstance();

        //如果匹配切面表达式，创建代理对象

        this.factoryBeanObjectCache.put(beanName, instance);
        return instance;
    }
}
