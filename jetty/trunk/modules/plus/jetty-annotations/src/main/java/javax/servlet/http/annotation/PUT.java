package javax.servlet.http.annotation;

import java.lang.annotation.*;


@Documented
@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
@HttpMethod(value="PUT")

public @interface PUT
{

}
