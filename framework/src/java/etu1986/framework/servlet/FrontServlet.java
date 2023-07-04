/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package etu1986.framework.servlet;

import etu1986.framework.Mapping;
import etu1986.framework.annotation.Url;
import jakarta.servlet.ServletConfig;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author hardy
 */
@WebServlet(name = "FrontServlet", urlPatterns = {"*.FrontServlet"})
public class FrontServlet extends HttpServlet {
    Map<String,Mapping> mappingURL;
    HttpServletRequest request;
    HttpServletResponse response;

    public Map<String, Mapping> getMappingURL() {
        return mappingURL;
    }

    public void setMappingURL(Map<String, Mapping> mappingURL) {
        this.mappingURL = mappingURL;
    }
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    
    
    
    protected void processRequest() throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
           // out.printf(request.getRequestURI());
            this.execute(this, request.getRequestURI());
        }
    }
    
    @Override
    public void init(ServletConfig config) throws ServletException{
        try {
            
        } catch (Exception ex) {
            Logger.getLogger(FrontServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.request=request;
        this.response=response;
        this.processRequest();
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        this.request=request;
        this.response=response;
        this.processRequest();
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    public String getURLvalues(String URL){
        String[] uri=URL.split("[/]");
        return uri[uri.length-1];
    }
    
    public String getAnnotationClass(String URL){
        String[] clas=getURLvalues(URL).split("[.]");
        return clas[clas.length-1];
    }
    
    
    public String getAnnotationFunction(String URL){
        String [] func=getURLvalues(URL).split("[.]");
        return func[0];
    }
    public void execute(Object control,String URL) throws IOException{
        Method[] m=control.getClass().getDeclaredMethods();
        
        for(Method meth:m){
            Annotation annot=meth.getAnnotation(Url.class);
            Url myannot=(Url)annot;
            if(myannot!=null && myannot.value().equals(this.getAnnotationFunction(URL))){
                try {
                    meth.invoke(control);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(FrontServlet.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
    
    public static Map<String, Mapping> scanPackageForAnnotations(String pk) throws Exception {
            Map<String, Mapping> result = new HashMap<>();
            String packageName = pk;
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File directory = new File(resource.getFile());
                if (!directory.exists()) {
                    continue;
                }
                File[] files = directory.listFiles();
                for (File file : files) {
                    if(file.isDirectory()){
                        Map<String, Mapping> temp = FrontServlet.scanPackageForAnnotations(file.getName());
                         Object[] ob=temp.keySet().toArray();
                            for (Object ob1 : ob) {
                                result.put(ob1.toString(), temp.get(ob1.toString()));
                            }
                    }
                    else{
                        String fileName = file.getName();
                        if (fileName.endsWith(".class")) {
                            String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                            Class<?> clazz = Class.forName(className);
                            if (clazz.isAnnotationPresent(WebServlet.class)) {
                                Method[] methods = clazz.getMethods();
                                for (Method method : methods) {
                                    if (method.isAnnotationPresent(Url.class)) {
                                        Mapping mapping = new Mapping();
                                        mapping.setClassName(clazz.getSimpleName());
                                        mapping.setMethod(method.getName());

                                        result.put(method.getAnnotation(Url.class).value()+"."+clazz.getSimpleName(), mapping);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            return result;
        }
}