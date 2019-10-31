package cn.horncomb.framework.security;

import java.io.Serializable;

public interface OnlineUser extends Serializable {
    String getName();

    boolean isRememberMe();

    Account getAccount();

    java.util.Set<String> getRoles();
}
