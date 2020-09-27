package cn.horncomb.framework.security;

import java.io.Serializable;

public interface OnlineUser extends Serializable {
    String getId();
    String getName();

    boolean isRememberMe();

    Account getAccount();

    java.util.Set<String> getRoles();

    java.util.Set<String> getAuthorities();

    java.util.Set<String> getDataScopes();
}
