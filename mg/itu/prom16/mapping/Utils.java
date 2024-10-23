package mg.itu.prom16.mapping;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import com.mysql.cj.exceptions.ExceptionFactory;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.annotation.Url;
import mg.itu.prom16.annotation.NameField;
import mg.itu.prom16.annotation.Param;
import mg.itu.prom16.annotation.Post;

public class Utils {
    public static ArrayList<String> allControlerName (String controllerPackage){
        ArrayList<String> listControler = new ArrayList<String>();
        try{
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            URL url = loader.getResource(controllerPackage);
            // out.println(url.getFile().replace("%20", " "));

            if(url != null){
                File directory = new File(url.getFile().replace("%20", " "));
                if(directory.exists() && directory.isDirectory()){
                    File[] files = directory.listFiles();
                    if(files!=null){
                        for(int i=0 ; i<files.length ; i++){
                            File file = files[i];
                            if(file.isFile() && file.getName().endsWith(".class")){
                                String nom = file.getName().split("\\.")[0];
                                Class<?> classe = Class.forName(String.format("%s.%s" , controllerPackage, nom));
                                if(classe.isAnnotationPresent(Controller.class)){
                                    listControler.add(classe.getName());
                                }
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            // out.println(e.getMessage());
        }
        return listControler;
    }

    public static void allAnnotedGetFunction (Map<String , Mapping> annotedGetFunction , String controllerPackage)throws Exception{
        ArrayList<String> listControler = allControlerName(controllerPackage);
        String verb = "";
        for(int i=0 ; i<listControler.size() ; i++){
            Class<?> controler = Class.forName(listControler.get(i));
            Object obj = controler.getDeclaredConstructor().newInstance();
            Method[] method = obj.getClass().getDeclaredMethods();
            for(int j=0 ; j<method.length ; j++){
                if(method[j].isAnnotationPresent(Url.class)){
                    if(!method[j].isAnnotationPresent(Post.class) && !method[j].isAnnotationPresent(Get.class)){
                        verb = "GET";
                    }else {
                        if (method[j].isAnnotationPresent(Post.class)){
                            verb = "POST";
                        }else{
                            verb = "GET";
                        }
                    }
                    Mapping map = new Mapping();
                    ClassMethod classMethod = new ClassMethod(listControler.get(i) , method[j].getName(), verb);

                    Url annotation = method[j].getAnnotation(Url.class);
                    if(urlAlreadyExist(annotedGetFunction, annotation.value()) == 0){
                        map.addClassMethod(classMethod);
                        annotedGetFunction.put(annotation.value(), map);
                    }
                    else{
                        Mapping temp = annotedGetFunction.get(annotation.value());
                        if(temp.classMethodByVerb(verb)!=null){
                            throw new Exception("Method "+verb+" already exist on the Url '"+annotation.value()+"'");
                        }
                        temp.addClassMethod(classMethod);
                        annotedGetFunction.replace(annotation.value(), temp);
                        /*Exception e = new Exception("Url deja present pour un autre fonction");
                        throw e;*/
                    }
                }
            }
        }
    }

    public static int urlAlreadyExist (Map<String , Mapping> annotedGetFunction , String url){
        for(String key: annotedGetFunction.keySet()){
            if(key.equals(url)){
                return 1;
            }
        }
        return 0;
    }

    public static Method callFunction(Mapping map , HttpSession httpSession, HttpServletRequest request){
        Method returned = null;
        try {
            Method method = (Method)determineMethod(map , httpSession, request);

            returned = method;
        } catch (Exception e) {
            
        }
        return returned;
    }

    public static Object executeFunctionWithNoArgument(Mapping map, Method method, HttpServletRequest request){
        Object returned = null;
        try{
            String verb = request.getMethod();
            ClassMethod classMethod = map.classMethodByVerb(verb);

            Class<?> classe = Class.forName(classMethod.getClassName());
            Object obj = classe.getDeclaredConstructor().newInstance();

            returned = method.invoke(obj, null);
        }
        catch(Exception e){
            System.out.println(e);
        }
        return returned;
    }

    public static Object determineMethod(Mapping map, HttpSession httpSession, HttpServletRequest request){
        Object returned = null;
        try{
            String verb = request.getMethod();
            ClassMethod classMethod = map.classMethodByVerb(verb);

            Class<?> classe = Class.forName(classMethod.getClassName());
            Object obj = classe.getDeclaredConstructor().newInstance();
            Method[] methods = obj.getClass().getDeclaredMethods();
            for(int i=0 ; i<methods.length ; i++){
                if(methods[i].getName().equals(classMethod.getFunctionName())){
                    Class<?>[] parameterTypes = methods[i].getParameterTypes();
                    returned = obj.getClass().getDeclaredMethod(classMethod.getFunctionName(), parameterTypes);
                    break;
                }
            }
        }catch(Exception e){

        }
        return returned;
    }

    public static Object[] parameterNames(Method method, HttpServletRequest request) throws Exception{
        Parameter[] parameterNames = method.getParameters();
        Object[] parameterValues = new Object[parameterNames.length];
        String requestValue;
        for(int i=0 ; i<parameterNames.length ; i++){
            if(parameterNames[i].getType().isPrimitive() || parameterNames[i].getType()==String.class){
                if(parameterNames[i].isAnnotationPresent(Param.class)){
                    Param annotation = parameterNames[i].getAnnotation(Param.class);

                    requestValue = request.getParameter(annotation.value());
                    parameterValues[i] = caster(requestValue, parameterNames[i].getType().getSimpleName());
                }else{
                    // names.add(parameterNames[i].getName());
                    Exception e = new Exception("ETU 002362 : L'argument nomme "+parameterNames[i].getName()+" n'est pas annote");
                    throw e;
                }
            }
            else{

                String nom = parameterNames[i].getName();
                String requestName;
                Class<?> parameterClass = parameterNames[i].getType();
                Object obj = parameterClass.getDeclaredConstructor().newInstance();

                if(parameterNames[i].isAnnotationPresent(Param.class)){
                    Param annotation = parameterNames[i].getAnnotation(Param.class);
                    nom = annotation.value();

                    Field[] attributs = parameterClass.getDeclaredFields();
                    Object[] attributsValue = new Object[attributs.length];

                    for(int j=0 ; j<attributs.length ; j++){
                        int type = 0;
                        if(attributs[j].isAnnotationPresent(NameField.class)){
                            NameField annotationField = attributs[j].getAnnotation(NameField.class);
                            requestName = nom+"."+annotationField.value();
                            if(annotationField.file()){
                                type = 1;
                            }
                        }
                        else{
                            requestName = nom+"."+attributs[j].getName();
                        }

                        if(type == 0){
                            requestValue = request.getParameter(requestName);
                            attributsValue[j] = caster(requestValue, attributs[j].getType().getSimpleName());
                        }else if(type == 1){
                            Part part = request.getPart(requestName);
                            if(attributs[j].getType().getSimpleName().equals("String")){
                                attributsValue[j] = fileName(part);
                            }else if(attributs[j].getType().isArray()){
                                attributsValue[j] = fileBytes(part);
                            }
                        }

                    }

                    obj = process(obj, attributsValue);
                    parameterValues[i] = obj;
                }
                else{
                    if(parameterNames[i].getType()!=CustomSession.class){
                        Exception e = new Exception("ETU 002362 : L'argument nomme "+nom+" n'est pas annote");
                        throw e;
                    }
                }
            }
        }
        return parameterValues;
    }

    public static Object callFunction2(Mapping map , Method method , Object[] parameters , HttpSession httpSession, HttpServletRequest request)throws Exception{
        String verb = request.getMethod();
        ClassMethod classMethod = map.classMethodByVerb(verb);

        Object returned = null;
        Class<?> classe = Class.forName(classMethod.getClassName());
        Object obj = classe.getDeclaredConstructor().newInstance();
        Parameter[] types = method.getParameters();
        for(int i=0 ; i<types.length ; i++){
            if(types[i].getType()==CustomSession.class){
                parameters[i] = new CustomSession(httpSession);
                break;
            }
        }
        
        Field[] attributs = obj.getClass().getDeclaredFields();
        for(Field attribut : attributs){
            if(attribut.getType()==CustomSession.class){
                CustomSession session = new CustomSession(httpSession);
                attribut.setAccessible(true);
                
                attribut.set(obj, session);
                attribut.setAccessible(false);
            }
        }

        returned = method.invoke(obj, parameters);
        return returned;
    }

    public static Object caster (String data , String type){
        if(data==null){
            return 0;    
        }
        if(type.equalsIgnoreCase("int")){
            return Integer.parseInt(data);
        }
        else if(type.equalsIgnoreCase("double")){
            return Double.parseDouble(data);
        }
        else if(type.equalsIgnoreCase("long")){
            return Long.parseLong(data);
        }
        return data;
    }

    public static <T> T process(T object , Object[] attributValues)throws Exception{
        Class<?> clazz = object.getClass();
        Field[] attributs = clazz.getDeclaredFields();

        for(int i=0 ; i<attributs.length ; i++){
            Field attribut = attributs[i];
            attribut.setAccessible(true);
            Object valeur = attributValues[i];
            if(valeur != null){
                attribut.set(object, valeur);
            }else{
                attribut.set(object, null);
            }
            attribut.setAccessible(false);
        }

        return object;
    }

    public static String fileName(Part part){
        String contentDisp = part.getHeader("content-disposition");
        String[] tokens = contentDisp.split(";");
        for(String token : tokens){
            if(token.trim().startsWith("filename")){
                return token.substring(token.indexOf("=")+1, token.length());
            }
        }
        return "";
    }

    public static byte[] fileBytes(Part part) throws IOException{
        InputStream inputStream = part.getInputStream();
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int n = 0;
        while (-1 != (n = inputStream.read(buffer))) {
            outputStream.write(buffer, 0, n);
        }
        return outputStream.toByteArray();
    }

    public static String ErrorPage(String title , String cause){
        String val = "";
        val += "Erreur : ";
        val += "Title : "+title+" ";
        val += "Cause : "+cause;
        return val;
    }
}
