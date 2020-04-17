package cn.horncomb.framework.security;

import java.io.Serializable;

public interface Account extends Serializable {
    Object getId();
    String getName();
}
