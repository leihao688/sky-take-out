package com.sky.Aspect;

import com.sky.annotation.autoFill;
import com.sky.context.BaseContext;
import com.sky.entity.Employee;
import com.sky.enumeration.OperationType;
import com.sun.tools.jdeprscan.scan.MethodSig;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Slf4j
@Component
public class autoFillAspect {

    /**
     * 1.创建注解确定我们需要自动设置字段的方法@autoFill
     * 2.创建切面对象AOP，第一个注解@Pointcut用标识需要在哪些方法里执行切面方法
     * 3.选择执行时间@Before、@Around、@After
     * 4.获取signature对象得到注解的属性
     * 5.通过JoinPoint获取实体再通过反射获取实体的方法实现需要的业务
     * 6.反射可以大程度的减少我们手写的代码
     */

    @Pointcut("execution(* com.sky.mapper.*.*(..))&&@annotation(com.sky.annotation.autoFill)")
    public void autoFillPointCut(){}

    @Before("autoFillPointCut()")
    public void autofill (JoinPoint joinPoint) throws Exception {
        //获取当前被拦截的方法上的数据库操作
        /** Signature 是一个方法签名的抽象接口
         *创建方法形signature，以获取拦截当前注解方法的类型
         */
        log.info("获取方法签名");
        MethodSignature signature = (MethodSignature)joinPoint.getSignature();
        autoFill autofill=signature.getMethod().getAnnotation(autoFill.class);
        OperationType operationType= autofill.value();

        //getArgs()获取到当前被拦截的方法的参数--实体对象
        Object[]objects=joinPoint.getArgs();
        if(objects.length == 0 || objects == null){
            return;
        }
        Object entity=objects[0];
        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId  = BaseContext.getCurrentId();
        //根据当前不同的操作类型，为对应的属性通过反射赋值
        if(operationType==operationType.INSERT){
           Method setCreateTime = entity.getClass().getMethod("setCreateTime",LocalDateTime.class);
           Method setUpdateTime = entity.getClass().getMethod("setUpdateTime",LocalDateTime.class);
           Method setCreateUser = entity.getClass().getMethod("setCreateUser",Long.class);
           Method setUpdateUser = entity.getClass().getMethod("setUpdateUser",Long.class);

           setCreateTime.invoke(entity,now);
           setCreateUser.invoke(entity,currentId);
           setUpdateTime.invoke(entity,now);
           setUpdateUser.invoke(entity,currentId);
        }
        else{
            Method setUpdateTime = entity.getClass().getMethod("setUpdateTime",LocalDateTime.class);
            Method setUpdateUser = entity.getClass().getMethod("setUpdateUser",Long.class);
            setUpdateTime.invoke(entity,now);
            setUpdateUser.invoke(entity,currentId);
        }



    }


}
