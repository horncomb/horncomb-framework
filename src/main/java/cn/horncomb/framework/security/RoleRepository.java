package cn.horncomb.framework.security;

public interface RoleRepository {

    Role[] getById(Object userId);

}
