package cn.horncomb.framework.security;

import java.util.Set;

public interface OnlineUserBuilder {
    OnlineUser build(Account account, boolean rememberMe, Set<String> roles);
}
