package cn.horncomb.framework.security.shiro;

import cn.horncomb.framework.security.*;
import cn.horncomb.framework.spring.boot.HorncombProperties;
import cn.horncomb.framework.web.rest.errors.CustomParameterizedException;
import cn.horncomb.framework.web.rest.errors.UnauthorizedAlertException;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultUserRealm extends AuthorizingRealm {
    private final Logger log = LoggerFactory.getLogger(DefaultUserRealm.class);

    private HorncombProperties horncombProperties;

    private AccountRepository accountRepository;

    private RoleService roleService;

    private WxUnionService wxUnionService;

    private OnlineUserBuilder userBuilder;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof DefaultUserToken;
    }

    public DefaultUserRealm(HorncombProperties horncombProperties, AccountRepository accountRepository,
                            RoleService roleService, WxUnionService wxUnionService,
                            OnlineUserBuilder userBuilder) {
        this.horncombProperties = horncombProperties;
        this.accountRepository = accountRepository;
        this.userBuilder = userBuilder;
        this.roleService = roleService;
        this.wxUnionService = wxUnionService;
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
        if (account == null){
            // 账号不存在
            throw new UnknownAccountException("Account not found :" + upToken.getUsername());
        }

        Set<String> roles = null;
        //操作权限
        List<String> permess = new ArrayList<>();
        //数据范围
        List<String> dataScopes = new ArrayList<>();
        //loginType: ‘0’:用户端登录, ‘1’:机构端登录，‘2’：管理平台登录
        if("1".equals(upToken.getLoginType())||"2".equals(upToken.getLoginType())){
            //机构端和管理平台登录权限：机构用户
            Role[] roleArr = roleService.getRolesByUserId((Long)account.getId());
            permess = roleService.getPermesByUserId((Long)account.getId());
            dataScopes = roleService.getDataScopesByUserId((Long)account.getId());
            boolean loginFlag = false;
            if(roleArr!=null&&roleArr.length>0){
                loginFlag = true;
            }
            if(loginFlag){
                roles = new HashSet<>();
                for(int i=0;i<roleArr.length;i++){
                    Role role = (Role) roleArr[i];
                    roles.add(""+role.getId());
                }
            }else{
                throw new UnauthorizedAlertException("当前登录账号不是机构人员账号，无法登录！");
            }
        }
        //刷新用户和微信数据
        wxUnionService.refreshAccountAndWxInfo(account,upToken);

        String encodedPassword = this.accountRepository.getEncodedPasswordById(account.getId());
        OnlineUser user = this.userBuilder.build(account, upToken.isRememberMe(), roles, permess, dataScopes);
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
