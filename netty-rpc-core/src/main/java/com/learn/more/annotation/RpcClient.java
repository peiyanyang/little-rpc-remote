package com.learn.more.annotation;

import java.lang.annotation.*;

/**
 * @author pyy
 */
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcClient {

    String value() default "";
    
}
