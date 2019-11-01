package cn.horncomb.framework.spring.boot;

import cn.horncomb.framework.config.HorncombConstants;
import cn.horncomb.framework.config.HorncombDefaults;
import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.web.cors.CorsConfiguration;

/**
 * Spring-boot autoconfig.
 */
@ConfigurationProperties(prefix = HorncombConstants.SPRING_CONFIG_PREFIX, ignoreUnknownFields = false)
@PropertySource(value = "classpath:git.properties", ignoreResourceNotFound = true)
@PropertySource(value = "classpath:META-INF/build-info.properties", ignoreResourceNotFound = true)
@lombok.Data
public class HorncombProperties {
    private final Logging logging = new Logging();

    /**
     * 日志配置.
     */
    @lombok.Data
    public static class Logging {

        private boolean useJsonFormat = HorncombDefaults.Logging.useJsonFormat;

        private final Logstash logstash = new Logstash();

        @lombok.Data
        public static class Logstash {

            private boolean enabled = HorncombDefaults.Logging.Logstash.enabled;

            private String host = HorncombDefaults.Logging.Logstash.host;

            private int port = HorncombDefaults.Logging.Logstash.port;

            private int queueSize = HorncombDefaults.Logging.Logstash.queueSize;
        }
    }

    private final Data data = new Data();

    @lombok.Data
    public static class Data {
        private final Web web = new Web();

        @lombok.Data
        public static class Web {
            private final SpringDataWebProperties.Pageable pageable = new SpringDataWebProperties.Pageable();
        }
    }

    private final Web web = new Web();

    @lombok.Data
    private static class Web {
        private final Rest version = new Rest();

        @lombok.Data
        private static class Rest {
            private String appApiVersion;
        }
    }


    private final Security security = new Security();

    @lombok.Data
    public static class Security {

        private final Password password = new Password();

        @lombok.Data
        public static class Password {
            private String algorithm = HorncombDefaults.Security.Password.algorithm;
            private boolean useSalt = HorncombDefaults.Security.Password.useSalt;
            private int saltHashInteractions = HorncombDefaults.Security.Password.saltHashIterations;
        }

        private final Authentication authentication = new Authentication();

        @lombok.Data
        public static class Authentication {

            private final Jwt jwt = new Jwt();

            @lombok.Data
            public static class Jwt {

                private String secret = HorncombDefaults.Security.Authentication.Jwt.secret;

                private String base64Secret = HorncombDefaults.Security.Authentication.Jwt.base64Secret;

                private long tokenValidityInSeconds = HorncombDefaults.Security.Authentication.Jwt
                        .tokenValidityInSeconds;

                private long tokenValidityInSecondsForRememberMe = HorncombDefaults.Security.Authentication.Jwt
                        .tokenValidityInSecondsForRememberMe;
            }
        }
    }

    /**
     * 跨域配置.
     *
     * @return
     */
    private final CorsConfiguration cors = new CorsConfiguration();
}
