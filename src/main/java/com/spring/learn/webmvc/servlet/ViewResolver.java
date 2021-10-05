package com.spring.learn.webmvc.servlet;

import java.io.File;
import java.util.Objects;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 10:57
 * @since jdk1.8
 */
public class ViewResolver {
    private File templateRootDir;
    private final static String DEFAULT_TEMPLATE_SUFFIX = ".html";
    public ViewResolver(String path) {
        String templateRootPath = this.getClass().getClassLoader().getResource(path).getFile();
        this.templateRootDir = new File(templateRootPath);
    }

    public View resolverViewName(String viewName) {
        if (viewName == null || Objects.equals("", viewName)) {
            return null;
        }
        if (!viewName.endsWith(DEFAULT_TEMPLATE_SUFFIX)) {
            viewName += DEFAULT_TEMPLATE_SUFFIX;
        }
        File templateFile = new File((this.templateRootDir.getPath() + "/" + viewName).replaceAll("/+", "/"));
        return new View(templateFile);
    }
}
