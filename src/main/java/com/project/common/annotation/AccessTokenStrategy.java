package com.project.common.annotation;

import org.springframework.beans.factory.annotation.Qualifier;

import java.lang.annotation.*;

@Target(value = {
        ElementType.ANNOTATION_TYPE,
        ElementType.FIELD,
        ElementType.METHOD,
        ElementType.TYPE,
        ElementType.PARAMETER
})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Qualifier("accessTokenStrategy")
public @interface AccessTokenStrategy {
}
