package cn.horncomb.framework.security;

import cn.horncomb.framework.spring.boot.HorncombProperties;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.stream.Collectors;


public class JwtTokenProvider {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public final static String BEAR_TOKEN_PREFIX = "Bearer";

    private final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String ROLES_KEY = "roles";

    private Key jwtSigningKey;

    private long tokenValidityInMilliseconds;

    private long tokenValidityInMillisecondsForRememberMe;

    private String authorizationHeader = AUTHORIZATION_HEADER;

    private String authzScheme = BEAR_TOKEN_PREFIX;

    private final HorncombProperties horncombProperties;

    public JwtTokenProvider(HorncombProperties horncombProperties) {
        this.horncombProperties = horncombProperties;
        this.init();
    }

    public void init() {
        byte[] keyBytes;
        String secret = horncombProperties.getSecurity().getAuthentication().getJwt().getSecret();
        if (!StringUtils.isEmpty(secret)) {
            log.warn("Warning: the JWT jwtSigningKey used is not Base64-encoded. " +
                    "We recommend using the `Horncomb.security.authentication.jwt.base64-secret` jwtSigningKey for optimum security.");
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        } else {
            log.debug("Using a Base64-encoded JWT secret jwtSigningKey");
            keyBytes = Decoders.BASE64.decode(horncombProperties.getSecurity().getAuthentication().getJwt().getBase64Secret());
        }
        this.jwtSigningKey = Keys.hmacShaKeyFor(keyBytes);
        this.tokenValidityInMilliseconds =
                1000 * horncombProperties.getSecurity().getAuthentication().getJwt().getTokenValidityInSeconds();
        this.tokenValidityInMillisecondsForRememberMe =
                1000 * horncombProperties.getSecurity().getAuthentication().getJwt()
                        .getTokenValidityInSecondsForRememberMe();
    }

    public static String buildTokenWithScheme(String scheme, String simpleToken) {
        return StringUtils.hasText(scheme) ? scheme + " " + simpleToken : simpleToken;
    }

    public static String parseSimpleToken(String scheme, String token) {
        String simpleToken = null;
        scheme = scheme == null ? "" : scheme;
        if (StringUtils.hasText(scheme) && token.length() > scheme.length())
            simpleToken = token.substring(scheme.length());
        return StringUtils.trimWhitespace(simpleToken);
    }

    public String createToken(OnlineUser user) {
        Assert.notNull(user, "Parameter user is required!");
        String authorities = user.getRoles().stream()
//                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        long now = (new Date()).getTime();
        Date validity;
        boolean rememberMe = user.isRememberMe();
        if (rememberMe) {
            validity = new Date(now + this.tokenValidityInMillisecondsForRememberMe);
        } else {
            validity = new Date(now + this.tokenValidityInMilliseconds);
        }

        String rawToken = Jwts.builder()
                .setSubject(String.valueOf(user.getAccount().getId()))
                .claim(ROLES_KEY, authorities)
                .signWith(jwtSigningKey, SignatureAlgorithm.HS512)
                .setExpiration(validity)
                .compact();

        return buildTokenWithScheme(this.getAuthzScheme(), rawToken);
    }

    public void validateToken(String token) {
        Assert.hasText(token, "Parameter token is required!");
        try {
            Jwts.parser().setSigningKey(jwtSigningKey).parseClaimsJws(parseSimpleToken(this.getAuthzScheme(), token));
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.trace("Invalid JWT signature trace: {}", e);
            throw e;
        } catch (ExpiredJwtException e) {
            log.trace("Expired JWT token trace: {}", e);
            throw e;
        } catch (UnsupportedJwtException e) {
            log.trace("Unsupported JWT token trace: {}", e);
            throw e;
        } catch (IllegalArgumentException e) {
            log.trace("JWT token compact of handler are invalid trace: {}", e);
            throw e;
        }
    }

    public String resolveAuthenticationSubject(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(jwtSigningKey)
                .parseClaimsJws(parseSimpleToken(this.getAuthzScheme(), token))
                .getBody();
        return claims.getSubject();
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getAuthzScheme() {
        return authzScheme;
    }

    public void setAuthzScheme(String authzScheme) {
        this.authzScheme = authzScheme;
    }
}
