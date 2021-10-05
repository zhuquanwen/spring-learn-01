package com.spring.learn.beans.support;

import com.spring.learn.Constants;
import com.spring.learn.annotation.Component;
import com.spring.learn.annotation.Repository;
import com.spring.learn.annotation.RestController;
import com.spring.learn.annotation.Service;
import com.spring.learn.config.BeanDefinition;
import com.spring.learn.util.ScannerUtils;
import com.spring.learn.util.StringUtil;

import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/23 20:58
 * @since jdk1.8
 */
public class BeanDefinitionReader implements Constants {
    private String[] locations;
    private Properties contextConfig = new Properties();
    private Set<Class<?>> registryBeanClass = new HashSet<>();

    public BeanDefinitionReader(String[] locations) {
        this.locations = locations;

        //1. 加载配置文件
        doLoadConfig(locations[0]);

        //2. 解析配置文件
        try {
            doScanner(contextConfig.getProperty("scan.packages"));
        } catch (IOException e) {
            throw new RuntimeException("解析配置文件出错", e);
        }
    }

    public List<BeanDefinition> doLoadBeanDefinitions() {
        List<BeanDefinition> definitions = new ArrayList<>();

        for (Class<?> beanClass : registryBeanClass) {
            if (beanClass.isInterface()) {
                continue;
            }
            //默认使用类型首字母小写的名字作为key
            definitions.add(doCreateBeanDefinition(StringUtil.firstLower(beanClass.getSimpleName()), beanClass.getName()));

            //如果这个类有接口,使用接口名字作为key
            Optional.ofNullable(beanClass.getInterfaces())
                    .ifPresent(interfaces -> Arrays.stream(interfaces).forEach(anInterface -> {
                        definitions.add(doCreateBeanDefinition(StringUtil.firstLower(anInterface.getName()), beanClass.getName()));
                    }));
        }
        return definitions;
    }

    public Properties getConfig() {
        return contextConfig;
    }

    private BeanDefinition doCreateBeanDefinition(String factoryBeanName, String beanClassName) {
        return new BeanDefinition(factoryBeanName, beanClassName);
    }

    private void doLoadConfig(String location) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location.replace("classpath:", ""))) {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            //todo 写日志
        }
    }

    private void doScanner(String scanPackages) throws IOException {
        for (String scanPackage : scanPackages.split(",")) {
            registryBeanClass.addAll(ScannerUtils.getClasses(scanPackage).stream().filter(aClass ->  aClass.isAnnotationPresent(Component.class) ||
                    aClass.isAnnotationPresent(RestController.class) ||
                    aClass.isAnnotationPresent(Service.class) ||
                    aClass.isAnnotationPresent(Repository.class)).collect(Collectors.toList()));
        }
    }
}
