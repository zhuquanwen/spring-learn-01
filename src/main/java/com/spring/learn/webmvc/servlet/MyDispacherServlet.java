package com.spring.learn.webmvc.servlet;

import com.spring.learn.Constants;
import com.spring.learn.annotation.RequestMapping;
import com.spring.learn.annotation.RestController;
import com.spring.learn.context.ApplicationContext;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Pattern;

/**
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2020/11/29 21:01
 * @since jdk1.8
 */
public class MyDispacherServlet extends HttpServlet implements Constants {
    //初始化IoC
    private Properties contextConfig = new Properties();
    //    private Map<String, HandlerMapping> handlerMapping = new HashMap<>();
    private ApplicationContext context;
    private List<HandlerMapping> handlerMappings = new ArrayList<>();
    private Map<HandlerMapping, HandlerAdaptor> handlerAdaptors = new HashMap<>();
    private List<ViewResolver> viewResolvers = new ArrayList<>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispacher(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> model = new HashMap<>();
            model.put("message", e.getMessage() == null ? "" : e.getMessage());
            model.put("detail", Arrays.toString(e.getStackTrace()));
            processDispatchResult(req, resp, new ModelAndView("500", model));
        }
    }

    private void doDispacher(HttpServletRequest req, HttpServletResponse resp) throws InvocationTargetException, IllegalAccessException, IOException {
        //1、根据URL找到HandlerMapping对象
        HandlerMapping handlerMapping = getHandler(req);
        if (handlerMapping == null) {
            //返回404
            processDispatchResult(req, resp, new ModelAndView("404"));
            return;
        }

        //2、根据HandlerMapping拿到HandlerAdaptor对象
        HandlerAdaptor handlerAdaptor = getHandlerAdaptor(handlerMapping);

        //3、根据handlerAdaptor对象动态匹配参数返回ModelAndView
        ModelAndView modelAndView = handlerAdaptor.handle(req, resp, handlerMapping);

        //4、根据ModelAndView决定选择哪个ModelResolver进行解析
        processDispatchResult(req, resp, modelAndView);

    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, ModelAndView modelAndView) throws IOException {
        if (modelAndView == null) {
            return;
        }
        if (this.viewResolvers.isEmpty()) {
            return;
        }
        for (ViewResolver viewResolver : this.viewResolvers) {
            View view = viewResolver.resolverViewName(modelAndView.getViewName());
            view.render(modelAndView.getModel(), req, resp);
            return;
        }

    }

    private HandlerAdaptor getHandlerAdaptor(HandlerMapping handlerMapping) {
        return this.handlerAdaptors.get(handlerMapping);
    }

    private HandlerMapping getHandler(HttpServletRequest req) {
        String requestURI = req.getRequestURI();
        String url = requestURI.replace(req.getContextPath(), "").replaceAll("/+", "/");
        return handlerMappings.stream().filter(handlerMapping -> handlerMapping.getPattern().matcher(url).matches())
                .findFirst().orElse(null);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        try {
            //初始化applicationContext
            context = new ApplicationContext(config.getInitParameter(INIT_PARAMETER_CONFIGURATION_LOCATION));

            //初始化MVC九大组件
            initStrategies(context);

            System.out.println("服务已启动");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initStrategies(ApplicationContext context) throws Exception {
        //多文件上传组件
//        initMultipartResolver(context);
        //初始化本地语言环境
//        initLocaleResolver(context);
        //初始化模板处理器
//        initThemeResolver(context);
        //handlerMapping
        initHandlerMappings(context);
        //初始化参数适配器
        initHandlerAdapters(context);
        //初始化异常拦截器
//        initHandlerExceptionResolvers(context);
        //初始化视图转换器
//        initRequestToViewNameTranslator(context);
        //初始化视图处理器
        initViewResolvers(context);
        //flashMap管理器
//        initFlashMapManager(context);
    }

    private void initViewResolvers(ApplicationContext context) {
        String templateRoot = context.getConfig().getProperty("template.root");
        //TODO 打成jar包后应该就不能运行了
        String templateRootPath = this.getClass().getClassLoader().getResource(templateRoot).getFile();
        File[] files = new File(templateRootPath).listFiles();
        for (File file : files) {
            this.viewResolvers.add(new ViewResolver(templateRoot));
        }
    }

    private void initHandlerAdapters(ApplicationContext context) {
        for (HandlerMapping handlerMapping : handlerMappings) {
            this.handlerAdaptors.put(handlerMapping, new HandlerAdaptor());
        }
    }

    private void initHandlerMappings(ApplicationContext context) throws Exception {
        if (this.context.getBeanDefiniationCount() == 0) {
            return;
        }
        String[] beanNames = context.getBeanDefiniationNames();
        for (String beanName : beanNames) {
            Object instance = context.getBean(beanName);
            Class<?> aClass = instance.getClass();
            if (!aClass.isAnnotationPresent(RestController.class)) {
                continue;
            }

            //获取上层path
            RequestMapping baseReqMapping = aClass.getAnnotation(RequestMapping.class);
            String basePath = null;
            if (baseReqMapping != null) {
                basePath = baseReqMapping.value();
            }

            //只取public的函数
            Method[] methods = aClass.getMethods();
            for (Method method : methods) {
                //函数必须被注解RequestMapping修饰
                if (!method.isAnnotationPresent(RequestMapping.class)) {
                    continue;
                }
                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                String regex = requestMapping.value();
                regex = ("/" + basePath + "/" + regex).replaceAll("/+", "/")
                        .replaceAll("\\*", ".*");
                Pattern pattern = Pattern.compile(regex);
                handlerMappings.add(new HandlerMapping(pattern, instance, method));
            }
        }
    }
}
