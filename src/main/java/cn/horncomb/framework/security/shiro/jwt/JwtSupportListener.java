package cn.horncomb.framework.security.shiro.jwt;

import cn.horncomb.framework.security.JwtTokenProvider;
import cn.horncomb.framework.security.OnlineUser;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationListener;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletResponse;

public class JwtSupportListener implements AuthenticationListener {
    private final Logger log = LoggerFactory.getLogger(JwtSupportListener.class);

    private final JwtTokenProvider jwtTokenProvider;

    public JwtSupportListener(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void onSuccess(AuthenticationToken token, AuthenticationInfo info) {
        if (!(token instanceof JwtToken)) { // 非JwtToken的认证
            Subject subject = SecurityUtils.getSubject();
            HttpServletResponse response = WebUtils.getHttpResponse(subject);
            OnlineUser user = (OnlineUser) info.getPrincipals().getPrimaryPrincipal();
            String jwtToken = this.jwtTokenProvider.createToken(user);
            response.setHeader(this.jwtTokenProvider.getAuthorizationHeader(), jwtToken);
            if (log.isTraceEnabled())
                log.trace("Token added to response: {}", jwtToken);
            else
                log.debug("Token added to response");
        }
    }

    @Override
    public void onFailure(AuthenticationToken token, AuthenticationException ae) {
        // do nothing
    }

    @Override
    public void onLogout(PrincipalCollection principals) {
        // TODO 将rememberMe的token置为无效
    }
}
