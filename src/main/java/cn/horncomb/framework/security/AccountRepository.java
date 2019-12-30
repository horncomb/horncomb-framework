package cn.horncomb.framework.security;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

public interface AccountRepository {

    Account getById(Object userId);

    /**
     * @param identifier 账号标识，包括：id、用户名、手机号、电子邮件.
     * @return 唯一用户.
     */
    Account findByAnyIdentifier(@NotBlank Object identifier);

    /**
     * @param userId 账号id.
     * @return 经过编码的账号密码.
     */
    String getEncodedPasswordById(@NotNull Object userId);

    /**
     * 更新unionId
     * @param unionId
     * @return
     */
    int updateUnionidById(String unionId,Object userId);
}
