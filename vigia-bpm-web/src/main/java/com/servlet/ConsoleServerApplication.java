package com.servlet;

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;



@ApplicationPath("/rest")
public class ConsoleServerApplication extends Application {

    @Override
    public Set<Class<?>> getClasses() {
	Set<Class<?>> classes = new HashSet<Class<?>>();
	classes.add(ConsoleServerRest.class);
	return classes;
    }
}
