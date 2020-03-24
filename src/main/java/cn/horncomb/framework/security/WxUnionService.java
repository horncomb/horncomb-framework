package cn.horncomb.framework.security;

public interface WxUnionService {

    public WxUnion selectWxUnionByOpenId(String openId);
    public void updateWxUnionByOpenId(String unionId,Long userId,String openId);
    public void insertWxUnion(String userId,String unionId,String openId,String appId,String unionType);
}
