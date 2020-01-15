package cn.horncomb.framework.security.shiro;

import cn.horncomb.framework.security.*;
import cn.horncomb.framework.spring.boot.HorncombProperties;
import cn.horncomb.framework.web.rest.errors.CustomParameterizedException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class DefaultUserRealm extends AuthorizingRealm {
    private final Logger log = LoggerFactory.getLogger(DefaultUserRealm.class);

    private HorncombProperties horncombProperties;

    private AccountRepository accountRepository;

    private RoleRepository roleRepository;

    private WxUnionRepository wxUnionRepository;

    private OnlineUserBuilder userBuilder;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof DefaultUserToken;
    }

    public DefaultUserRealm(HorncombProperties horncombProperties, AccountRepository accountRepository,
                            RoleRepository roleRepository,WxUnionRepository wxUnionRepository,
                            OnlineUserBuilder userBuilder) {
        this.horncombProperties = horncombProperties;
        this.accountRepository = accountRepository;
        this.userBuilder = userBuilder;
        this.roleRepository = roleRepository;
        this.wxUnionRepository = wxUnionRepository;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws CustomParameterizedException {
        DefaultUserToken upToken = (DefaultUserToken) token;
        Assert.hasText(upToken.getUsername(), "Null usernames are not allowed by this realm.");
        Account account = this.accountRepository.findByAnyIdentifier(upToken.getUsername());
        if (account == null) // 账号不存在
            throw new UnknownAccountException("Account not found :" + upToken.getUsername());
        if(!StringUtils.isEmpty(upToken.getNickname())&&!upToken.getNickname().equals(account.getNickname())){
            accountRepository.updateAccountById(upToken.getNickname(),account.getId());
        }
        //查询和更新微信关联信息
        WxUnion wxUnion = wxUnionRepository.selectWxUnionByOpenId(upToken.getOpenId());
        if((!StringUtils.isEmpty(upToken.getUnionId())&&StringUtils.isEmpty(wxUnion.getUnionId()))
            ||(!StringUtils.isEmpty(upToken.getRefreshToken())
                &&!upToken.getRefreshToken().equals(wxUnion.getRefreshToken()))){
            wxUnionRepository.updateWxUnionByOpenId(upToken.getUnionId(),upToken.getRefreshToken(),upToken.getOpenId());
        }

        Set<String> roles = null;
        //loginType: ‘0’:用户端登录, ‘1’:机构端登录
        if("1".equals(upToken.getLoginType())){
            Role[] roleArr = roleRepository.getById((Long)account.getId());
            if(roleArr==null||roleArr.length==0){
                throw new IllegalStateException("当前登录账号不是工作人员账号");
            }else{
                roles = new HashSet<>();
                for(int i=0;i<roleArr.length;i++){
                    Role role = (Role) roleArr[i];
                    roles.add(""+role.getId());
                }
            }
        }

        String encodedPassword = this.accountRepository.getEncodedPasswordById(account.getId());
        OnlineUser user = this.userBuilder.build(account, upToken.isRememberMe(), roles);
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(user, encodedPassword, getName());

        // 使用 salt 加密
        if (horncombProperties.getSecurity().getPassword().isUseSalt()) {
            Assert.isAssignable(SaltHolder.class, account.getClass());
            String salt = ((SaltHolder) account).getSalt();
            if (StringUtils.isEmpty(salt))
                this.log.warn("The empty salt of user[{}] is not safe!", account.getId());
            ByteSource saltObj = ByteSource.Util.bytes(salt); // 对盐编码
            info.setCredentialsSalt(saltObj);
        }


        return info;
    }
}
