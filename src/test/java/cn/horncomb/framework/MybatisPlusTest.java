package cn.horncomb.framework;

import cn.horncomb.framework.data.mybatis.plus.override.SpringMybatisMapperMethod;
import com.baomidou.mybatisplus.core.override.MybatisMapperProxy;
import org.apache.ibatis.javassist.*;
import org.junit.Test;
import org.springframework.util.ReflectionUtils;

public class MybatisPlusTest {

    @Test
    public void testReplaceMethd() {
        try {
            ClassPool pool = new ClassPool();
            pool.appendSystemPath();
            CtClass clz = pool.makeClass(MybatisMapperProxy.class.getName());
            CtMethod method = clz.getDeclaredMethod("cachedMapperMethod");
//            CtClass newClz = pool.makeClass(SpringMybatisMapperProxy.class.getName());
//            CtMethod newMethod = newClz.getDeclaredMethod("cachedMapperMethod");
            method.setBody("return methodCache.computeIfAbsent(method, k -> new "
                    + SpringMybatisMapperMethod.class.getName()
                    + "(mapperInterface, method, sqlSession.getConfiguration()));");
//            HotSwapper swap = new HotSwapper(8000);
//            swap.reload(MybatisMapperProxy.class.getName(), clz.toBytecode());
        } catch (NotFoundException | CannotCompileException e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }
    }
}