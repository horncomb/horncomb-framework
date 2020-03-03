package cn.horncomb.framework.security;

public interface WxUnionService {

    public WxUnion selectWxUnionByOpenId(String openId);
    public void updateWxUnionByOpenId(String unionId,String refreshToken,String openId);
    public void insertWxUnion(String unionId,String openId,String appId,String unionType);
}
