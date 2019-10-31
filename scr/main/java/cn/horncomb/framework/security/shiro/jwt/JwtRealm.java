package cn.horncomb.framework.security.shiro.jwt;

import cn.horncomb.framework.security.Account;
import cn.horncomb.framework.security.*;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JwtRealm extends AuthorizingRealm {
    private final Logger log = LoggerFactory.getLogger(JwtRealm.class);

    private final AccountRepository accountRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final OnlineUserBuilder userBuilder;

    public JwtRealm(AccountRepository accountRepository, JwtTokenProvider jwtTokenProvider, OnlineUserBuilder userBuilder) {
        this.accountRepository = accountRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userBuilder = userBuilder;
    }

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) {
        JwtToken jwtToken = (JwtToken) token;
        try {
            this.jwtTokenProvider.validateToken(jwtToken.getToken());
        } catch (ExpiredJwtException e) {
            log.trace("Expired JWT token trace: {}", e);
            throw new ExpiredCredentialsException("Expired JWT token", e);
        } catch (Exception e) {
            throw new IncorrectCredentialsException(e);
        }

        String accountId = this.jwtTokenProvider.resolveAuthenticationSubject(jwtToken.getToken());
        Account account = this.accountRepository.getById(accountId);
        if (account == null) throw new UnknownAccountException();
        OnlineUser user = this.userBuilder.build(account, false, null);
        return new SimpleAccount(user, jwtToken.getToken(), getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return new SimpleAuthorizationInfo(((OnlineUser) principals.getPrimaryPrincipal()).getRoles());
    }
}
