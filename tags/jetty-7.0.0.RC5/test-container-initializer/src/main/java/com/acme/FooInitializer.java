package com.acme;

import java.util.Set;
import java.util.ArrayList;
import javax.servlet.ServletContext;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.ServletContainerInitializer;

@HandlesTypes ({javax.servlet.Servlet.class, Foo.class})
public class FooInitializer implements ServletContainerInitializer
{

    public void onStartup(Set<Class<?>> classes, ServletContext context)
    {
        context.setAttribute("com.acme.Foo", new ArrayList<Class>(classes));
    }
}
