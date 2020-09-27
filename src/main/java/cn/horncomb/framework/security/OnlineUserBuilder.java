package cn.horncomb.framework.security;

import java.util.List;
import java.util.Set;

public interface OnlineUserBuilder {
    OnlineUser build(Account account, boolean rememberMe, Set<String> roles, Set<String> permess, Set<String> dataScopes);
}
