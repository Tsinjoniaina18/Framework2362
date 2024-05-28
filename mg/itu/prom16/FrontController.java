package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotation.Controller;
import mg.itu.prom16.annotation.Get;
import mg.itu.prom16.mapping.Mapping;
import jakarta.servlet.RequestDispatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.lang.Package;
import java.lang.reflect.Method;
import java.net.URL;

public class FrontController extends HttpServlet{
    private Map<String , Mapping> annotedGetFunction = new HashMap<>();

    public void init ()throws ServletException{
        try{
            allAnnotedGetFunction();
        }
        catch(Exception e){

        }
    }

    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("servlet post");

        processRequest(req, res);
    }

    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        out.println("servlet get");

        processRequest(req, res);
    }

    protected void processRequest(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException{
        res.setContentType("text/plain");
        PrintWriter out = res.getWriter();
        String url = req.getRequestURL().toString();
        out.println(url);

        ArrayList<String> listControler = allControlerName();
        out.println("");
        out.println("Liste des controllers : ");
        for(int i=0 ; i<listControler.size() ; i++){
            out.println(listControler.get(i));
        }

        out.println("");
        out.print("Fonction correspondant a l'url : \t");
        String slach = "/";
        String[] urlSplited = url.split(slach);
        Mapping map = annotedGetFunction.get(urlSplited[urlSplited.length-1]); 
        if(map != null){
            out.println(map.getClassName()+" -> "+map.getFunctionName());

            out.println("");
            out.println("valeur que la fonction retourne : "+this.callMethod(map));
        }else {
            out.print("Aucune fonction correspondante");
        }
    }

    public ArrayList<String> allControlerName (){
        ArrayList<String> listControler = new ArrayList<String>();
        try{
            String controllerPackage = getServletConfig().getInitParameter("Controllers");
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

    public void allAnnotedGetFunction ()throws Exception{
        ArrayList<String> listControler = this.allControlerName();
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

    public String callMethod(Mapping m){
        String val = "Tay";
        try{
            Class<?> classe = Class.forName(m.getClassName());
            Object obj = classe.newInstance();

            Method method = obj.getClass().getDeclaredMethod(m.getFunctionName(), null);
            // val = method.getName();
            val = (String) method.invoke(obj, null);
        }
        catch(Exception e){
            val = e.getMessage();
            e.printStackTrace();
        }
        return val;
    }
}