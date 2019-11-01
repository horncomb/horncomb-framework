package cn.horncomb.framework.data.mybatis.plus.override;

import com.baomidou.mybatisplus.core.override.MybatisMapperProxyFactory;
import org.apache.ibatis.session.SqlSession;

public class SpringMybatisMapperProxyFactory<T> extends MybatisMapperProxyFactory<T> {
    public SpringMybatisMapperProxyFactory(Class<T> mapperInterface) {
        super(mapperInterface);
    }

    public T newInstance(SqlSession sqlSession) {
        final SpringMybatisMapperProxy<T> mapperProxy = new SpringMybatisMapperProxy<>(sqlSession, this.getMapperInterface(), this.getMethodCache());
        return newInstance(mapperProxy);
    }
}
