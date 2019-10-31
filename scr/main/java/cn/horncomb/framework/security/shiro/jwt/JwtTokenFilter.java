package cn.horncomb.framework.security.shiro.jwt;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.util.StringUtils;
import org.apache.shiro.web.filter.authc.AuthenticatingFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JwtTokenFilter extends AuthenticatingFilter {
    private final Logger log = LoggerFactory.getLogger(JwtTokenFilter.class);

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String BEAR_TOKEN_PREFIX = "Bear";

    private String authorizationHeader = AUTHORIZATION_HEADER;

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        return new JwtToken(resolveToken(request));
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        return super.isAccessAllowed(request, response, mappedValue)
                || this.isLoginRequest(request, response); // 地址是登录请求，提交给登录过滤器处理
    }

    @Override
    protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
        if (hasJwtToken(request)) { // 是否带有token
            if (log.isTraceEnabled()) {
                log.trace("Jwt token detected. Attempting to execute login.");
            }
            return super.executeLogin(request, response);
        }
        return true;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse httpResponse = WebUtils.toHttp(response);
        httpResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 未授权
        return false;
    }

    protected boolean hasJwtToken(ServletRequest request) {
        Assert.isAssignable(HttpServletRequest.class, request.getClass());
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return StringUtils.hasText(httpRequest.getHeader(this.getAuthorizationHeader()));
    }

    protected String resolveToken(ServletRequest request) {
        Assert.isAssignable(HttpServletRequest.class, request.getClass());
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String token = httpRequest.getHeader(this.getAuthorizationHeader());
        Assert.hasText(token, "Token is empty in HTTP header[" + this.getAuthorizationHeader() + "]");
        return token;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }
}
