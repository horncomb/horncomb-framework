package cn.horncomb.framework.security.shiro;

import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.springframework.util.StringUtils;

import java.util.UUID;

public abstract class ShiroHashUtils {
    public static String generateSalt() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    public static Hash hashWithAutoSalt(String source, String algorithmName) {
        return ShiroHashUtils.hash(source, algorithmName, true);
    }

    public static Hash hash(String source, String algorithmName, boolean autoSalt) {
        if (!StringUtils.hasText(algorithmName))
            algorithmName = "MD5";
        String salt = autoSalt ? ShiroHashUtils.generateSalt() : null;
        return ShiroHashUtils.hashWithSalt(source, salt, algorithmName);
    }

    public static Hash hashWithSalt(String source, String salt, String hashAlgorithm) {
        int hashIterations = 1024;
        return new SimpleHash(hashAlgorithm, source, salt, hashIterations);
//        ByteSource saltBytes = ByteSource.Util.bytes(salt);
//        ByteSource sourceBytes = ByteSource.Util.bytes(source);
//        return new SimpleHash(hashAlgorithm, sourceBytes, saltBytes, hashIterations);
    }
}
