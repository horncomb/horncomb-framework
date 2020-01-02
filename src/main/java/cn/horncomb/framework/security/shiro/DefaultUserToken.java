package cn.horncomb.framework.security.shiro;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.shiro.authc.AuthenticationToken;

@Data
@Accessors(chain = true)
public class DefaultUserToken implements AuthenticationToken {
    private String username;
    private String password;
    private boolean rememberMe;
    private String host;
    private String loginType;
    private String unionId;
    private String nickname;

    public DefaultUserToken(String username, String password, boolean rememberMe, String host, String loginType,String unionId,String nickname) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
        this.host = host;
        this.loginType = loginType;
        this.unionId = unionId;
        this.nickname = nickname;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }

    @Override
    public Object getCredentials() {
        return password;
    }
}
