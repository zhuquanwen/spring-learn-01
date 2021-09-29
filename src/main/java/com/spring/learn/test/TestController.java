package com.spring.learn.test;

import com.spring.learn.annotation.*;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/12/1 21:18
 * @since jdk1.8
 */
@RestController
@RequestMapping("/t")
public class TestController {
    @Autowired
    private ITestService testService;
    @RequestMapping(method = RequestMethod.GET, value = "/t")
    public String aaa(@RequestParam("a") String a) {
        return "hi:" + testService.test(a);
    }
}
