/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package etu1986.framework;

import java.lang.reflect.Method;

/**
 *
 * @author hardy
 */
public class Mapping {
    String className;
    String methodname;
    Method method;

    public Mapping(String className,Method method) {
        this.className = className;
        this.methodname = methodname;
        this.method = method;
    }
    
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getMethod() {
        return methodname;
    }

    public void setMethod(String methodname) {
        this.methodname = methodname;
    }

    public Mapping(String className, String methodname) {
        this.className = className;
        this.methodname = methodname;
    }
    public Mapping(){
        
    }
}
