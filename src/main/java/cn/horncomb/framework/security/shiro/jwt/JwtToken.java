package cn.horncomb.framework.security.shiro.jwt;

import org.apache.shiro.authc.AuthenticationToken;

public class JwtToken implements AuthenticationToken {
    private String token;

    public JwtToken(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public Object getPrincipal() {
        return getToken();
    }

    @Override
    public Object getCredentials() {
        return getToken();
    }
}
