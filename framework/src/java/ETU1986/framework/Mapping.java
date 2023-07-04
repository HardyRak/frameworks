package ETU1986.framework;

import java.lang.reflect.Method;

public class Mapping {
    String className;
    String methodName;
    Method method;

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Mapping() {
    }

    public Mapping(String className, Method method) {
        this.setClassName(className);
        this.setMethod(method);
    }

    public Mapping(String className, String methodName) {
        this.setClassName(className);
        this.setMethodName(methodName);
    }

    public Mapping(String className, String methodName, Method method) {
        this.setClassName(className);
        this.setMethodName(methodName);
        this.setMethod(method);
    }
}
