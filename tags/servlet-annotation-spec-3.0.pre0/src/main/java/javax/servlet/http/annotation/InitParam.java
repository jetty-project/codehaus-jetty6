package javax.servlet.http.annotation;

import java.lang.annotation.*;


@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface InitParam
{
    String name() default "";
    String description() default "";
    String value() default "";
}
