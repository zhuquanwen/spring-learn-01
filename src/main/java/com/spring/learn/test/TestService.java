package com.spring.learn.test;

import com.spring.learn.annotation.Service;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/12/1 21:34
 * @since jdk1.8
 */
@Service
public class TestService implements ITestService {
    public String test(String name) {
        return name.toUpperCase();
    }
}
