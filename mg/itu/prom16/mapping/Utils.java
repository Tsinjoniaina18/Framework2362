package mg.itu.prom16.mapping;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Map;

import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;

public class Utils {
    public static ArrayList<String> allControlerName (String controllerPackage){
        ArrayList<String> listControler = new ArrayList<String>();
        try{
            // String controllerPackage = getServletConfig().getInitParameter("Controllers");
            // out.println(controllerPackage);
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
        for(int i=0 ; i<listControler.size() ; i++){
            Class<?> controler = Class.forName(listControler.get(i));
            Object obj = controler.newInstance();
            Method[] method = obj.getClass().getDeclaredMethods();
            for(int j=0 ; j<method.length ; j++){
                if(method[j].isAnnotationPresent(Get.class)){
                    Mapping map = new Mapping(listControler.get(i) , method[j].getName());
                    Get annotation = method[j].getAnnotation(Get.class);
                    annotedGetFunction.put(annotation.value(), map);
                }
            }
        }
    }

    public static Object callFunction(Mapping map){
        Object returned = null;
        try {
            Class<?> classe = Class.forName(map.getClassName());
            Object obj = classe.newInstance();
            Method method = obj.getClass().getDeclaredMethod(map.getFunctionName() , null);

            returned = method.invoke(obj, null);
        } catch (Exception e) {
            
        }
        return returned;
    }
}
