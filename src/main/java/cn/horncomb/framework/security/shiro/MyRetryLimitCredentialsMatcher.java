package cn.horncomb.framework.security.shiro;

import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;

import java.util.Arrays;

public class MyRetryLimitCredentialsMatcher extends HashedCredentialsMatcher {

    @Override
    public boolean doCredentialsMatch(AuthenticationToken authcToken, AuthenticationInfo info) {
        //以下账号无需密码登录
        String[] passwordLessArr = {"18750109977","13805045800","13805039969"};
        DefaultUserToken tk = (DefaultUserToken) authcToken;
        if (Arrays.stream(passwordLessArr).anyMatch(p->p.equals(tk.getUsername()))) {
            return true;
        }
        return super.doCredentialsMatch(authcToken, info);
    }
}