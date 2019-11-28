package cn.horncomb.framework.security.shiro;

import cn.horncomb.framework.security.Account;
import cn.horncomb.framework.security.AccountRepository;
import cn.horncomb.framework.security.OnlineUser;
import cn.horncomb.framework.security.OnlineUserBuilder;
import cn.horncomb.framework.spring.boot.HorncombProperties;
import cn.horncomb.framework.web.rest.errors.CustomParameterizedException;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.util.ByteSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class DefaultUserRealm extends AuthorizingRealm {
    private final Logger log = LoggerFactory.getLogger(DefaultUserRealm.class);

    private HorncombProperties horncombProperties;

    private AccountRepository accountRepository;

    private OnlineUserBuilder userBuilder;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof UsernamePasswordToken;
    }

    public DefaultUserRealm(HorncombProperties horncombProperties, AccountRepository accountRepository, OnlineUserBuilder userBuilder) {
        this.horncombProperties = horncombProperties;
        this.accountRepository = accountRepository;
        this.userBuilder = userBuilder;
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws CustomParameterizedException {
        UsernamePasswordToken upToken = (UsernamePasswordToken) token;
        Assert.hasText(upToken.getUsername(), "Null usernames are not allowed by this realm.");
        Account account = this.accountRepository.findByAnyIdentifier(upToken.getUsername());
        if (account == null) // 账号不存在
            throw new UnknownAccountException("Account not found :" + upToken.getUsername());
        String encodedPassword = this.accountRepository.getEncodedPasswordById(account.getId());

        OnlineUser user = this.userBuilder.build(account, upToken.isRememberMe(), null);
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
