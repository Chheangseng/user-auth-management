package com.tcs.user_auth_management.config.taskConfig;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.springframework.core.annotation.AliasFor;
import org.springframework.scheduling.annotation.Async;

///  use platform thread for heavy task
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Async("platformTaskExecutor")
public @interface AsyncPlatform {

    @AliasFor(annotation = Async.class, attribute = "value")
    String value() default "platformTaskExecutor";
}