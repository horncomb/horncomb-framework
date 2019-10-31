package cn.horncomb.framework.security;

import java.io.Serializable;
import java.util.Set;

public interface Account extends Serializable {
    Object getId();
    String getName();
}
