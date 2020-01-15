package cn.horncomb.framework.security;

public interface WxUnionRepository {

    public WxUnion selectWxUnionByOpenId(String openId);
    public void updateWxUnionByOpenId(String unionId,String refreshToken,String openId);
}
