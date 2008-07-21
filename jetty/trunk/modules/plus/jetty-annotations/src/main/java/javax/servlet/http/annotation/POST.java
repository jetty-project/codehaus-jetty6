package javax.servlet.http.annotation;

import java.lang.annotation.*;
import java.lang.annotation.ElementType.*;
import java.lang.annotation.RetentionPolicy.*;

@Target(value=ElementType.METHOD)
@Retention(value=RetentionPolicy.RUNTIME)
@HttpMethod(value="POST")
public @interface POST
{

}
