package com.uin.server.config.security.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.uin.server.vo.RespBean;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * \* Created with IntelliJ IDEA.
 * \* @author wanglufei
 * \* Date: 2021年08月07日 15:12
 * \* Description: 当访问接口没有权限时,自定义返回结果
 * \
 */
//访问接口没有权限时
@Component
public class RestfulAccessDeniedHandler implements AccessDeniedHandler {
    /**
     * 拒绝访问处理程序
     *
     * @param request
     * @param response
     * @param e
     * @author wanglufei
     * @date 2022/4/13 8:25 AM
     */
    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException e) throws IOException, ServletException {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        //RespBean bean = RespBean.error("RestfulAccessDeniedHandler + 权限不足!");
        RespBean bean = RespBean.error("权限不足请联系管理员!");
        //403
        bean.setCode(403);
        //ObjectMapper对象映射器
        out.write(new ObjectMapper().writeValueAsString(bean));
        out.flush();
        out.close();
    }
}
