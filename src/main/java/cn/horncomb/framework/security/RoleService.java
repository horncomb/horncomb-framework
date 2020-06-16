package cn.horncomb.framework.security;

import java.util.List;

public interface RoleService {

    /**
     * 取得用户角色
     * @param userId
     */
    Role[] getRolesByUserId(Object userId);

    /**
     * 取得用户权限控制点
     * @param userId
     */
    List<String> getPermesByUserId(Object userId);

    /**
     * 取得用户数据权限
     * @param userId
     */
    List<String> getDataScopesByUserId(Object userId);
}
