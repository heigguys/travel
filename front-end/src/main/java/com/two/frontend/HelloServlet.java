package com.two.frontend;

import java.io.*;

import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;

@WebServlet(name = "helloServlet", value = "/hello-servlet")
/**
 * 示例 Servlet，用于验证前端 Web 工程可以正常部署和响应请求。
 */
public class HelloServlet extends HttpServlet {
    private String message;

    /**
     * 初始化示例响应文本。
     */
    public void init() {
        message = "Hello World!";
    }

    /**
     * 输出一个简单 HTML 页面。
     *
     * @param request HTTP 请求
     * @param response HTTP 响应
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html");

        // 生成示例页面内容。
        PrintWriter out = response.getWriter();
        out.println("<html><body>");
        out.println("<h1>" + message + "</h1>");
        out.println("</body></html>");
    }

    /**
     * Servlet 销毁钩子，当前没有额外资源需要释放。
     */
    public void destroy() {
    }
}
