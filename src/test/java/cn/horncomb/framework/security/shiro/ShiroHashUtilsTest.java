package cn.horncomb.framework.security.shiro;

import org.apache.shiro.codec.CodecSupport;
import org.apache.shiro.codec.Hex;
import org.apache.shiro.crypto.hash.Hash;
import org.apache.shiro.crypto.hash.SimpleHash;
import org.apache.shiro.util.ByteSource;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.util.UUID;


public class ShiroHashUtilsTest {
    private final Logger log = LoggerFactory.getLogger(ShiroHashUtilsTest.class);

    static String source = "888888";

    @Test
    public void testHash() {
        for (int i = 0; i < 5; i++) {
            Hash hash = ShiroHashUtils.hashWithAutoSalt(source, "MD5");
            log.info("'{}', '{}'", hash.toString(),
                    CodecSupport.toString(hash.getSalt().getBytes()));
        }
    }


    @Test
    public void testHashWithSalt() throws UnsupportedEncodingException {
        String expected = "4e3f2cf42bf12218a45247ca0a55212a";
        String salt = "c68b6d134f9c4ff28793df057662c077";
        Hash hash = ShiroHashUtils.hashWithSalt(source, salt, "MD5");
//        log.info("result: {}, source: {}, salt: {}", hash.toString(), source, hash.getSalt());
        Assert.assertEquals(expected, hash.toString());
    }
}
