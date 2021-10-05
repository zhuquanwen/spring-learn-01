package com.spring.learn.webmvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author zhuquanwen
 * @vesion 1.0
 * @date 2021/10/5 11:51
 * @since jdk1.8
 */
public class View {
    private File viewFile;
    public View(File templateFile) {
        this.viewFile = templateFile;
    }

    public void render(Map<String, ?> model, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        StringBuilder sb = new StringBuilder();
        RandomAccessFile randomAccessFile = new RandomAccessFile(this.viewFile, "r");
        String line = null;
        while ((line = randomAccessFile.readLine()) != null) {
            line = new String(line.getBytes(StandardCharsets.ISO_8859_1), StandardCharsets.UTF_8);
            Pattern pattern = Pattern.compile("\\$\\{[^\\}]+\\}", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                String paramName = matcher.group();
                paramName = paramName.replaceAll("\\$\\{|\\}", "");
                Object o = model.get(paramName);
                line = matcher.replaceFirst(makeStrToRegExp(o.toString()));
                matcher = pattern.matcher(line);
            }
            sb.append(line);
        }
        resp.setCharacterEncoding("utf-8");
        resp.getWriter().write(sb.toString());
    }

    private String makeStrToRegExp(String str) {
        return str.replace("\\", "\\\\")
                .replace("*", "\\*")
                .replace("+", "\\+")
                .replace("|", "\\|")
                .replace("{", "\\{")
                .replace("}", "\\}")
                .replace("(", "\\(")
                .replace(")", "\\)")
                .replace("^", "\\^")
                .replace("&", "\\&")
                .replace("[", "\\[")
                .replace("]", "\\]")
                .replace("$", "\\$")
                .replace("?", "\\?")
                .replace(".", "\\.")
                .replace(",", "\\,");
    }
}
