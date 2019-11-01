package cn.horncomb.framework.data.mybatis.plus.page;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.core.MybatisDefaultParameterHandler;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.parser.ISqlParser;
import com.baomidou.mybatisplus.core.parser.SqlInfo;
import com.baomidou.mybatisplus.core.toolkit.ArrayUtils;
import com.baomidou.mybatisplus.core.toolkit.ExceptionUtils;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.handlers.AbstractSqlParserHandler;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectFactory;
import com.baomidou.mybatisplus.extension.plugins.pagination.DialectModel;
import com.baomidou.mybatisplus.extension.toolkit.JdbcUtils;
import com.baomidou.mybatisplus.extension.toolkit.SqlParserUtils;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.scripting.defaults.DefaultParameterHandler;
import org.apache.ibatis.session.Configuration;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.stream.Collectors;

@Intercepts({@Signature(
        type = StatementHandler.class,
        method = "prepare",
        args = {Connection.class, Integer.class}
)})
public class SpringPaginationInterceptor extends AbstractSqlParserHandler implements Interceptor {
    private ISqlParser countSqlParser;
    private boolean overflow = false;
    private long limit = 500L;
    private String dialectType;
    private String dialectClazz;

    public SpringPaginationInterceptor() {
    }

    public static String concatOrderBy(String originalSql, IPage<?> page, boolean orderBy) {
        Pageable pageParam = null;
        if (!orderBy) return originalSql;
        if (page instanceof Pageable) {
            pageParam = (Pageable) page;
            if (pageParam.getSort() == null || pageParam.getSort().isUnsorted())
                return originalSql;

            StringBuilder buildSql = new StringBuilder(originalSql);
            buildSql.append(" ORDER BY ");

            Iterator<Sort.Order> i = pageParam.getSort().iterator();
            Sort.Order order = i.next();
            buildSql.append(order.getProperty())
                    .append(" ")
                    .append(order.getDirection().toString());
            while (i.hasNext()) {
                order = i.next();
                buildSql.append(", ")
                        .append(order.getProperty())
                        .append(" ")
                        .append(order.getDirection().toString());
            }
            return buildSql.toString();
        } else {
            if (!ArrayUtils.isNotEmpty(page.ascs()) && !ArrayUtils.isNotEmpty(page.descs()))
                return originalSql;

            StringBuilder buildSql = new StringBuilder(originalSql);
            String ascStr = concatOrderBuilder(page.ascs(), " ASC");
            String descStr = concatOrderBuilder(page.descs(), " DESC");
            if (StringUtils.isNotEmpty(ascStr) && StringUtils.isNotEmpty(descStr)) {
                ascStr = ascStr + ", ";
            }

            if (StringUtils.isNotEmpty(ascStr) || StringUtils.isNotEmpty(descStr)) {
                buildSql.append(" ORDER BY ").append(ascStr).append(descStr);
            }

            return buildSql.toString();
        }
    }

    private static String concatOrderBuilder(String[] columns, String orderWord) {
        return ArrayUtils.isNotEmpty(columns) ? (String) Arrays.stream(columns).filter(StringUtils::isNotEmpty).map((i) -> {
            return i + orderWord;
        }).collect(Collectors.joining(",")) : "";
    }

    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) PluginUtils.realTarget(invocation.getTarget());
        MetaObject metaObject = SystemMetaObject.forObject(statementHandler);
        this.sqlParser(metaObject);
        MappedStatement mappedStatement = (MappedStatement) metaObject.getValue("delegate.mappedStatement");
        if (SqlCommandType.SELECT == mappedStatement.getSqlCommandType() && StatementType.CALLABLE != mappedStatement.getStatementType()) {
            BoundSql boundSql = (BoundSql) metaObject.getValue("delegate.boundSql");
            Object paramObj = boundSql.getParameterObject();
            IPage<?> pageParam = null;
            if (paramObj instanceof IPage) {
                pageParam = (IPage) paramObj;
            } else if (paramObj instanceof Map) {
                Iterator i = ((Map) paramObj).values().iterator();

                while (i.hasNext()) {
                    Object arg = i.next();
                    if (arg instanceof IPage) {
                        pageParam = (IPage) arg;
                        break;
                    }
                }
            }

            if (null != pageParam && pageParam.getSize() >= 0L) {
                if (this.limit > 0L && this.limit <= pageParam.getSize()) {
                    pageParam.setSize(this.limit);
                }

                String originalSql = boundSql.getSql();
                Connection connection = (Connection) invocation.getArgs()[0];
                DbType dbType = StringUtils.isNotEmpty(this.dialectType) ? DbType.getDbType(this.dialectType) : JdbcUtils.getDbType(connection.getMetaData().getURL());
                boolean orderBy = true;
                if (pageParam.isSearchCount()) {
                    SqlInfo sqlInfo = SqlParserUtils.getOptimizeCountSql(pageParam.optimizeCountSql(), this.countSqlParser, originalSql);
                    orderBy = sqlInfo.isOrderBy();
                    this.queryTotal(this.overflow, sqlInfo.getSql(), mappedStatement, boundSql, pageParam, connection);
                    if (pageParam.getTotal() <= 0L) {
                        return null;
                    }
                }

                String buildSql = concatOrderBy(originalSql, pageParam, orderBy);
                DialectModel model = DialectFactory.buildPaginationSql(pageParam, buildSql, dbType, this.dialectClazz);
                Configuration configuration = mappedStatement.getConfiguration();
                List<ParameterMapping> mappings = new ArrayList(boundSql.getParameterMappings());
                Map<String, Object> additionalParameters = (Map) metaObject.getValue("delegate.boundSql.additionalParameters");
                model.consumers(mappings, configuration, additionalParameters);
                metaObject.setValue("delegate.boundSql.sql", model.getDialectSql());
                metaObject.setValue("delegate.boundSql.parameterMappings", mappings);
                return invocation.proceed();
            } else {
                return invocation.proceed();
            }
        } else {
            return invocation.proceed();
        }
    }

    protected void queryTotal(boolean overflowCurrent, String sql, MappedStatement mappedStatement, BoundSql boundSql, IPage<?> page, Connection connection) {
        try {
            PreparedStatement statement = connection.prepareStatement(sql);
            Throwable error = null;

            try {
                DefaultParameterHandler parameterHandler = new MybatisDefaultParameterHandler(mappedStatement, boundSql.getParameterObject(), boundSql);
                parameterHandler.setParameters(statement);
                long total = 0L;
                ResultSet resultSet = statement.executeQuery();
                Throwable total_error = null;

                try {
                    if (resultSet.next()) {
                        total = resultSet.getLong(1);
                    }
                } catch (Throwable ex) {
                    total_error = ex;
                    throw ex;
                } finally {
                    if (resultSet != null) {
                        if (total_error != null) {
                            try {
                                resultSet.close();
                            } catch (Throwable ex) {
                                total_error.addSuppressed(ex);
                            }
                        } else {
                            resultSet.close();
                        }
                    }

                }

                page.setTotal(total);
                long pages = page.getPages();
                if (overflowCurrent && page.getCurrent() > pages) {
                    page.setCurrent(1L);
                }
            } catch (Throwable ex) {
                error = ex;
                throw ex;
            } finally {
                if (statement != null) {
                    if (error != null) {
                        try {
                            statement.close();
                        } catch (Throwable ex) {
                            error.addSuppressed(ex);
                        }
                    } else {
                        statement.close();
                    }
                }

            }

        } catch (Exception var42) {
            throw ExceptionUtils.mpe("Error: Method queryTotal execution error of sql : \n %s \n", var42, new Object[]{sql});
        }
    }

    public Object plugin(Object target) {
        return target instanceof StatementHandler ? Plugin.wrap(target, this) : target;
    }

    public void setProperties(Properties prop) {
        String dialectType = prop.getProperty("dialectType");
        String dialectClazz = prop.getProperty("dialectClazz");
        if (StringUtils.isNotEmpty(dialectType)) {
            this.dialectType = dialectType;
        }

        if (StringUtils.isNotEmpty(dialectClazz)) {
            this.dialectClazz = dialectClazz;
        }

    }

    public SpringPaginationInterceptor setCountSqlParser(final ISqlParser countSqlParser) {
        this.countSqlParser = countSqlParser;
        return this;
    }

    public SpringPaginationInterceptor setOverflow(final boolean overflow) {
        this.overflow = overflow;
        return this;
    }

    public SpringPaginationInterceptor setLimit(final long limit) {
        this.limit = limit;
        return this;
    }

    public SpringPaginationInterceptor setDialectType(final String dialectType) {
        this.dialectType = dialectType;
        return this;
    }

    public SpringPaginationInterceptor setDialectClazz(final String dialectClazz) {
        this.dialectClazz = dialectClazz;
        return this;
    }
}
