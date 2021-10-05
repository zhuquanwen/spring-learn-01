package com.spring.learn.test;

import com.spring.learn.annotation.*;
import com.spring.learn.test.service.ITestService;
import com.spring.learn.webmvc.servlet.ModelAndView;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;

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
    public ModelAndView aaa(@RequestParam("a") String a) {
        return new ModelAndView("test", new HashMap<String, Object>(){{
            put("username", testService.test(a));
        }});
    }

    @RequestMapping(method = RequestMethod.GET, value = "/error")
    public ModelAndView error(@RequestParam("a") String a) {
        return new ModelAndView("test", new HashMap<String, Object>(){{
            put("username", testService.error(a));
        }});
    }

    @RequestMapping(method = RequestMethod.GET, value = "/json")
    public ModelAndView json(HttpServletResponse resp, @RequestParam("a") String a) throws IOException {
        resp.getWriter().println("Hi," + testService.test(a));
        return null;
    }
}
