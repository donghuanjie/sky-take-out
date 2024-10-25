package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面，实现公共字段自动填充处理原则
 */

@Aspect
@Component
@Slf4j
public class AutoFillAspect {

    /**
     * 定义切入点，表达式左边的代表选择所有底下的mapper，右边表示选择带有AutoFill的
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointcut() {
    }

    /**
     * 前置通知，在通知中进行公共字段的赋值
     * @param joinPoint
     */
//    我们需要获取拦截点的信息，这个拦截点就是JoinPoint
    @Before("autoFillPointcut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充...");

        //1. 获取当前被拦截方法的签名，数据库操作类型(update, insert)
        // 注意这里必须向下转型成MethodSignature才行，MethodSignature才有getMethod方法
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFill autoFill = signature.getMethod().getAnnotation(AutoFill.class);
        OperationType operationType = autoFill.value();

        //2. 获取当前被拦截方法的参数--实体对象（这里我们约定实体放在第一位）
        Object[] args = joinPoint.getArgs();
        // 防止出现空指针的情况
        if (args == null || args.length == 0) {
            return;
        }

        Object entity = args[0];  // 这里我们之前约定了第一个输入的参数就是实体

        //3. 准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();

        //4. 根据当前不同的操作类型，为相应的属性赋值（如果是create则4个都要赋值，如果是update则只需要2个赋值）
        if (operationType == OperationType.INSERT) {
            // 通过反射赋值（因为实体类已经有@Data的注释，所以可以直接调用setter)
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AOP自动赋值错误");
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                log.error("AOP自动更新值错误");
                e.printStackTrace();
            }
        }
    }

}
