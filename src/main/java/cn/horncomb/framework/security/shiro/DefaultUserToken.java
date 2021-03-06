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
    private String avatar;
    private String nickname;
    private String openId;

    /**
     * 类型 mp：公众号，miniapp：小程序
     */
    private String unionType;
    /**
     * 公众号token，有效期30天
     */
    private String RefreshToken;
    private String appId;

    public DefaultUserToken(String username, String password, boolean rememberMe, String host,
                            String loginType,String openId,String unionId,String avatar,String nickname,
                            String unionType,String refreshToken,String appId) {
        this.username = username;
        this.password = password;
        this.rememberMe = rememberMe;
        this.host = host;
        this.loginType = loginType;
        this.openId = openId;
        this.unionId = unionId;
        this.avatar = avatar;
        this.nickname = nickname;
        this.unionType = unionType;
        this.RefreshToken = refreshToken;
        this.appId = appId;
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
