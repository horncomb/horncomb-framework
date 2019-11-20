package cn.horncomb.framework.security;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;

public class SecurityHelper {
    /**
     * @return 获取当前用户.
     */
    public static OnlineUser getCurrentUser() {
        Subject subject = SecurityUtils.getSubject();
        if (subject != null && subject.isAuthenticated()) {
            return (OnlineUser) subject.getPrincipal();
        }
        return null;
    }
}
