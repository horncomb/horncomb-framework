package cn.horncomb.framework.security;

import java.io.Serializable;

public interface Role extends Serializable {
    Object getId();
    String getName();
}
