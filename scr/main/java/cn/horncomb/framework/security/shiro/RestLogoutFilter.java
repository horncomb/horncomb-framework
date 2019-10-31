package cn.horncomb.framework.security.shiro;

import org.apache.shiro.web.filter.authc.LogoutFilter;

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
        if (this.logoutResponseBuilder != null)
            this.logoutResponseBuilder.writeSuccessResponse((HttpServletRequest) request, resp);
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.flushBuffer();
    }
}
