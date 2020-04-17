package cn.horncomb.framework.security;

import cn.horncomb.framework.security.shiro.DefaultUserToken;

public interface WxUnionService {

    public void refreshAccountAndWxInfo(Account account, DefaultUserToken upToken);
}
