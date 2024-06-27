package mg.itu.prom16.mapping;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.annotation.NameField;
import mg.itu.prom16.annotation.Param;

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

    public static ArrayList<String> allAnnotedGetFunction (Map<String , Mapping> annotedGetFunction , String controllerPackage)throws Exception{
        ArrayList<String> doublons = new ArrayList<>();
        ArrayList<String> listControler = allControlerName(controllerPackage);
        for(int i=0 ; i<listControler.size() ; i++){
            Class<?> controler = Class.forName(listControler.get(i));
            Object obj = controler.newInstance();
            Method[] method = obj.getClass().getDeclaredMethods();
            for(int j=0 ; j<method.length ; j++){
                if(method[j].isAnnotationPresent(Get.class)){
                    Mapping map = new Mapping(listControler.get(i) , method[j].getName());
                    Get annotation = method[j].getAnnotation(Get.class);
                    if(urlAlreadyExist(annotedGetFunction, annotation.value()) == 0){
                        annotedGetFunction.put(annotation.value(), map);
                    }
                    else{
                        doublons.add(annotation.value());
                    }
                }
            }
        }
        return doublons;
    }

    public static int urlAlreadyExist (Map<String , Mapping> annotedGetFunction , String url){
        for(String key: annotedGetFunction.keySet()){
            if(key.equals(url)){
                return 1;
            }
        }
        return 0;
    }

    public static Object callFunction(Mapping map){
        Object returned = null;
        try {
            Class<?> classe = Class.forName(map.getClassName());
            Object obj = classe.newInstance();
            // Method method = obj.getClass().getDeclaredMethod(map.getFunctionName() , null);
            Method method = (Method)determineMethod(map);

            if(method.getParameterCount()==0){
                returned = method.invoke(obj, null);
            }
            else{
                returned = method;
            }
        } catch (Exception e) {
            
        }
        return returned;
    }

    public static Object determineMethod(Mapping map){
        Object returned = null;
        try{
            Class<?> classe = Class.forName(map.getClassName());
            Object obj = classe.newInstance();
            Method[] methods = obj.getClass().getDeclaredMethods();
            for(int i=0 ; i<methods.length ; i++){
                if(methods[i].getName().equals(map.getFunctionName())){
                    Class<?>[] parameterTypes = methods[i].getParameterTypes();
                    returned = obj.getClass().getDeclaredMethod(map.getFunctionName(), parameterTypes);
                    break;
                }
            }
        }catch(Exception e){

        }
        return returned;
    }

    public static ArrayList<String> parameterNames(Method method) throws Exception{
        Parameter[] parameterNames = method.getParameters();
        ArrayList<String> names = new ArrayList<String>();
        for(int i=0 ; i<parameterNames.length ; i++){
            if(parameterNames[i].getType().isPrimitive() || parameterNames[i].getType()==String.class){
                if(parameterNames[i].isAnnotationPresent(Param.class)){
                    Param annotation = parameterNames[i].getAnnotation(Param.class);
                    names.add(annotation.value());
                }else{
                    // names.add(parameterNames[i].getName());
                    Exception e = new Exception("ETU 002362 : L'argument nomme "+parameterNames[i].getName()+" n'est pas annote");
                    throw e;
                }
            }
            else{
                String nom = parameterNames[i].getName();
                if(parameterNames[i].isAnnotationPresent(Param.class)){
                    Param annotation = parameterNames[i].getAnnotation(Param.class);
                    nom = annotation.value();
                }
                else{
                    Exception e = new Exception("ETU 002362 : L'argument nomme "+nom+" n'est pas annote");
                    throw e;
                }
                Class<?> obj = parameterNames[i].getType();
                Field[] attributs = obj.getDeclaredFields();
                for(int j=0 ; j<attributs.length ; j++){
                    if(attributs[j].isAnnotationPresent(NameField.class)){
                        NameField annotation = attributs[j].getAnnotation(NameField.class);
                        names.add(nom+"."+annotation.value());
                    }
                    else{
                        names.add(nom+"."+attributs[j].getName());
                    }
                }
            }
        }
        return names;
    }

    public static Object callFunction2(Mapping map , Method method , ArrayList<String> requestValue)throws Exception{
        Object returned = null;
        Object[] parameters = castToRigthType(method, requestValue);
        Class<?> classe = Class.forName(map.getClassName());
        Object obj = classe.newInstance();
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

    public static Object[] castToRigthType(Method method , ArrayList<String> requestValue)throws Exception{
        Object[] objs = new Object[method.getParameterTypes().length];
        Class<?>[] parameterTypes = method.getParameterTypes();
        int indArg = 0;
        for(int i = 0 ; i<requestValue.size() ; i++){

            if(parameterTypes[indArg].isPrimitive() || parameterTypes[indArg] == String.class){
                objs[indArg] = caster(requestValue.get(i) , parameterTypes[indArg].getSimpleName());
                indArg++;
            }else{
                Class<?> obj = parameterTypes[indArg];
                Object obj2 = obj.newInstance();
                Field[] attributs = obj.getDeclaredFields();
                Object[] attributValues = new Object[attributs.length];
                for(int j=0 ; j<attributs.length ; j++){
                    attributValues[j] = caster(requestValue.get(i), attributs[j].getType().getSimpleName());
                    i++;
                }

                obj2 = process(obj2 , attributValues);
                objs[indArg] = obj2;
                indArg++;
                if(attributs.length>0){
                    i--;
                }
            }

        }
        return objs;
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
        }

        return object;
    }

    public static String ErrorPage(String title , String cause){
        String val = "";
        val += "<html>";
        val += "<head>";
        val += "<title>Error Framework 2362</title>";
        val += "</head>";    
        val += "<body>";

        val += "<h1>"+title+"</h1>";
        val += "<h3>"+cause+"</h3>";

        val += "</body>";
        val += "</html>";
        return val;
    }
}
