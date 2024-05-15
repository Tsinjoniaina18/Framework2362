package mg.itu.prom16;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import mg.itu.prom16.annotation.Controller;
import jakarta.servlet.RequestDispatcher;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.lang.Package;
import java.net.URL;

public class FrontController extends HttpServlet{

    boolean checked = false;
    ArrayList<String> listControler = new ArrayList<String>();

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
        out.println(req.getRequestURL().toString());

        if(!checked){
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
                            // out.println(files.length);
                            for(int i=0 ; i<files.length ; i++){
                                File file = files[i];
                                if(file.isFile() && file.getName().endsWith(".class")){
                                    String nom = file.getName().split("\\.")[0];
                                    // out.println(String.format("%s.%s" , controllerPackage, nom));
                                    Class<?> classe = Class.forName(String.format("%s.%s" , controllerPackage, nom));
                                    if(classe.isAnnotationPresent(Controller.class)){
                                        listControler.add(classe.getName());
                                        // out.println(classe.getName());
                                    }
                                    // out.println(classe.getName());
                                }
                                // out.println(file.getName());
                            }
                        }
                    }
                }
                checked = true;
            }catch(Exception e){
                // out.println(e.getMessage());
            }
        }
        out.println("");
        out.println("Liste des controllers : ");
        for(int i=0 ; i<listControler.size() ; i++){
            out.println(listControler.get(i));
        }
    }
}