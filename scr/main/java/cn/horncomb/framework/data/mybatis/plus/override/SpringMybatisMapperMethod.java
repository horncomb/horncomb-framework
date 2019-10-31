package cn.horncomb.framework.data.mybatis.plus.override;

import cn.horncomb.framework.data.mybatis.plus.page.SpringPage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.override.MybatisMapperMethod;
import org.apache.ibatis.binding.BindingException;
import org.apache.ibatis.binding.MapperMethod;
import org.apache.ibatis.cursor.Cursor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.StatementType;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class SpringMybatisMapperMethod extends MybatisMapperMethod {

    private final MapperMethod.SqlCommand command;
    private final MapperMethod.MethodSignature method;

    public SpringMybatisMapperMethod(Class<?> mapperInterface, Method method, Configuration config) {
        super(mapperInterface, method, config);
        this.command = new MapperMethod.SqlCommand(config, mapperInterface, method);
        this.method = new MapperMethod.MethodSignature(config, mapperInterface, method);
    }


    public Object execute(SqlSession sqlSession, Object[] args) {
        Object result;
        switch (command.getType()) {
            case INSERT: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.insert(command.getName(), param));
                break;
            }
            case UPDATE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.update(command.getName(), param));
                break;
            }
            case DELETE: {
                Object param = method.convertArgsToSqlCommandParam(args);
                result = rowCountResult(sqlSession.delete(command.getName(), param));
                break;
            }
            case SELECT:
                if (method.returnsVoid() && method.hasResultHandler()) {
                    executeWithResultHandler(sqlSession, args);
                    result = null;
                } else if (method.returnsMany()) {
                    result = executeForMany(sqlSession, args);
                } else if (method.returnsMap()) {
                    result = executeForMap(sqlSession, args);
                } else if (method.returnsCursor()) {
                    result = executeForCursor(sqlSession, args);
                } else {
                    Object param = method.convertArgsToSqlCommandParam(args);
                    // TODO 这里下面改了
                    if (Page.class.isAssignableFrom(method.getReturnType()) && args != null
                            && Pageable.class.isAssignableFrom(args[0].getClass())) {
//                        result = ((IPage<?>) args[0]).setRecords(executeForIPage(sqlSession, args));
//                        IPage<?> ipage = (IPage<?>) args[0];
                        Pageable pageable = (Pageable) args[0];
                        IPage ipage = new SpringPage(pageable); // 替换参数，兼容IPage
                        args[0] = ipage;
                        result = new PageImpl(executeForIPage(sqlSession, args), pageable, ipage.getTotal());
                        // TODO 这里上面改了
                    } else if (IPage.class.isAssignableFrom(method.getReturnType()) && args != null
                            && IPage.class.isAssignableFrom(args[0].getClass())) {
                        result = ((IPage<?>) args[0]).setRecords(executeForIPage(sqlSession, args));
                    } else {
                        result = sqlSession.selectOne(command.getName(), param);
                        if (method.returnsOptional()
                                && (result == null || !method.getReturnType().equals(result.getClass()))) {
                            result = Optional.ofNullable(result);
                        }
                    }
                }
                break;
            case FLUSH:
                result = sqlSession.flushStatements();
                break;
            default:
                throw new BindingException("Unknown execution method for: " + command.getName());
        }
        if (result == null && method.getReturnType().isPrimitive() && !method.returnsVoid()) {
            throw new BindingException("Mapper method '" + command.getName()
                    + " attempted to return null from a method with a primitive return type (" + method.getReturnType() + ").");
        }
        return result;
    }

    /**
     * TODO IPage 专用
     */
    private <E> List<E> executeForIPage(SqlSession sqlSession, Object[] args) {
        Object param = method.convertArgsToSqlCommandParam(args);
        return sqlSession.selectList(command.getName(), param);
    }

    private Object rowCountResult(int rowCount) {
        final Object result;
        if (method.returnsVoid()) {
            result = null;
        } else if (Integer.class.equals(method.getReturnType()) || Integer.TYPE.equals(method.getReturnType())) {
            result = rowCount;
        } else if (Long.class.equals(method.getReturnType()) || Long.TYPE.equals(method.getReturnType())) {
            result = (long) rowCount;
        } else if (Boolean.class.equals(method.getReturnType()) || Boolean.TYPE.equals(method.getReturnType())) {
            result = rowCount > 0;
        } else {
            throw new BindingException("Mapper method '" + command.getName() + "' has an unsupported return type: " + method.getReturnType());
        }
        return result;
    }

    private void executeWithResultHandler(SqlSession sqlSession, Object[] args) {
        MappedStatement ms = sqlSession.getConfiguration().getMappedStatement(command.getName());
        if (!StatementType.CALLABLE.equals(ms.getStatementType())
                && void.class.equals(ms.getResultMaps().get(0).getType())) {
            throw new BindingException("method " + command.getName()
                    + " needs either a @ResultMap annotation, a @ResultType annotation,"
                    + " or a resultType attribute in XML so a ResultHandler can be used as a parameter.");
        }
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            sqlSession.select(command.getName(), param, rowBounds, method.extractResultHandler(args));
        } else {
            sqlSession.select(command.getName(), param, method.extractResultHandler(args));
        }
    }

    private <E> Object executeForMany(SqlSession sqlSession, Object[] args) {
        List<E> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectList(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectList(command.getName(), param);
        }
        // issue #510 Collections & arrays support
        if (!method.getReturnType().isAssignableFrom(result.getClass())) {
            if (method.getReturnType().isArray()) {
                return convertToArray(result);
            } else {
                return convertToDeclaredCollection(sqlSession.getConfiguration(), result);
            }
        }
        return result;
    }

    private <T> Cursor<T> executeForCursor(SqlSession sqlSession, Object[] args) {
        Cursor<T> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectCursor(command.getName(), param, rowBounds);
        } else {
            result = sqlSession.selectCursor(command.getName(), param);
        }
        return result;
    }

    private <E> Object convertToDeclaredCollection(Configuration config, List<E> list) {
        Object collection = config.getObjectFactory().create(method.getReturnType());
        MetaObject metaObject = config.newMetaObject(collection);
        metaObject.addAll(list);
        return collection;
    }

    @SuppressWarnings("unchecked")
    private <E> Object convertToArray(List<E> list) {
        Class<?> arrayComponentType = method.getReturnType().getComponentType();
        Object array = Array.newInstance(arrayComponentType, list.size());
        if (arrayComponentType.isPrimitive()) {
            for (int i = 0; i < list.size(); i++) {
                Array.set(array, i, list.get(i));
            }
            return array;
        } else {
            return list.toArray((E[]) array);
        }
    }

    private <K, V> Map<K, V> executeForMap(SqlSession sqlSession, Object[] args) {
        Map<K, V> result;
        Object param = method.convertArgsToSqlCommandParam(args);
        if (method.hasRowBounds()) {
            RowBounds rowBounds = method.extractRowBounds(args);
            result = sqlSession.selectMap(command.getName(), param, method.getMapKey(), rowBounds);
        } else {
            result = sqlSession.selectMap(command.getName(), param, method.getMapKey());
        }
        return result;
    }
}
