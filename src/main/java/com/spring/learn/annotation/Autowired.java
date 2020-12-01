package com.spring.learn.annotation;

import java.lang.annotation.*;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/11/30 19:55
 * @since jdk1.8
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Autowired {
    String name() default "";
}
