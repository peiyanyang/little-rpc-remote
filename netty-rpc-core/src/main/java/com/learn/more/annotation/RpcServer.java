package com.learn.more.annotation;

import java.lang.annotation.*;

/**
 * @author pyy
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface RpcServer {

    String value() default "";
    
}
