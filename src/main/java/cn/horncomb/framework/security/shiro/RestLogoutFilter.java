package cn.horncomb.framework.security.shiro;

import org.apache.shiro.web.filter.authc.LogoutFilter;
import org.springframework.http.HttpStatus;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestLogoutFilter extends LogoutFilter {
    private LogoutResponseBuilder logoutResponseBuilder;

    public RestLogoutFilter() {
        this(null);
    }

    public RestLogoutFilter(LogoutResponseBuilder logoutResponseBuilder) {
        this.logoutResponseBuilder = logoutResponseBuilder;
    }

    @Override
    protected void issueRedirect(ServletRequest request, ServletResponse response, String redirectUrl) throws Exception {
        HttpServletResponse resp = (HttpServletResponse) response;
        prepareResponse(HttpStatus.OK,request,response);
        if (this.logoutResponseBuilder != null)
            this.logoutResponseBuilder.writeSuccessResponse((HttpServletRequest) request, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
    }

    private HttpServletResponse prepareResponse(HttpStatus httpStatus, ServletRequest request, ServletResponse
            response) {
        HttpServletResponse resp = (HttpServletResponse) response;
        HttpServletRequest requ = (HttpServletRequest) request;
        //跨域的header设置
        resp.setHeader("Access-control-Allow-Origin", requ.getHeader("Origin"));
        resp.setHeader("Access-Control-Allow-Methods", requ.getMethod());
        resp.setHeader("Access-Control-Allow-Credentials", "true");
        resp.setHeader("Access-Control-Max-Age", "3600");
        resp.setHeader("Access-Control-Allow-Headers", requ.getHeader("Access-Control-Request-Headers"));
        resp.setHeader("Access-Control-Expose-Headers","Authorization");
        //防止乱码，适用于传输JSON数据
        resp.setHeader("Content-Type","application/json;charset=UTF-8");
        resp.setCharacterEncoding(request.getCharacterEncoding());
        resp.setStatus(httpStatus.value());
//        resp.setContentType(request.getContentType());
        return resp;
    }
}
