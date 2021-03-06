package cn.horncomb.framework.security.shiro;

import cn.horncomb.framework.web.rest.errors.ExceptionTranslator;
import com.alibaba.fastjson.JSONObject;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.core.GenericTypeResolver;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.GenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.util.Assert;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.ServletWebRequest;
import org.zalando.problem.spring.common.HttpStatusAdapter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;

public class RestAuthenticationFilter extends FormAuthenticationFilter {
    private final Logger log = LoggerFactory.getLogger(RestAuthenticationFilter.class);

    private HttpStatus loginFaildHttpStatus = HttpStatus.UNAUTHORIZED;
    private HttpStatus loginSuccessHttpStatus = HttpStatus.OK;

    private LoginResultProvider loginResultProvider;
    private final ExceptionTranslator exceptionTranslator;
    private final HttpMessageConverters httpMessageConverters;

    public RestAuthenticationFilter(ExceptionTranslator exceptionTranslator, HttpMessageConverters httpMessageConverters) {
        this(exceptionTranslator, httpMessageConverters, null);
    }

    public RestAuthenticationFilter(ExceptionTranslator exceptionTranslator, HttpMessageConverters httpMessageConverters,
                                    LoginResultProvider loginResultProvider) {
        this.loginResultProvider = loginResultProvider;
        this.exceptionTranslator = exceptionTranslator;
        this.httpMessageConverters = httpMessageConverters;
    }
    /**
     * 在访问过来的时候检测是否为OPTIONS请求，如果是就直接返回true
     */
    @Override
    protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        if (httpRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            prepareResponse(HttpStatus.OK,request,response);
            return true;
        }
        return super.preHandle(request,response);
    }

    @Override
    protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
        HashMap<String, String> loginReqParams = parseLoginFormParams(request);
        String username = loginReqParams.get(this.getUsernameParam());
        String password = loginReqParams.get(this.getPasswordParam());
        String rememberMeStr = loginReqParams.get(this.getRememberMeParam());
        String loginType = loginReqParams.get("loginType");
        String openId = loginReqParams.get("openid");
        String unionId = loginReqParams.get("unionid");
        String nickname = loginReqParams.get("nickName");
        String avatar = loginReqParams.get("avatar");
        String unionType = loginReqParams.get("unionType");
        String refreshToken = loginReqParams.get("refreshToken");
        String appId = loginReqParams.get("appid");
        Boolean rememberMe = rememberMeStr == null ? false : Boolean.valueOf(rememberMeStr);
        String host = getHost(request);
        return new DefaultUserToken(username, password, rememberMe, host,
                loginType,openId,unionId,avatar,nickname,unionType,refreshToken,appId);
    }

    protected <T> T parseLoginFormParams(ServletRequest request) {
        try {
            String reqBody = StreamUtils.copyToString(request.getInputStream(), Charset.forName(request.getCharacterEncoding()));
            return (T)JSONObject.parseObject(reqBody, HashMap.class);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
        boolean accessFlag = !isLoginRequest(request, response) && getSubject(request, response).isAuthenticated();
        return accessFlag;
    }

    @Override
    protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
        HttpServletResponse resp = (HttpServletResponse) response;
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.flushBuffer();
    }

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {
        HttpServletResponse resp = this.prepareResponse(this.loginSuccessHttpStatus, request, response);
        ResponseEntity result = null;
        if (this.loginResultProvider != null)
            result = this.loginResultProvider.handleSuccessResult(token, subject, (HttpServletRequest) request);
        this.flushResponseMessage(resp, result);
        return false;
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {
        HttpServletResponse resp = this.prepareResponse(this.loginFaildHttpStatus, request, response);
        try {
            ResponseEntity problemEntity = exceptionTranslator.create(
                    new HttpStatusAdapter(this.loginFaildHttpStatus), e,
                    new ServletWebRequest((HttpServletRequest) request));
            if (this.loginResultProvider != null)
                problemEntity = this.loginResultProvider.handleFailureResult(problemEntity, token,
                        this.getSubject(request, response), (HttpServletRequest) request);
            this.flushResponseMessage((HttpServletResponse) response, problemEntity);
        } catch (IOException ex) {
            ReflectionUtils.rethrowRuntimeException(ex);
        }
        return false;
    }

    protected <T> void flushResponseMessage(HttpServletResponse response, ResponseEntity<T> responseEntity) throws IOException {
        Assert.notNull(response, "Parameter response is required!");
        try (ServletServerHttpResponse resp = new ServletServerHttpResponse(response)) {
            if (responseEntity == null) {
                resp.close();
                return;
            }
            final T body = responseEntity.getBody();
            final MediaType mediaType = responseEntity.getHeaders().getContentType();
            resp.getHeaders().addAll(responseEntity.getHeaders());
            resp.getHeaders().setContentType(mediaType);
            resp.setStatusCode(responseEntity.getStatusCode());
            if (body == null) {
                resp.close();
                return;
            }
            Class<?> type = GenericTypeResolver.resolveTypeArgument(responseEntity.getClass(), ResponseEntity.class);
            for (HttpMessageConverter converter : this.httpMessageConverters.getConverters()) {
                GenericHttpMessageConverter c = null;
                if (converter instanceof GenericHttpMessageConverter)
                    c = (GenericHttpMessageConverter) converter;
                if (type != null && c != null && c.canWrite(type, body.getClass(), mediaType)) {
                    c.write(body, type, mediaType, resp);
                    break;
                } else if (converter.canWrite(body.getClass(), mediaType)) {
                    converter.write(body, mediaType, resp);
                    break;
                }
            }
        }
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

    public void setLoginResultProvider(LoginResultProvider loginResultProvider) {
        this.loginResultProvider = loginResultProvider;
    }

    public void setLoginFaildHttpStatus(HttpStatus loginFaildHttpStatus) {
        this.loginFaildHttpStatus = loginFaildHttpStatus;
    }

    public void setLoginSuccessHttpStatus(HttpStatus loginSuccessHttpStatus) {
        this.loginSuccessHttpStatus = loginSuccessHttpStatus;
    }
}
