package com.spring.learn.beans.support;

import com.spring.learn.Constants;
import com.spring.learn.config.BeanDefinition;

import javax.servlet.ServletConfig;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

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

    public BeanDefinitionReader(String[] locations) {
        this.locations = locations;

        //1. 加载配置文件
        doLoadConfig(locations[0]);

        //2. 解析配置文件
    }

    public List<BeanDefinition> doLoadBeanDefinitions() {
        return null;
    }

    private void doLoadConfig(String location) {
        try (InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(location)) {
            contextConfig.load(is);
        } catch (IOException e) {
            e.printStackTrace();
            //todo 写日志
        }
    }
}
