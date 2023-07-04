package ETU1986.framework.servlet;

import ETU1986.framework.Mapping;
import ETU1986.framework.ModelView;
import ETU1986.framework.annotation.Url;

import ETU1986.framework.annotation.pName;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@WebServlet(name = "FrontServlet", value = "/")
public class FrontServlet extends HttpServlet {
    private HashMap<String, Mapping> MappingUrls;

    public HashMap<String, Mapping> getMappingUrls() {
        return MappingUrls;
    }
    public void setMappingUrls(HashMap<String, Mapping> mappingUrls) {
        MappingUrls = mappingUrls;
    }

    @Override
    public void init(){
        try {
            String[] packages = this.readConfig();
            this.fillMappingUrls(packages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void redirect(String indexOfFile, HttpServletRequest req, HttpServletResponse rep) throws ServletException, IOException {
        RequestDispatcher dispatcher = req.getRequestDispatcher(indexOfFile);
        dispatcher.forward(req, rep);
    }

    public void fillingAttribute(ModelView modelView,HttpServletRequest req, HttpServletResponse rep){
        Set<String> mvKey = modelView.getData().keySet();
        for(String key : mvKey){
            req.setAttribute(key, modelView.getData().get(key));
        }
    }

    public Object conversion(String parametre){
        Object value = parametre.trim();
        try{
            value = Integer.parseInt(parametre.trim());
        }catch (NumberFormatException e1){
            try{
                value = Double.parseDouble(parametre.trim());
            }catch (NumberFormatException e2) {
                try{
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                    value = sdf.parse(parametre.trim());
                } catch (ParseException ignored) {
                }
            }
        }
        return value;
    }

    public Object myCast(String parametre, Class<?> type){
        Object value = parametre.trim();
        if(type.equals(int.class) || type.equals(Integer.class)){
            value = Integer.parseInt(parametre.trim());
        }
        if(type.equals(double.class) || type.equals(Double.class)){
            value = Double.parseDouble(parametre.trim());
        }
        if(type.equals(Date.class)){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                value = sdf.parse(parametre.trim());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        return value;
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse rep) throws Exception {
        PrintWriter out = rep.getWriter();
        String incomingURL = String.valueOf(req.getRequestURL());
        String target = this.getTarget(incomingURL);

        Mapping mapping = this.getMappingUrls().get(target);
        if(mapping != null){
            Class<?> clazz = Class.forName(mapping.getClassName());
            Constructor<?> constructor = clazz.getConstructor();
            Object clone = constructor.newInstance();
            Method method = mapping.getMethod();

            Parameter[] parameters = method.getParameters();
            String[] argumentsNamesList = this.getArgumentNames(method);
            Object[] args = new Object[argumentsNamesList.length];

            Enumeration<String> parameterNames = req.getParameterNames();   // Maka ny nom parametres en entrer rehetra dispo ao @ getParameter() 
            while(parameterNames.hasMoreElements()){    // si != 0 ny taille (Misy parametres hoavy)
                String paramName = parameterNames.nextElement();
                Field paramField = null;
                try{
                    paramField = clone.getClass().getField(paramName.toLowerCase());    // si le name du formulaire est un field de la classe Cible
                }catch (NoSuchFieldException ignored){
                    System.out.println("L'attribut "+paramName+" n'est pas présent dans la class '"+clazz.getSimpleName()+"'");
                }

        // -------------- en utilisant les setters -----------------
                if(paramField != null){    // si le field du formulaire ou du link existe
                    String[] paramValues = req.getParameterValues(paramName);
                    Object paramValue = null;
                    if(paramValues.length == 1){
                        paramValue = myCast(paramValues[0], paramField.getType());
                    }else{
                        Object[] objectValue = new Object[paramValues.length];
                        for(int i=0;i<paramValues.length;i++){
                            objectValue[i] = this.conversion(paramValues[i]);
                        }
                        paramValue = objectValue;
                    }
                    Method setters = clone.getClass().getMethod("set"+paramName.substring(0, 1).toUpperCase()+paramName.substring(1), paramField.getType()); // settena ao @ class cible tous les parametres
                    //System.out.println("Setters: "+setters.getName()+" avec value: "+paramValue);
                    setters.invoke(clone, paramValue);
                }
        // ------- en utilisant les arguments d'une fonction --------
                String[] paramValues = req.getParameterValues(paramName);
                int pIndice = this.getArgumentIndice(argumentsNamesList ,paramName);
                if(pIndice != -1){
                    Object paramValue = myCast(paramValues[0], parameters[pIndice].getType());
                    args[pIndice] = paramValue;
                }
            }

            Object result = result = method.invoke(clone, args);
            if(result instanceof ModelView){
                ModelView modelView = (ModelView) result;
                this.fillingAttribute(modelView, req, rep);
                this.redirect(modelView.getView(), req, rep);
            }
        }else{
            //System.out.println("Url inconnu");
            throw new Exception("Url inconnu");
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp){
        try {
            this.processRequest(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp){
        try {
            this.processRequest(req, resp);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String removeHttpProtocoleStr(String URL){
        return URL.split("//")[1];
    }

    private String getTarget(String URL){
        URL = removeHttpProtocoleStr(URL);
        String[] urlsplitted = URL.split("/");
        //System.out.println("Target >>> "+urlsplitted[urlsplitted.length-1]);
        return urlsplitted[urlsplitted.length-1];
    }

    public String[] getArgumentNames(Method method){    // prendre la liste de tous les arguments annote d'une fonction
        Parameter[] params = method.getParameters();
        String[] argsNames = new String[params.length];
        int it = 0;
        for(Parameter p : params){
            if(p.isAnnotationPresent(pName.class)){
                pName pname = (pName) p.getAnnotation(pName.class);
                argsNames[it] = pname.value();
            }else{
                argsNames[it] = null;
            }
            it++;
        }
        return argsNames;
    }

    public int getArgumentIndice(String[] argsNames, String name){  // recuperer la position d'un arguments dans sa fonction
        int ind = -1;
        for(int i = 0; i<argsNames.length ; i++){
            if(argsNames[i] != null && argsNames[i].equals(name)){
                ind = i;
                break;
            }
        }
        return ind;
    }
    
    public String[] readConfig() throws IOException, SAXException, ParserConfigurationException {
        String path = getServletContext().getRealPath("/WEB-INF/myconfig.xml");
        File inputFile = new File(path);
        //System.out.println("Absolute path: "+inputFile.getAbsolutePath());
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document doc = dBuilder.parse(inputFile);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("myclasspath");
        List<String> packages = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node nNode = nodeList.item(i);

            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                packages.add(element.getElementsByTagName("package").item(0).getTextContent());
            }
        }

        String[] list = new String[packages.size()];
        int it = 0;
        for(String s : packages){
            list[it] = s;
            it++;
        }
        return list;
    }

    public List<File> packagesFiles(String[] packages) throws Exception {  // String[] to List<String>
        List<File> files = new ArrayList<>();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for(String pkg : packages){
            try{
                files.add(new File(Objects.requireNonNull(classLoader.getResource(pkg.replace('.', '/'))).getFile()));
            }catch (Exception e){
                throw new Exception("Votre package n'existe pas ("+pkg+")");
            }
        }
        return files;
    }

    private void fillMappingUrls(String[] packages) throws Exception {
        this.setMappingUrls(new HashMap<String, Mapping>());

        List<File> packageDir = this.packagesFiles(packages);
        for(int i=0 ; i < packageDir.size() ; i++){
            if(packageDir.get(i).exists()){
                File[] files = packageDir.get(i).listFiles();
                assert files != null;
                for(File f : files){
                    String file = f.getName();
                    if(f.isDirectory()){
                        String[] newpackage = new String[1];
                        newpackage[0] = packages[i]+"."+file;
                        this.fillMappingUrls(newpackage);
                    }else{
                        if(file.endsWith(".class")){
                            String className = file.substring(0, file.length()-6 /* exemple: test.class >> enlever .class */);
                            Class<?> clazz = Class.forName(packages[i] + '.' + className);
                            System.out.println(clazz.getName());
                            for(Method method : clazz.getDeclaredMethods()){
                                if(method.isAnnotationPresent(Url.class)){
                                    Url url = (Url) method.getAnnotation(Url.class);
                                    this.getMappingUrls().put(url.value(), new Mapping(clazz.getName(), method));
                                }
                            }
                        }
                    }
                }
            }
        }
        //System.out.println("Mapping length: "+this.getMappingUrls().size());
    }
}
