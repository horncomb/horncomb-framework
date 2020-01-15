package cn.horncomb.framework.security;

import java.io.Serializable;

public interface WxUnion extends Serializable {
    String getOpenId();
    String getUnionId();
    String getUnionType();
    String getRefreshToken();
    Long getUserId();
}
