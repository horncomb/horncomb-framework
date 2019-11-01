package cn.horncomb.framework.data.mybatis.plus;

import cn.horncomb.framework.data.mybatis.plus.override.SpringMybatisMapperMethod;
import cn.horncomb.framework.data.mybatis.plus.override.SpringMybatisMapperRegistry;
import com.baomidou.mybatisplus.autoconfigure.ConfigurationCustomizer;
import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.MybatisMapperRegistry;
import com.baomidou.mybatisplus.core.override.MybatisMapperMethod;
import javassist.*;
import org.apache.ibatis.session.Configuration;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Field;

public class SpringMybatisConfigurationCustomizer implements ConfigurationCustomizer {
//    static Class mybatisMapperProxy;
//    static {
//        try {
//            ClassPool pool = new ClassPool();
//            pool.appendSystemPath();
////            pool.insertClassPath(new LoaderClassPath(MybatisMapperProxy.class.getClassLoader()));
////            CtClass clz = pool.get(MybatisMapperProxy.class.getName());
//            CtClass clz = pool.get("com.baomidou.mybatisplus.core.override.MybatisMapperProxy");
//            CtMethod method = clz.getDeclaredMethod("cachedMapperMethod");
//            StringBuilder bodyBuilder = new StringBuilder();
//            bodyBuilder
//                    .append("{")
//                    .append(MybatisMapperMethod.class.getName() + " mapperMethod = methodCache.get($1);")
//                    .append("System.out.println(\"Yes!!\");")
//                    .append("  if (mapperMethod == null) {")
//                    .append("    mapperMethod = new " + SpringMybatisMapperMethod.class.getName() + "(mapperInterface, $1, sqlSession.getConfiguration());")
//                    .append("    methodCache.put($1, mapperMethod);")
//                    .append("  }")
//                    .append("  return mapperMethod;")
//                    .append("}");
//            method.setBody(bodyBuilder.toString());
//            mybatisMapperProxy = clz.toClass();
//        } catch (NotFoundException | CannotCompileException e) {
//            ReflectionUtils.rethrowRuntimeException(e);
//        }
//    }

    @Override
    public void customize(Configuration configuration) {
        try {
            MybatisConfiguration conf = (MybatisConfiguration) configuration;
            Field field = conf.getClass().getDeclaredField("mybatisMapperRegistry");
            int rawModifierInt = field.getModifiers();

            Field modifiersField = Field.class.getDeclaredField("modifiers");
            boolean rawModifierAccessible = modifiersField.isAccessible();

            // 强制修改final
            modifiersField.setAccessible(true);
            modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);
            field.setAccessible(true);
            field.set(conf, new SpringMybatisMapperRegistry(conf));

            // 还原 modifiersField
            modifiersField.setInt(field, rawModifierInt);
            modifiersField.setAccessible(rawModifierAccessible);
        } catch (IllegalAccessException e) {
            ReflectionUtils.rethrowRuntimeException(e);
        } catch (NoSuchFieldException e) {
            ReflectionUtils.rethrowRuntimeException(e);
        }


//        try {
//            ClassPool pool = new ClassPool();
//            pool.appendSystemPath();
//            pool.insertClassPath(new LoaderClassPath(MybatisMapperProxy.class.getClassLoader()));
//            CtClass clz = pool.get(MybatisMapperProxy.class.getName());
//            CtClass clz = pool.get("com.baomidou.mybatisplus.core.override.MybatisMapperProxy");
//            CtMethod method = clz.getDeclaredMethod("cachedMapperMethod");

//            CtClass newClz = pool.get(SpringMybatisMapperProxy.class.getName());
//            CtMethod newMethod = newClz.getDeclaredMethod("cachedMapperMethod");
//            ClassMap clzMap = new ClassMap();
//            clzMap.put(SpringMybatisMapperMethod.class.getSimpleName(), SpringMybatisMapperMethod.class.getName());
//            method.setBody(newMethod, clzMap);

//            StringBuilder bodyBuilder = new StringBuilder();
//            bodyBuilder
//                    .append("{")
//                    .append(MybatisMapperMethod.class.getName() + " mapperMethod = methodCache.get($1);")
//                    .append("System.out.println(\"Yes!!\");")
//                    .append("  if (mapperMethod == null) {")
//                    .append("    mapperMethod = new " + SpringMybatisMapperMethod.class.getName() + "(mapperInterface, $1, sqlSession.getConfiguration());")
//                    .append("    methodCache.put($1, mapperMethod);")
//                    .append("  }")
//                    .append("  return mapperMethod;")
//                    .append("}");
//            method.setBody(bodyBuilder.toString());
//            clz.toClass();

//            method.setBody("return new " + SpringMybatisMapperMethod.class.getName() + "(mapperInterface, $1, sqlSession.getConfiguration());");
//            MybatisMapperProxy proxy = new MybatisMapperProxy(configuration);
//            HotSwapper swap = new HotSwapper(8000);
//            swap.reload(MybatisMapperProxy.class.getName(), clz.toBytecode());
//        } catch (NotFoundException | CannotCompileException e) {
//            ReflectionUtils.rethrowRuntimeException(e);
//        }
    }
}
