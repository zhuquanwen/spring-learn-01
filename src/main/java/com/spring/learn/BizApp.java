package com.spring.learn;

import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;

import javax.servlet.ServletException;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/9/20 11:14
 * @since jdk1.8
 */
public class BizApp {
    public static void main(String[] args) throws ServletException {
        DeploymentInfo servletBuilder = Servlets.deployment().setClassLoader(BizApp.class.getClassLoader())
                .setDeploymentName("spring-demo").setContextPath("/")
                .addServlets(Servlets.servlet("dispacherServlet", MyDispacherServlet.class)
                        .addInitParam(Constants.INIT_PARAMETER_CONFIGURATION_LOCATION, "application.properties")
                        .addMapping("/*"));
        DeploymentManager manager = Servlets.defaultContainer().addDeployment(servletBuilder);
        manager.deploy();
        PathHandler path = Handlers.path(Handlers.redirect("/")).addPrefixPath("/", manager.start());
        Undertow server = Undertow.builder().addHttpListener(8080, "localhost").setHandler(path).build();
        server.start();
    }
}
