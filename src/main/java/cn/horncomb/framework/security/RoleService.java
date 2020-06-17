package cn.horncomb.framework.security;

import java.util.List;

public interface RoleService {

    Role[] getRolesByUserId(Object userId);

    List<String> getPermesByUserId(Object userId);

    List<String> getDataScopesByUserId(Object userId);
}
