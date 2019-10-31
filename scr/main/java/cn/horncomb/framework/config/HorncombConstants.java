package cn.horncomb.framework.config;

/**
 * Horncomb Framework Constants.
 */
public interface HorncombConstants {
    String SPRING_CONFIG_PREFIX = "horncomb";

    String SPRING_PROFILE_DEVELOPMENT = "dev";
    String SPRING_PROFILE_TEST = "test";
    String SPRING_PROFILE_STAGE = "stage";
    String SPRING_PROFILE_PRODUCTION = "prod";

    // Spring profile used to disable running liquibase
    String SPRING_PROFILE_NO_LIQUIBASE = "no-liquibase";
}
