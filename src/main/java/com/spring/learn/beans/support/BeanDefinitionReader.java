package com.spring.learn.beans.support;

import com.spring.learn.Constants;
import com.spring.learn.config.BeanDefinition;
import com.spring.learn.util.ScannerUtils;

import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 *
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
            
        }
        return definitions;
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
            registryBeanClass.addAll(ScannerUtils.getClasses(scanPackage));
        }
    }
}
