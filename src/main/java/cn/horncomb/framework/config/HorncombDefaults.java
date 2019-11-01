package cn.horncomb.framework.config;

/**
 * Horncomb Framework default configuration.
 */
public interface HorncombDefaults {
    interface Logging {
        /**
         * 启用JSON格式
         */
        boolean useJsonFormat = true;

        /**
         * Logstash配置.
         */
        interface Logstash {
            boolean enabled = false;
            String host = "localhost";
            int port = 5000;
            int queueSize = 512;
        }
    }

    interface Security {
        interface Password {
            String algorithm = "MD5"; // 默认密码加密算法
            boolean useSalt = true;
            int saltHashIterations = 1024; // Math
        }
        interface Authentication {
            interface Jwt {
                String secret = null;
                String base64Secret = null;
                /**
                 * jwt token 有效期.
                 */
                long tokenValidityInSeconds = 1800; // 0.5 hour
                long tokenValidityInSecondsForRememberMe = 2592000; // 30 hours;
            }
        }
    }

    interface Web {
        interface Rest {
            /**
             * RESTful API 默认路径.
             */
            String apiBaseUrl = "/api";
        }
    }
}
